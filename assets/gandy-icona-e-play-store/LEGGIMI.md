# Gandy – Icona app e grafiche Google Play

## android/res → icona launcher (adaptive icon)

Copia il contenuto di `android/res/` dentro `app/src/main/res/` (sovrascrive le `ic_launcher*` di default).

- `mipmap-anydpi-v26/ic_launcher.xml` e `ic_launcher_round.xml`: adaptive icon con background, foreground e monochrome (icone a tema di Android 13+).
- `mipmap-*dpi/ic_launcher_foreground.png`, `ic_launcher_background.png`, `ic_launcher_monochrome.png`: layer a 108 dp.
- `mipmap-*dpi/ic_launcher.png` e `ic_launcher_round.png`: icone legacy per Android 7.1 e precedenti.

Nel manifest servono, come nel template standard:

```xml
android:icon="@mipmap/ic_launcher"
android:roundIcon="@mipmap/ic_launcher_round"
```

Se nel progetto esistono `drawable/ic_launcher_background.xml` o `drawable-v24/ic_launcher_foreground.xml` del template, eliminali per evitare conflitti di nome.

## play-store → scheda dello store

| File | Dove si carica | Formato |
|---|---|---|
| `icon-512.png` | Icona dell'app | 512 × 512 PNG, senza trasparenza |
| `feature-graphic-1024x500.png` | Grafica in primo piano | 1024 × 500 PNG |
| `screenshots/01…05.png` | Screenshot telefono | 1080 × 1920 (9:16) PNG |

Sorgenti modificabili: canvas "Gandy – App Android", sezione "4 · Icona e Google Play Store".
