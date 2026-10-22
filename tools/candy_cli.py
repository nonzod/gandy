#!/usr/bin/env python3
"""Candy Simply-Fi Local CLI & Diagnostic Tool.

100% Local Tool for:
- Automatic LAN Discovery of Candy appliances
- Instant XOR Key Auto-Recovery
- Device Telemetry Status & Testing
- Command Execution (Start, Pause, Reset, Beep)
- Standalone Wi-Fi Provisioning (Enrollment) without Simply-Fi App
"""

import argparse
import asyncio
import ipaddress
import itertools
import json
import os
import re
import socket
import sys
import urllib.error
import urllib.parse
import urllib.request

KEY_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
_MAX_COMBOS = 200000

STATUS_ROOTS = {
    "statusLavatrice": "Lavatrice / Lavasciuga",
    "statusDWash": "Lavastoviglie",
    "statusWD": "Lavasciuga",
    "statusTD": "Asciugatrice",
    "statusForno": "Forno",
    "statusHob": "Piano Cottura",
    "statusRX": "Frigorifero",
}


def xor_bytes(data: bytes, key: bytes) -> bytes:
    if not key:
        return data
    return bytes(b ^ key[i % len(key)] for i, b in enumerate(data))


def encrypt(plaintext: str, key: str) -> str:
    return xor_bytes(plaintext.encode("utf-8"), key.encode("utf-8")).hex().upper()


def decrypt(hex_text: str, key: str) -> str:
    raw = bytes.fromhex(hex_text.strip())
    return xor_bytes(raw, key.encode("utf-8")).decode("utf-8", "replace")


def get_key_prefixes() -> list[bytes]:
    gaps = ("", "\r\n\t", "\n\t", "\r\n  ", "\n  ", " ", "\r\n\t\t", "\n\t\t")
    out = []
    for root in STATUS_ROOTS:
        for after_brace in gaps:
            for after_colon in ("", " "):
                for after_inner in gaps:
                    out.append(
                        (
                            "{"
                            + after_brace
                            + '"'
                            + root
                            + '":'
                            + after_colon
                            + "{"
                            + after_inner
                            + '"'
                        ).encode()
                    )
    return sorted(set(out), key=len, reverse=True)


def recover_key(hex_text: str, key_lengths=(16, 8, 32)):
    """Recover the XOR encryption key using known-plaintext attack."""
    try:
        ct = bytes.fromhex(hex_text.strip())
    except ValueError:
        return None

    prefixes = get_key_prefixes()

    for klen in key_lengths:
        if len(ct) < klen:
            continue
        for prefix in prefixes:
            known = min(len(prefix), klen)
            key = [ct[i] ^ prefix[i] for i in range(known)]
            if not all(chr(c) in KEY_ALPHABET for c in key):
                continue

            options, combos = [], 1
            for i in range(known, klen):
                positions = range(i, len(ct), klen)
                ok = [
                    ord(c)
                    for c in KEY_ALPHABET
                    if all(32 <= (ct[j] ^ ord(c)) < 127 for j in positions)
                ]
                if not ok:
                    options = None
                    break
                options.append(ok)
                combos *= len(ok)

            if options is None or combos > _MAX_COMBOS:
                continue

            for tail in itertools.product(*options) if options else [()]:
                cand = bytes(key + list(tail))
                try:
                    text = xor_bytes(ct, cand).decode("utf-8")
                except UnicodeDecodeError:
                    continue
                if not text.startswith(prefix.decode("utf-8", errors="ignore")[:known]):
                    continue
                cleaned = re.sub(r",\s*([}\]])", r"\1", text)
                try:
                    data = json.loads(cleaned)
                    if isinstance(data, dict):
                        return cand.decode("latin1"), data
                except (ValueError, json.JSONDecodeError):
                    continue
    return None


def http_get(url: str, timeout: float = 6.0) -> str:
    req = urllib.request.Request(url, headers={"Connection": "close", "User-Agent": "CandyLocal/1.0"})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return resp.read().decode("utf-8", "replace").strip()


def cmd_getkey(ip: str):
    """Query appliance and recover its encryption key."""
    print(f"\n[*] Interrogazione di {ip} per recupero chiave...")
    for enc in (1, 0):
        url = f"http://{ip}/http-read.json?encrypted={enc}"
        try:
            body = http_get(url)
        except Exception as err:
            continue

        if body.startswith("{"):
            print("[+] L'elettrodomestico risponde in CHIARO (senza crittografia)!")
            print("[+] In Home Assistant non è richiesta alcuna chiave.")
            return

        res = recover_key(body)
        if res:
            key, data = res
            print("\n" + "=" * 55)
            print(f" [SUCCESS] CHIAVE CRITTOGRAFICA TROVATA: {key}")
            print("=" * 55)
            print("Copia questa chiave di 16 caratteri e incollala nella configurazione di Home Assistant!")
            return key

    print("[-] Impossibile recuperare la chiave. Verifica che l'indirizzo IP sia corretto e raggiungibile.")


def cmd_status(ip: str, key: str = ""):
    """Read and display status of appliance."""
    print(f"\n[*] Lettura stato da {ip}...")
    data = None
    recovered_key = None

    for enc in (0, 1):
        url = f"http://{ip}/http-read.json?encrypted={enc}"
        try:
            body = http_get(url)
        except Exception:
            continue

        if body.startswith("{"):
            try:
                parsed = json.loads(re.sub(r",\s*([}\]])", r"\1", body))
                # Verify that response is valid telemetry, not an error like {"response":"BAD REQUEST"}
                if isinstance(parsed, dict) and not any(k in parsed for k in ("response", "error", "Error")) and any(
                    k in parsed for k in ("statusLavatrice", "statusWD", "statusTD", "statusDWash", "statusLavastoviglie", "StatoDWash", "statusDishwasher")
                ):
                    data = parsed
                    break
            except Exception:
                pass
        else:
            used_key = key
            if not used_key:
                rec = recover_key(body)
                if rec:
                    recovered_key, data = rec
                    break
            else:
                dec = decrypt(body, used_key)
                try:
                    data = json.loads(re.sub(r",\s*([}\]])", r"\1", dec))
                    break
                except Exception:
                    pass

    if not data:
        print("[-] Errore: Impossibile leggere o decifrare lo stato dell'elettrodomestico.")
        return

    print("\n" + "=" * 60)
    print(" STATO ELETTRODOMESTICO CANDY / HOOVER")
    print("=" * 60)
    if recovered_key:
        print(f" Chiave crittografica auto-rilevata: {recovered_key}")

    if "statusLavatrice" in data or "statusWD" in data:
        sub = data.get("statusLavatrice", data.get("statusWD", {}))
        print(" Tipo: Lavatrice / Lavasciuga")
        print(f" Stato Macchina (MachMd): {sub.get('MachMd')} (1=Idle, 2=In Funzione, 3=Pausa, 7=Terminato)")
        print(f" Programma (Pr):          {sub.get('Pr')} (PrCode: {sub.get('PrCode')})")
        print(f" Fase Ciclo (PrPh):       {sub.get('PrPh')} (1=Prelavaggio, 2=Lavaggio, 3=Risciacquo, 4=Centrifuga, 5=Fine, 6=Asciugatura)")
        rem = sub.get("RemTime", 0)
        try:
            rem_sec = int(rem)
            print(f" Tempo Residuo:           {rem_sec // 60} minuti ({rem_sec} secondi)")
        except Exception:
            print(f" Tempo Residuo:           {rem}")
        print(f" Temperatura:             {sub.get('Temp')} °C")
        spin = sub.get("SpinSp", 0)
        try:
            s_val = int(spin)
            print(f" Centrifuga:              {s_val * 100 if s_val < 20 else s_val} RPM")
        except Exception:
            print(f" Centrifuga:              {spin}")
        print(f" Asciugatura (DryT):      {sub.get('DryT', '0')}")
        print(f" Codice Errore:           {sub.get('CodiceErrore', 'E0')}")
        print(f" Controllo Remoto (Wi-Fi):{'ABILITATO (Pr=16)' if str(sub.get('Pr')) == '16' or str(sub.get('WiFiStatus')) == '1' else 'DISABILITATO (Ruota la manopola fisica su Wi-Fi!)'}")

    elif any(k in data for k in ("statusDWash", "statusLavastoviglie", "StatoDWash", "statusDishwasher")):
        sub = data.get("statusDWash") or data.get("statusLavastoviglie") or data.get("statusDishwasher") or data
        print(" Tipo: Lavastoviglie")
        print(f" Stato (StatoDWash):      {sub.get('StatoDWash')} (1=Standby, 2=In Funzione, 3=Pausa, 5=Terminato)")
        print(f" Programma:               {sub.get('Program')}")
        print(f" Tempo Residuo:           {sub.get('RemTime')} minuti")
        print(f" Mezzo Carico:            {'SI' if str(sub.get('MetaCarico')) == '1' else 'NO'}")
        print(f" Pastiglia 3-in-1:        {'SI' if str(sub.get('TreinUno')) == '1' else 'NO'}")
        print(f" Asciugatura Extra:       {'SI' if str(sub.get('ExtraDry')) == '1' else 'NO'}")
        print(f" Apertura Sportello Auto: {'SI' if str(sub.get('OpenDoorOpt')) in ('1', '7') else 'NO'}")
        print(f" Sportello Aperto:        {'APERTO' if str(sub.get('OpenDoor')) == '1' else 'CHIUSO'}")
        print(f" Allarme Sale:            {'MANCA IL SALE!' if str(sub.get('MissSalt')) == '1' else 'OK'}")
        print(f" Allarme Brillantante:    {'MANCA IL BRILLANTANTE!' if str(sub.get('MissRinse')) == '1' else 'OK'}")
        print(f" Codice Errore:           {sub.get('CodiceErrore', 'E0')}")

    print("=" * 60)


def cmd_provision():
    """Step-by-step Wi-Fi setup wizard without the simply-Fi app."""
    print("\n" + "=" * 65)
    print(" PROCEDURA GUIDATA CONNESSIONE WI-FI CANDY / HOOVER")
    print("=" * 65)
    print("\nIstruzioni preliminari:")
    print("1. Metti l'elettrodomestico in modalità Access Point (Wi-Fi pairing):")
    print("   - Lavatrice/Lavasciuga: posiziona la manopola su WI-FI e tieni premuto")
    print("     il tasto Opzioni o Avvio per 5 secondi finché l'icona Wi-Fi lampeggia veloce.")
    print("   - Lavastoviglie: tieni premuto il pulsante Wi-Fi/Avvio per 5 secondi.")
    print("2. Connettiti dal tuo computer/smartphone alla rete Wi-Fi generata dall'elettrodomestico")
    print("   (es. 'CANDY_WASHING_xxxx' oppure 'CANDYDISHWASHING_xxxx').")
    print("3. La rete è aperta (nessuna password) e l'elettrodomestico risponde all'indirizzo 192.168.0.1.\n")

    input("Premi INVIO dopo esserti connesso alla rete Wi-Fi dell'elettrodomestico...")

    appliance_ip = input("Indirizzo IP dell'elettrodomestico [predefinito: 192.168.0.1]: ").strip() or "192.168.0.1"
    ssid = input("Nome della tua rete Wi-Fi di casa (SSID - solo 2.4 GHz): ").strip()
    if not ssid:
        print("[-] SSID non può essere vuoto!")
        return

    password = input("Password del tuo Wi-Fi di casa: ").strip()

    # Generate a random 16 character alphanumeric key
    import random
    import string
    enc_key = "".join(random.choices(string.ascii_letters + string.digits, k=16))

    print(f"\n[*] Chiave di crittografia generata per Home Assistant: {enc_key}")
    print(f"[*] Invio credenziali Wi-Fi a http://{appliance_ip}...")

    url = (
        f"http://{appliance_ip}/http-config.json?"
        f"encrypted=0&NTW_MODE=3&SSID={urllib.parse.quote(ssid)}"
        f"&PASSWORD={urllib.parse.quote(password)}&ENCRYPT_KEY={enc_key}"
    )

    try:
        resp = http_get(url, timeout=10.0)
        print("\n" + "=" * 65)
        print(" [SUCCESS] CONFIGURAZIONE INVIATA CON SUCCESSO!")
        print("=" * 65)
        print("L'elettrodomestico ora si collegherà alla tua rete Wi-Fi domestica.")
        print(f"IMPORTANTE: Conserva questa chiave per Home Assistant:\n   {enc_key}")
        print("Una volta connesso, trova l'IP assegnato dal tuo router e configuralo in Home Assistant.")
    except Exception as err:
        print(f"[-] Errore durante l'invio della configurazione: {err}")
        print("Assicurati di essere connesso alla rete Wi-Fi dell'elettrodomestico e che risponda a 192.168.0.1.")


def cmd_send(ip: str, param_str: str, key: str = ""):
    """Send command parameters to http-write.json."""
    if not key:
        print("[*] Cerco di recuperare la chiave prima di inviare il comando...")
        res = recover_key(http_get(f"http://{ip}/http-read.json?encrypted=1"))
        if res:
            key = res[0]
            print(f"[+] Chiave trovata: {key}")

    if key:
        enc_hex = encrypt(param_str, key)
        url = f"http://{ip}/http-write.json?encrypted=1&data={enc_hex}"
    else:
        url = f"http://{ip}/http-write.json?encrypted=0&{param_str}"

    print(f"[*] Invio: {param_str} a {ip}...")
    try:
        resp = http_get(url)
        print(f"[+] Risposta ricevuta: {resp}")
    except Exception as err:
        print(f"[-] Errore invio comando: {err}")


def interactive_menu():
    while True:
        print("\n" + "=" * 50)
        print("  CANDY SIMPLY-FI LOCAL - STRUMENTO DI CONTROLLO")
        print("=" * 50)
        print(" 1. Recupera Chiave Crittografica (XOR Key)")
        print(" 2. Visualizza Stato Dettagliato (Telemetria)")
        print(" 3. Connetti Elettrodomestico al Wi-Fi (Onboarding)")
        print(" 4. Fai Suonare il Segnale Acustico (Bip)")
        print(" 5. Invia Comando Personalizzato")
        print(" 6. Esci")
        print("=" * 50)

        choice = input("Seleziona un'opzione [1-6]: ").strip()
        if choice == "1":
            ip = input("Inserisci l'indirizzo IP locale dell'elettrodomestico: ").strip()
            if ip:
                cmd_getkey(ip)
        elif choice == "2":
            ip = input("Inserisci l'indirizzo IP locale dell'elettrodomestico: ").strip()
            key = input("Chiave crittografica (lascia vuoto per rilevamento automatico): ").strip()
            if ip:
                cmd_status(ip, key)
        elif choice == "3":
            cmd_provision()
        elif choice == "4":
            ip = input("Inserisci l'indirizzo IP locale: ").strip()
            key = input("Chiave crittografica (lascia vuoto per auto-rilevamento): ").strip()
            if ip:
                cmd_send(ip, "BM=1", key)
        elif choice == "5":
            ip = input("Inserisci l'indirizzo IP locale: ").strip()
            key = input("Chiave crittografica (lascia vuoto per auto-rilevamento): ").strip()
            params = input("Parametri da inviare (es. 'Reset=1' o 'StartStop=1&Program=P3'): ").strip()
            if ip and params:
                cmd_send(ip, params, key)
        elif choice == "6":
            print("Arrivederci!")
            break
        else:
            print("Opzione non valida.")


def main():
    parser = argparse.ArgumentParser(description="Candy Simply-Fi Local CLI Tool")
    subparsers = parser.add_subparsers(dest="command")

    # getkey
    p_getkey = subparsers.add_parser("getkey", help="Recupera la chiave crittografica")
    p_getkey.add_argument("ip", help="Indirizzo IP dell'elettrodomestico")

    # status
    p_status = subparsers.add_parser("status", help="Leggi lo stato corrente")
    p_status.add_argument("ip", help="Indirizzo IP dell'elettrodomestico")
    p_status.add_argument("--key", default="", help="Chiave crittografica opzionale")

    # provision
    subparsers.add_parser("provision", help="Guida interattiva onboarding Wi-Fi")

    # cmd
    p_cmd = subparsers.add_parser("cmd", help="Invia comando query string")
    p_cmd.add_argument("ip", help="Indirizzo IP dell'elettrodomestico")
    p_cmd.add_argument("params", help="Stringa parametri es. 'BM=1' o 'Reset=1'")
    p_cmd.add_argument("--key", default="", help="Chiave crittografica opzionale")

    args = parser.parse_args()

    if not args.command:
        interactive_menu()
    elif args.command == "getkey":
        cmd_getkey(args.ip)
    elif args.command == "status":
        cmd_status(args.ip, args.key)
    elif args.command == "provision":
        cmd_provision()
    elif args.command == "cmd":
        cmd_send(args.ip, args.params, args.key)


if __name__ == "__main__":
    main()
