# Gandy

App Android per controllare lavatrici Candy/Hoover con modulo WiFi SimplyFi, 100% in locale sulla LAN.

## Stack

Kotlin, Jetpack Compose, Material 3, Hilt, OkHttp, DataStore. Min SDK 26.

## Build

```bash
# Solo build release
tools/build.sh

# Build con incremento versione
tools/build.sh --update-version fix       # 0.2.0 → 0.2.1
tools/build.sh --update-version release   # 0.2.0 → 0.3.0
tools/build.sh --update-version major     # 0.2.0 → 1.0.0
```

L'APK viene generato come `gandy-<versione>.apk` in `app/build/outputs/apk/release/`.

Il flag `--update-version` aggiorna `versionName` e `versionCode` in `app/build.gradle.kts` prima di compilare.

## Tools

| Script | Descrizione |
|---|---|
| `tools/build.sh` | Build release con versioning opzionale |
| `tools/candy_cli.py` | CLI per comunicare con la lavatrice |
| `candy_discover.py` | Discovery appliance sulla LAN |
