#!/usr/bin/env python3
"""
Candy SimplyFi - Discovery & Status Reader
Finds your Candy/Hoover washing machine on the local network
and reads its status via the local HTTP API.

Usage:
    pip install zeroconf requests
    python3 candy_discover.py                  # auto-discover via mDNS + subnet scan
    python3 candy_discover.py 192.168.1.42     # direct IP if you already know it
"""

import sys
import json
import socket
import struct
import itertools
import string
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests

APPLIANCE_KEYS = {
    "statusLavatrice": "Lavatrice (Washing Machine)",
    "statusWD": "Washer-Dryer",
    "statusTD": "Tumble Dryer",
    "statusDWash": "Dishwasher",
    "statusLavastoviglie": "Dishwasher (IT)",
    "StatoDWash": "Dishwasher (alt)",
    "statusDishwasher": "Dishwasher (EN)",
    "statusForno": "Oven",
    "statusHob": "Hob",
    "statusRX": "Fridge",
    "statusWCool": "Wine Cooler",
}

STATUS_FIELDS = {
    "MachMd": "Machine Mode",
    "Pr": "Program Number",
    "PrPh": "Program Phase",
    "Temp": "Temperature (°C)",
    "SpinSp": "Spin Speed (x100 rpm)",
    "RemTime": "Remaining Time (min)",
    "DelVal": "Delay Start (min)",
    "Err": "Error Code",
    "WiFiStatus": "WiFi Status",
    "SLevel": "Soil Level",
    "DryT": "Dry Time",
    "Steam": "Steam",
    "CheckUpState": "CheckUp State",
}

MACHINE_MODES = {
    "0": "Not running",
    "1": "Pre-wash",
    "2": "Washing",
    "3": "Rinse",
    "4": "Spin / Last rinse",
    "5": "End",
    "6": "Drying",
    "7": "Error",
    "8": "Steam",
    "9": "Night Dry",
}

PROGRAM_PHASES = {
    "0": "Ready",
    "1": "Pre-wash",
    "2": "Wash",
    "3": "Rinse",
    "4": "Final rinse",
    "5": "Spin",
    "6": "Drain",
    "7": "End",
    "8": "Drying",
    "9": "Error",
    "10": "Steam",
    "11": "Scheduled start",
    "12": "Refresh",
}


CANDY_AP_IPS = ["192.168.0.1", "192.168.4.1", "192.168.1.1", "10.0.0.1"]


def get_local_ip():
    """Get the local IP address of this machine."""
    # Try internet-routable address first
    for dest in [("8.8.8.8", 80), ("192.168.0.1", 80), ("192.168.4.1", 80)]:
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.settimeout(2)
            s.connect(dest)
            ip = s.getsockname()[0]
            s.close()
            if ip and not ip.startswith("127."):
                return ip
        except Exception:
            pass
    # Last resort: parse interfaces
    try:
        import subprocess
        out = subprocess.check_output(["ip", "-4", "addr"], text=True, timeout=5)
        for line in out.splitlines():
            line = line.strip()
            if line.startswith("inet ") and "127.0.0.1" not in line:
                return line.split()[1].split("/")[0]
    except Exception:
        pass
    return None


def get_subnet_hosts(local_ip):
    """Generate all IPs in the /24 subnet of local_ip."""
    parts = local_ip.rsplit(".", 1)
    base = parts[0]
    return [f"{base}.{i}" for i in range(1, 255)]


def check_host(ip, timeout=1.5):
    """Check if a host has port 80 open and responds like a Candy appliance."""
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(timeout)
        result = sock.connect_ex((ip, 80))
        sock.close()
        if result == 0:
            return ip
    except Exception:
        pass
    return None


def try_read_status(ip, timeout=8):
    """Try to read appliance status from the given IP."""
    for encrypted in [0, 1]:
        url = f"http://{ip}/http-read.json?encrypted={encrypted}"
        try:
            resp = requests.get(url, timeout=timeout, headers={"Connection": "close"})
            if resp.status_code == 200 and resp.text.strip():
                return resp.text.strip(), encrypted
        except Exception:
            continue
    return None, None


def check_ap_mode(ip, timeout=5):
    """Check if a Candy appliance is in AP (provisioning) mode."""
    try:
        resp = requests.get(
            f"http://{ip}/http-config.json?encrypted=0",
            timeout=timeout,
            headers={"Connection": "close"},
        )
        if resp.status_code == 200:
            return True
    except Exception:
        pass
    # Some models respond on root or with a simple GET
    try:
        resp = requests.get(
            f"http://{ip}/",
            timeout=timeout,
            headers={"Connection": "close"},
        )
        if resp.status_code == 200:
            return True
    except Exception:
        pass
    return False


def xor_decrypt(data_hex, key):
    """Decrypt XOR-encrypted hex data with a repeating key."""
    try:
        raw = bytes.fromhex(data_hex.strip())
    except ValueError:
        return None
    key_bytes = key.encode("utf-8") if isinstance(key, str) else key
    decrypted = bytes(b ^ key_bytes[i % len(key_bytes)] for i, b in enumerate(raw))
    try:
        return decrypted.decode("utf-8", errors="replace")
    except Exception:
        return None


def recover_xor_key(data_hex, key_length=16):
    """
    Recover XOR key using known-plaintext attack.
    Candy JSON always starts with '{"status...' so we can recover the key.
    """
    try:
        raw = bytes.fromhex(data_hex.strip())
    except ValueError:
        return None

    known_prefixes = [
        b'{"statusLavatric',
        b'{"statusWD":{"',
        b'{"statusTD":{"',
        b'{"statusDWash":{',
        b'{"statusLavasto',
        b'{"StatoDWash":{',
        b'{"statusDishwas',
        b'{"statusForno":{',
        b'{"statusHob":{"',
        b'{"statusRX":{"',
        b'{"statusWCool":{',
    ]

    for prefix in known_prefixes:
        test_len = min(len(prefix), key_length, len(raw))
        candidate_key = bytearray(key_length)
        known_positions = set()

        for i in range(test_len):
            candidate_key[i % key_length] = raw[i] ^ prefix[i]
            known_positions.add(i % key_length)

        if len(known_positions) == key_length:
            decrypted = xor_decrypt(data_hex, bytes(candidate_key))
            if decrypted and is_valid_json(decrypted):
                return bytes(candidate_key), decrypted

        # Try with CR/LF/tab variations at the start
        for ws in [b'\r\n', b'\n', b'\t', b' ']:
            padded_prefix = ws + prefix[:key_length - len(ws)]
            test_len = min(len(padded_prefix), key_length, len(raw))
            candidate_key = bytearray(key_length)
            known_positions = set()
            for i in range(test_len):
                candidate_key[i % key_length] = raw[i] ^ padded_prefix[i]
                known_positions.add(i % key_length)
            if len(known_positions) == key_length:
                decrypted = xor_decrypt(data_hex, bytes(candidate_key))
                if decrypted and is_valid_json(decrypted):
                    return bytes(candidate_key), decrypted

    return None


def is_valid_json(text):
    """Check if text is valid JSON (with trailing comma tolerance)."""
    cleaned = text.strip()
    # Candy firmware sometimes emits trailing commas
    cleaned = cleaned.replace(",}", "}").replace(",]", "]")
    try:
        obj = json.loads(cleaned)
        return isinstance(obj, dict) and any(k in obj for k in APPLIANCE_KEYS)
    except (json.JSONDecodeError, ValueError):
        return False


def parse_status(text):
    """Parse the JSON status, tolerating trailing commas."""
    cleaned = text.strip()
    cleaned = cleaned.replace(",}", "}").replace(",]", "]")
    return json.loads(cleaned)


def try_mdns_discovery(timeout=5):
    """Try to find Candy appliances via mDNS/ZeroConf."""
    try:
        from zeroconf import ServiceBrowser, Zeroconf

        found = []

        class Listener:
            def add_service(self, zc, type_, name):
                info = zc.get_service_info(type_, name)
                if info and info.addresses:
                    ip = socket.inet_ntoa(info.addresses[0])
                    found.append((ip, name))

            def remove_service(self, zc, type_, name):
                pass

            def update_service(self, zc, type_, name):
                pass

        zc = Zeroconf()
        for stype in ["_http._tcp.local.", "_workstation._tcp.local."]:
            ServiceBrowser(zc, stype, Listener())

        time.sleep(timeout)
        zc.close()
        return found
    except ImportError:
        return None


def print_header(text):
    print(f"\n{'=' * 60}")
    print(f"  {text}")
    print(f"{'=' * 60}")


def print_appliance_status(data):
    """Pretty-print the appliance status."""
    for root_key, appliance_name in APPLIANCE_KEYS.items():
        if root_key in data:
            print_header(f"Appliance: {appliance_name}")
            status = data[root_key]
            if not isinstance(status, dict):
                print(f"  Raw data: {status}")
                continue

            print()
            for field_key, field_name in STATUS_FIELDS.items():
                if field_key in status:
                    value = status[field_key]
                    extra = ""
                    if field_key == "MachMd":
                        extra = f" ({MACHINE_MODES.get(str(value), 'Unknown')})"
                    elif field_key == "PrPh":
                        extra = f" ({PROGRAM_PHASES.get(str(value), 'Unknown')})"
                    elif field_key == "SpinSp":
                        try:
                            rpm = int(value) * 100 if int(value) < 100 else int(value)
                            extra = f" ({rpm} rpm)"
                        except ValueError:
                            pass
                    print(f"  {field_name:.<30} {value}{extra}")

            print(f"\n  --- All raw fields ---")
            for k, v in sorted(status.items()):
                if k not in STATUS_FIELDS:
                    print(f"  {k:.<30} {v}")
            return True
    return False


def main():
    target_ip = sys.argv[1] if len(sys.argv) > 1 else None

    print_header("Candy SimplyFi - Discovery & Status Reader")
    print(f"  Model target: BWM4 137PH6/1-S (Bianca)")

    candidates = []

    if target_ip:
        print(f"\n[*] Using provided IP: {target_ip}")
        candidates = [target_ip]
    else:
        # Phase 1: mDNS
        print("\n[1/3] Trying mDNS/ZeroConf discovery...")
        mdns_results = try_mdns_discovery(timeout=4)
        if mdns_results is None:
            print("  zeroconf not installed (pip install zeroconf), skipping mDNS")
        elif mdns_results:
            print(f"  Found {len(mdns_results)} service(s) via mDNS:")
            for ip, name in mdns_results:
                print(f"    - {ip} ({name})")
                candidates.append(ip)
        else:
            print("  No services found via mDNS")

        # Phase 2: Subnet scan + known AP addresses
        local_ip = get_local_ip()
        if local_ip:
            subnet = local_ip.rsplit(".", 1)[0]
            print(f"\n[2/3] Scanning subnet {subnet}.0/24 for open port 80...")
            hosts = get_subnet_hosts(local_ip)
        else:
            print("\n[2/3] Could not determine local IP, scanning known AP addresses...")
            hosts = []

        # Always include known Candy AP-mode IPs
        for ap_ip in CANDY_AP_IPS:
            if ap_ip not in hosts:
                hosts.insert(0, ap_ip)

        with ThreadPoolExecutor(max_workers=50) as executor:
            futures = {executor.submit(check_host, ip): ip for ip in hosts}
            open_hosts = []
            for future in as_completed(futures):
                result = future.result()
                if result:
                    open_hosts.append(result)

        open_hosts.sort(key=lambda x: tuple(int(p) for p in x.split(".")))
        print(f"  Found {len(open_hosts)} host(s) with port 80 open")

        for ip in open_hosts:
            if ip not in candidates:
                candidates.append(ip)

    # Phase 3: Try each candidate
    print(f"\n[3/3] Probing {len(candidates)} candidate(s) for Candy API...")
    found_any = False

    for ip in candidates:
        print(f"\n  Trying {ip}...")

        raw_data, encrypted = try_read_status(ip)
        if raw_data is None:
            if check_ap_mode(ip):
                print(f"    [!] Appliance in AP MODE (provisioning) at {ip}")
                print(f"        The machine is broadcasting its own WiFi for initial setup.")
                print(f"        It needs to be configured to join your home WiFi first.")
                print(f"")
                print(f"        Run the provisioning wizard:")
                print(f"          python3 tools/candy_cli.py provision")
                print(f"")
                print(f"        Or manually send config:")
                print(f"          http://{ip}/http-config.json?encrypted=1&NTW_MODE=3")
                print(f"            &SSID=<your_wifi>&PASSWORD=<your_password>&ENCRYPT_KEY=<16chars>")
                found_any = True
            else:
                print(f"    No response from /http-read.json")
            continue

        print(f"    Got response (encrypted={encrypted})")

        # Try to parse directly
        if encrypted == 0 or is_valid_json(raw_data):
            try:
                data = parse_status(raw_data)
                print(f"    Unencrypted JSON response!")
                print_appliance_status(data)
                found_any = True
                save_result(ip, data, None)
                continue
            except json.JSONDecodeError:
                pass

        # Check if it's hex-encoded plaintext
        try:
            decoded = bytes.fromhex(raw_data.strip()).decode("utf-8", errors="strict")
            if is_valid_json(decoded):
                data = parse_status(decoded)
                print(f"    Hex-encoded plaintext JSON!")
                print_appliance_status(data)
                found_any = True
                save_result(ip, data, None)
                continue
        except (ValueError, UnicodeDecodeError):
            pass

        # Try XOR key recovery
        if encrypted == 1:
            print(f"    Attempting XOR key recovery...")
            for key_len in [16, 8, 32]:
                result = recover_xor_key(raw_data, key_len)
                if result:
                    key, decrypted_text = result
                    key_str = key.decode("ascii", errors="replace")
                    print(f"    KEY FOUND! (length={key_len}): {key_str}")
                    data = parse_status(decrypted_text)
                    print_appliance_status(data)
                    found_any = True
                    save_result(ip, data, key_str)
                    break
            else:
                print(f"    Could not recover XOR key.")
                print(f"    Raw response (first 200 chars): {raw_data[:200]}")

    if not found_any:
        print_header("No Candy appliance found")
        print("""
  Possible causes:
  1. The washing machine is not connected to WiFi yet
     -> Use the Candy SimplyFi app to do initial WiFi setup
     -> Or use the onboarding script from candysimply project
  2. The machine is on a different subnet
     -> Try: python3 candy_discover.py <machine_ip>
  3. The machine is in standby / WiFi module is sleeping
     -> Turn the machine on and wait ~30 seconds
  4. Your model uses NFC-only (no WiFi)
     -> Check if your BWM4 137PH6/1-S has the WiFi indicator
""")
    else:
        print_header("Discovery complete!")
        print("  Config saved to candy_config.json")
        print("  You can now run: python3 candy_discover.py <ip> to read status anytime")


def save_result(ip, data, xor_key):
    """Save the discovered config for reuse."""
    config = {
        "appliance_ip": ip,
        "xor_key": xor_key,
        "encrypted": xor_key is not None,
        "model": "BWM4 137PH6/1-S",
    }
    for root_key in APPLIANCE_KEYS:
        if root_key in data:
            config["appliance_type"] = root_key
            break

    with open("candy_config.json", "w") as f:
        json.dump(config, f, indent=2)
    print(f"\n  Config saved: {json.dumps(config, indent=2)}")


if __name__ == "__main__":
    main()
