# Gandy - Mappa completa delle schermate

App Android nativa in Jetpack Compose + Material 3 per controllare lavatrici Candy via Wi-Fi locale.
Navigazione semplice a 3 schermate (no bottom nav, no drawer), con 1 dialog modale.

---

## Tema e design system

- **Material 3** con Dynamic Color su Android 12+ (fallback su palette custom)
- **Light/Dark mode** automatico (segue il sistema)
- **Palette fallback light:** primary `#0061A4` (blu), tertiary `#006D3B` (verde), error `#BA1A1A`, surface `#F8F9FF`
- **Palette fallback dark:** primary `#9ECAFF`, tertiary `#80D99B`, error `#FFB4AB`, surface `#111318`
- **Tipografia custom** (GandyTypography): display 48/40sp Light, headline 28/24sp SemiBold, title 20/16/14sp Medium, body 16/14/12sp Normal, label 14/12/11sp Medium
- **Angoli arrotondati** coerenti: Card 16-24dp, Button 14-16dp, TextField 12dp, Badge 8dp
- **Lingua UI:** italiano

---

## Componenti condivisi

### GandyTopBar
Top app bar centrata, sfondo `surface`. Freccia back opzionale a sinistra, slot azioni a destra.

### StatusBadge
Etichetta in maiuscolo con `letterSpacing 1sp`, `fontWeight Bold`, `labelMedium`. Sfondo = colore al 15% alpha, angoli 8dp, padding 10x4dp.

### InfoRow
Riga label-valore a larghezza piena: label a sinistra in `bodyMedium` grigio (`onSurfaceVariant`), valore a destra in `bodyLarge` Medium.

### ErrorCard
Card `errorContainer`, angoli 16dp. Riga con icona Warning in `error` (20dp), testo errore in `bodyMedium`, pulsante "OK" per dismiss.

### SelectionDropdown
`ExposedDropdownMenuBox` read-only. `OutlinedTextField` con trailing icon freccia, angoli 12dp, full width. Menu a tendina con `DropdownMenuItem`.

### NumberedStep
Riga con cerchietto numerato (24dp, `primaryContainer`, numero in `labelSmall` Bold) + testo in `bodyLarge` con padding top 2dp.

---

## Schermata 1 - Lista dispositivi (schermata iniziale)

### Struttura
- **Top bar:** titolo "Gandy" in `headlineMedium` Bold, nessun pulsante back, nessuna azione
- **FAB:** angolo basso-destra, icona `+` (Add), angoli 16dp

### Variante A - Stato vuoto (nessun dispositivo salvato)
Contenuto centrato verticalmente e orizzontalmente, padding 32dp:

1. **Icona** lavatrice (`LocalLaundryService` outlined), 72dp, colore `outlineVariant`
2. **Spaziatore** 20dp
3. **Titolo** "Nessun dispositivo" in `headlineMedium`, colore `onSurfaceVariant`
4. **Spaziatore** 8dp
5. **Sottotitolo** "Aggiungi la tua lavatrice per iniziare" in `bodyMedium`, colore `outline`
6. **Spaziatore** 28dp
7. **Pulsante pieno** "Aggiungi dispositivo" — 70% larghezza, altezza 52dp, angoli 14dp
8. **Spaziatore** 12dp
9. **Pulsante outline** "Configura nuovo (Wi-Fi)" — icona Wi-Fi (18dp) + testo, 70% larghezza, altezza 52dp, angoli 14dp

### Variante B - Con dispositivi
`LazyColumn` full size, padding orizzontale 20dp, spacing verticale 10dp:

- **Per ogni dispositivo → DeviceCard:**
  - Card full width, sfondo `surfaceVariant` al 50% alpha, angoli 16dp, clickable
  - Riga con padding 16dp:
    - Icona lavatrice (`LocalLaundryService` outlined) 36dp, colore `primary`
    - Spaziatore 16dp
    - Colonna (peso 1f):
      - Nome dispositivo in `titleMedium` SemiBold
      - Spaziatore 2dp
      - Indirizzo IP in `bodySmall`, colore `onSurfaceVariant`
    - IconButton cestino (`Delete`) 20dp, colore `error` al 70% alpha
    - Freccia destra (`KeyboardArrowRight`), colore `onSurfaceVariant` al 50% alpha

- **In coda alla lista:**
  - Pulsante outline "Configura nuovo dispositivo" — icona Wi-Fi (18dp) + testo, full width, altezza 48dp, angoli 14dp, padding verticale 8dp
  - Spaziatore finale 80dp (per non coprire il FAB)

### Dialog modale - Aggiungi dispositivo
`AlertDialog` Material 3:
- **Titolo:** "Aggiungi dispositivo"
- **Campo 1:** "Nome" — OutlinedTextField, placeholder "es. Lavatrice", singleLine, angoli 12dp, full width
- **Campo 2:** "Indirizzo IP" — OutlinedTextField, placeholder "es. 192.168.1.100", singleLine, angoli 12dp, full width
- **Spaziatura** tra campi: 12dp
- **Pulsanti:** "Annulla" (TextButton) | "Aggiungi" (TextButton, abilitato solo se entrambi i campi non vuoti)

---

## Schermata 2 - Controllo lavatrice

### Struttura
- **Top bar:** titolo = nome dispositivo in SemiBold, freccia back a sinistra, icona Refresh a destra
- **Layout:** colonna scrollabile verticalmente, padding orizzontale 20dp, spacing 16dp tra elementi

### Variante A - Caricamento iniziale (nessun dato ricevuto)
`CircularProgressIndicator` centrato orizzontalmente, con padding top 120dp.

### Variante B - Remoto non abilitato
**RemoteWarningCard** (mostrata in cima, prima di qualsiasi altro contenuto):
- Card sfondo `errorContainer`, angoli 16dp, full width
- Riga con padding 16dp:
  - Icona Warning 24dp, colore `error`
  - Spaziatore 12dp
  - Testo "Porta la manopola sulla posizione Wi-Fi per abilitare il controllo remoto." in `bodyMedium`, colore `onErrorContainer`

### Variante C - Macchina IDLE (pronta, remoto abilitato)

**1. IdleCard**
Card sfondo `surfaceVariant` al 50% alpha, angoli 20dp, full width, padding interno 24dp, spacing 16dp:
- Riga superiore (SpaceBetween):
  - StatusBadge "PRONTA" in colore `primary`
  - CircularProgressIndicator 16dp (solo se loading)
- Divider orizzontale, colore `outlineVariant` al 50% alpha
- InfoRow "Programma" → nome del programma attuale (es. "Cotone Standard")
- InfoRow "Temperatura" → es. "40°C"
- InfoRow "Centrifuga" → es. "1200 rpm"

**2. ProgramControls**
Card angoli 20dp, full width, padding interno 20dp, spacing 16dp:
- Titolo "Impostazioni programma" in `titleMedium` SemiBold
- SelectionDropdown "Programma" — opzioni:
  - Cotone Resistente, Cotone Standard, Sintetici e Colorati, Lana, Delicati
  - Rapido 14', Rapido 30', Rapido 44', Rapido 59'
  - Risciacqui, Scarico & Centrifuga, Vapore
- SelectionDropdown "Temperatura" — opzioni: Freddo, 20°C, 30°C, 40°C, 60°C, 90°C (valori variabili)
- SelectionDropdown "Centrifuga" — opzioni: No centrifuga, 400 rpm, 600 rpm, 800 rpm, 1000 rpm, 1200 rpm (valori variabili)

**3. Pulsante "Avvia programma"**
Button full width, altezza 56dp, angoli 16dp, sfondo `primary`:
- Icona PlayArrow 22dp + spaziatore 8dp + testo "Avvia programma" in `titleMedium`

### Variante D - Macchina IN FUNZIONE

**1. RunningCard**
Card angoli 24dp, sfondo `primaryContainer`, full width, padding 24dp, centrato:
- Riga superiore (SpaceBetween):
  - StatusBadge "IN FUNZIONE" in colore `primary`
  - CircularProgressIndicator 16dp (solo se loading)
- Spaziatore 20dp
- **TimerRing** (elemento centrale):
  - Cerchio 160dp con arco di progresso
  - Traccia di sfondo: `outlineVariant` al 40% alpha, stroke 8dp, cap Round
  - Arco progresso: colore `primary`, sweep calcolato da (tempoTotale - tempoRimanente) / tempoTotale * 360°
  - Al centro: tempo rimanente in formato "1h 23min" o "45min" (stile `displayMedium` a 36sp, fontWeight Light)
  - Sotto il tempo: nome fase corrente in `labelMedium` al 70% alpha
- Spaziatore 16dp
- Nome programma in `titleMedium` SemiBold, colore `onPrimaryContainer`
- Spaziatore 4dp
- Dettagli "40°C · 1200 rpm" in `bodyMedium`, colore `onPrimaryContainer` al 70% alpha

**2. PhaseIndicator**
Card angoli 16dp, sfondo `surfaceVariant` al 50% alpha, padding 12x14dp:
- Riga orizzontale con fasi del ciclo di lavaggio (SpaceBetween):
  - Per ogni fase (Prelavaggio, Lavaggio, Risciacquo, Centrifuga, Fine):
    - Linea connettore orizzontale tra le fasi (Divider peso 1f, spessore 2dp): `primary` se fase raggiunta, `outlineVariant` altrimenti
    - Pallino: 12dp se fase corrente, 8dp altrimenti
      - Colore: `primary` pieno se corrente, `primary` al 50% se superata, `outlineVariant` se futura
    - Spaziatore 6dp
    - Etichetta fase in `labelSmall`: Bold e `primary` se corrente, Normal e `onSurfaceVariant` altrimenti
  - Animazione colore pallino con `animateColorAsState`

**3. ActionButtons**
Riga orizzontale, spacing 12dp, full width:
- **Pulsante Pausa** (OutlinedButton): peso 1f, altezza 52dp, angoli 14dp, testo "Pausa" in `labelLarge`
- **Pulsante Stop** (FilledTonalButton): peso 1f, altezza 52dp, angoli 14dp, sfondo `errorContainer`, contenuto `onErrorContainer`. Icona Stop 20dp + testo "Stop" in `labelLarge`

### Variante E - Macchina IN PAUSA
Identica alla variante D con queste differenze:
- RunningCard sfondo `secondaryContainer` anziché `primaryContainer`
- StatusBadge "IN PAUSA" in colore `secondary`
- TimerRing arco in `secondary`, testi in `onSecondaryContainer`
- Nome e dettagli in `onSecondaryContainer`
- Al posto del pulsante Pausa: **Pulsante Riprendi** (Button pieno): peso 1f, altezza 52dp, angoli 14dp. Icona PlayArrow 20dp + testo "Riprendi" in `labelLarge`
- Pulsante Stop rimane invariato

### Variante F - Ciclo TERMINATO

**1. IdleCard** con sfondo `tertiaryContainer`:
- StatusBadge "TERMINATO" in colore `tertiary`
- Stesse InfoRow della variante C

**2. Pulsante "Resetta"**
FilledTonalButton full width, altezza 56dp, angoli 16dp. Testo "Resetta" in `titleMedium`.

### Variante G - ERRORE

**1. IdleCard** con sfondo `errorContainer`:
- StatusBadge con stato in colore `error`
- Testo "Errore: E{codice}" in `bodyLarge` SemiBold, colore `error`
- Stesse InfoRow della variante C

**2. ErrorCard** (se presente un errore di comunicazione):
- Card `errorContainer`, icona Warning, testo errore, pulsante "OK"

---

## Schermata 3 - Configurazione nuovo dispositivo (Provision)

### Struttura
- **Top bar:** titolo "Nuovo dispositivo" in SemiBold, freccia back a sinistra, nessuna azione destra
- **Layout:** colonna scrollabile verticalmente, padding orizzontale 20dp, spacing 16dp

### StepIndicator (sempre visibile in cima)
Riga orizzontale full width, 3 step equamente distribuiti (peso 1f ciascuno):

Per ogni step (Preparazione, Rete Wi-Fi, Salvataggio):
- Cerchio 28dp:
  - Se step completato: sfondo `primary`, icona Check 16dp in `onPrimary`
  - Se step corrente: sfondo `primary`, numero in `labelSmall` Bold, colore `onPrimary`
  - Se step futuro: sfondo `outlineVariant`, numero in `labelSmall` Bold, colore `onSurfaceVariant`
- Spaziatore 4dp
- Etichetta in `labelSmall`:
  - Step attivo: colore `primary`, fontWeight Medium
  - Step futuro: colore `onSurfaceVariant`, fontWeight Normal

### Step 1 - Preparazione (istruzioni)

**Card istruzioni:** angoli 20dp, padding 24dp, spacing 16dp:
- Titolo "Preparazione" in `titleLarge` SemiBold
- NumberedStep 1: "Posiziona la manopola su Wi-Fi"
- NumberedStep 2: "Tieni premuto Avvio per 5 secondi fino al lampeggio rapido Wi-Fi"
- NumberedStep 3: "Connettiti alla rete aperta del dispositivo (es. CANDY_WASHING_xxxx)"
- NumberedStep 4: "Torna qui quando sei connesso"

**Pulsante:** "Sono connesso alla rete del dispositivo" — Button pieno, full width, altezza 56dp, angoli 16dp, testo in `titleMedium`

### Step 2 - Credenziali Wi-Fi

**Card credenziali:** angoli 20dp, padding 24dp, spacing 16dp:
- Titolo "Rete Wi-Fi di casa" in `titleLarge` SemiBold
- Descrizione "Inserisci le credenziali della tua rete Wi-Fi domestica (solo 2.4 GHz)." in `bodyMedium`, colore `onSurfaceVariant`
- OutlinedTextField "Nome rete (SSID)" — singleLine, angoli 12dp, full width
- OutlinedTextField "Password" — singleLine, angoli 12dp, full width

**Card errore (se presente):** sfondo `errorContainer`, angoli 16dp, testo in padding 16dp, colore `onErrorContainer`

**Pulsante:** "Invia configurazione" — Button pieno, full width, altezza 56dp, angoli 16dp, testo in `titleMedium`
- Disabilitato se SSID o password vuoti, o se in caricamento
- Se loading: CircularProgressIndicator 20dp in `onPrimary` + spaziatore 12dp + testo

### Step 3 - Salvataggio dispositivo

**Card successo:** sfondo `tertiaryContainer`, angoli 20dp, padding 24dp, spacing 12dp:
- Titolo (stringResource provision_sent_title) in `titleLarge` SemiBold
- Descrizione (stringResource provision_sent_description) in `bodyMedium`
- NumberedStep 1-3: istruzioni finali per riconnessione alla rete domestica

**Card dati dispositivo:** angoli 20dp, padding 24dp, spacing 16dp:
- Riga chiave generata:
  - Label "Chiave:" in `titleSmall`, colore `onSurfaceVariant`
  - Spaziatore 8dp
  - Valore chiave in `bodyLarge` Bold, colore `primary`
- OutlinedTextField "Nome dispositivo" — placeholder "es. Lavatrice", singleLine, angoli 12dp, full width
- OutlinedTextField "Indirizzo IP" — placeholder "es. 192.168.1.100", singleLine, angoli 12dp, full width

**Pulsante:** "Salva" — Button pieno, full width, altezza 56dp, angoli 16dp, testo in `titleMedium`
- Disabilitato se nome o IP vuoti

---

## Riepilogo flusso di navigazione

```
┌─────────────────────┐
│  1. Lista dispositivi│ ← schermata iniziale
│     (DeviceList)     │
└──────┬──────┬────────┘
       │      │
       │      │ tap "Configura nuovo"
       │      ▼
       │  ┌──────────────────────┐
       │  │ 3. Provision         │
       │  │    Step 1: Istruzioni│
       │  │    Step 2: Wi-Fi     │
       │  │    Step 3: Salva     │
       │  └──────────┬───────────┘
       │             │ back / salva
       │             ▼
       │     (torna a Lista)
       │
       │ tap su dispositivo
       ▼
┌──────────────────────────┐
│ 2. Controllo lavatrice   │
│    (ControlScreen)       │
│                          │
│  Loading → Idle → Running│
│  → Paused → Done → Error │
└──────────┬───────────────┘
           │ back
           ▼
    (torna a Lista)
```

### Icone Material utilizzate
- `LocalLaundryService` (outlined) — icona lavatrice
- `Add` — FAB aggiunta
- `Delete` — rimozione dispositivo
- `KeyboardArrowRight` — freccia navigazione
- `ArrowBack` — navigazione indietro
- `Refresh` — aggiornamento stato
- `Warning` — errori e avvisi
- `PlayArrow` — avvio/riprendi programma
- `Stop` — arresto programma
- `Check` — step completato
- `Wifi` (outlined) — provisioning Wi-Fi
