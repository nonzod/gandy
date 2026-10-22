# Gandy — App Android per Candy SimplyFi

## Obiettivo

App Android nativa per controllare lavatrici Candy/Hoover con modulo WiFi SimplyFi.
Comunicazione **100% locale** (HTTP sulla LAN), nessuna dipendenza dal cloud Candy.

## Modello di riferimento

**Candy BWM4 137PH6/1-S** (linea Bianca), 7kg, 1300rpm, WiFi + Bluetooth.

## Protocollo locale (porta 80)

### Endpoint

| Endpoint | Metodo | Descrizione |
|---|---|---|
| `/http-read.json?encrypted=0` | GET | Stato in JSON chiaro |
| `/http-read.json?encrypted=1` | GET | Stato cifrato XOR (hex) |
| `/http-write.json?encrypted=0&key=val&...` | GET | Invio comandi in chiaro |
| `/http-write.json?encrypted=1&data=HEX` | GET | Invio comandi cifrati |
| `/http-config.json?encrypted=1&...` | GET | Provisioning WiFi (solo in AP mode) |

### Cifratura XOR

- Chiave: 16 caratteri alfanumerici, ripetuta ciclicamente
- Encrypt: `plaintext_bytes XOR key_bytes` → output hex uppercase
- Decrypt: hex → bytes, XOR con chiave, decode UTF-8
- Recupero chiave: attacco known-plaintext sul prefisso JSON noto (`{"statusLavatrice":{`)

### Risposta stato (JSON)

Root key: `statusLavatrice` per lavatrici.

Campi principali:

| Campo | Significato | Valori |
|---|---|---|
| `MachMd` | Stato macchina | 0=Off, 1=Pre-lavaggio, 2=Lavaggio, 3=Risciacquo, 4=Centrifuga, 5=Fine, 7=Errore |
| `Pr` | Numero programma | intero |
| `PrPh` | Fase corrente | 0=Pronto, 2=Lavaggio, 3=Risciacquo, 5=Centrifuga, 7=Fine |
| `Temp` | Temperatura °C | "30", "40", "60"... |
| `SpinSp` | Centrifuga | valore × 100 = RPM (se < 20) |
| `RemTime` | Tempo residuo | secondi |
| `Err` | Codice errore | "E0" = nessuno |
| `WiFiStatus` | Stato WiFi | "1" = connesso |

### Comandi (query string)

| Azione | Parametri |
|---|---|
| Avvia programma | `Write=1&StartStop=1&StSt=1&PrNm=X&Pr=X&Temp=60&SpinSp=8` |
| Stop/Reset | `Reset=1&StartStop=0&StSt=0` |
| Pausa | `Pa=1` |
| Buzzer test | `BM=1` |

**Nota:** la manopola fisica deve essere su **WI-FI** per accettare comandi remoti.

## Stack tecnologico

| Componente | Scelta |
|---|---|
| Linguaggio | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Networking | Ktor Client o OkHttp |
| Discovery | Android NSD (mDNS) + scansione subnet |
| Architettura | MVVM (ViewModel + StateFlow) |
| DI | Hilt |
| Persistenza | DataStore (config, chiave XOR, IP) |
| Min SDK | 26 (Android 8.0) |

## Moduli dell'app

1. **discovery** — Ricerca appliance sulla LAN (mDNS + port scan)
2. **protocol** — Client HTTP, cifratura XOR, parsing JSON stato
3. **ui/dashboard** — Stato live: programma, fase, tempo, temperatura, giri
4. **ui/controls** — Selezione programma, avvio, pausa, stop
5. **ui/settings** — Config IP manuale, chiave XOR, tema

## Flusso utente

1. Primo avvio → discovery automatico o inserimento IP manuale
2. Tentativo lettura `encrypted=0`, fallback su `encrypted=1` + key recovery
3. Dashboard con stato live (polling ogni 30s)
4. Pannello controlli per avvio/pausa/stop programmi
5. Notifiche fine ciclo

## Fonti e riferimenti

- [benedettosiddi/candysimply](https://github.com/benedettosiddi/candysimply) — Implementazione locale di riferimento
- [tools/candy_cli.py](tools/candy_cli.py) — Script CLI locale (scaricato nel progetto)
- [candy_discover.py](candy_discover.py) — Script di discovery e test
