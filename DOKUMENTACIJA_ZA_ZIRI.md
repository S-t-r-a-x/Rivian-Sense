# Rivian Sense - Dokumentacija Projekta

**Tim:** S-t-r-a-x  
**Projekat:** Rivian Sense - Kontekstualno svesna vozačka aplikacija  

---

## Pregled Rešenja

Rivian Sense je Android aplikacija koja detektuje emociono stanje vozača i okolinu u kojoj se kreće vozilo, i na osnovu toga predlaže pametne akcije koje mogu poboljšati bezbednost i komfor tokom vožnje.

Aplikacija radi tako što AI model analizira video sa kamere i podatke o vozilu u realnom vremenu, pa šalje predikcije Android aplikaciji preko WebSocket konekcije. Na osnovu toga što je model detektovao (npr. "vozač je umoran" ili "vozilo je na autoputu"), aplikacija automatski sugeriše relevantne akcije kao što su: breathing vežba za smirenje, pronalaženje najbližeg odmorišta, puštanje energične muzike, itd.

Ključne funkcionalnosti:
- Real-time detekcija mood-a vozača (Nervous/Tired/Neutral)
- Klasifikacija okoline (Highway/City/Forest/Parking)
- Pametne kontekstualne akcije (npr. ako je vozač umoran na autoputu, prioritet je rest stop)
- Gamifikacija kroz badge sistem - podstiče korisnike da više koriste safety features
- Reminderi koji se prikazuju tek kada vozač stane (ne tokom vožnje)

---

## Opis Rešenja

### Problem koji rešavamo
Vozači često budu pod stresom, umorom ili nisu dovoljno pažljivi tokom vožnje. To dovodi do potencijalnih bezbednosnih problema. Nema sistema koji bi proaktivno detektovao ova stanja i automatski predlagao akcije koje bi pomogle.

Naš pristup kombinuje AI model za detekciju stanja vozača, kontekstualnu logiku koja uzima u obzir i lokaciju vozila, i gamifikaciju koja podstiče bezbednije ponašanje.

### AI Model i Backend

Model prima video frame (224x224 RGB slika) i metadata o vozilu (brzina, nagib, GPS koordinate, itd - ukupno 9 parametara). 

Arhitektura:
- Video frame prolazi kroz EfficientNet-B0 (CNN model) i izvlači image features
- Metadata prolazi kroz mali fully-connected network i izvlači metadata features  
- Oba seta feature-a se spajaju (concatenate)
- Rezultat ide u dva odvojena "heada":
  - Prvi klasifikuje mood vozača (5 klasa: Relaxed/Focused/Stressed/Tired/Distracted)
  - Drugi klasifikuje scenu (6 klasa: City/Highway/Forest/Garage/Offroad/Traffic)

Koristimo dual-head arhitekturu jer su oba task-a povezana i dele iste feature-e, što čini model efikasnijim nego dva odvojena modela.

Metadata koje koristimo:
- altitude, displaySpeed, pitchAngle, rollAngle
- powerMeter, regenCapabilityPct, propulsionCapabilityPct  
- latitude, longitude

Trening:
- Dataset se sastoji od 4 foldera sa video frame-ovima (ukupno ~2500+ slika)
- Label-ovanje smo radili range-based strategijom (npr. frame 0-110 su garage+relaxed, 111-221 su city+relaxed, itd)
- Koristimo Adam optimizer sa learning rate 1e-3
- Loss funkcija je CrossEntropyLoss za oba task-a
- Trenirali smo 4 epochs sa batch size 16

Inference Server:
- Flask-SocketIO server na portu 5000
- Postavljen na lokalnoj mreži (192.168.40.152)
- Koristi WebSocket protokol (sa HTTP long-polling fallback-om)

Tok podataka:
1. Video frame dolazi sa kamere vozila
2. Model radi inference i dobija mood + scene predikcije
3. Server šalje JSON preko WebSocket-a: `{"mood": "nervous", "scene": "highway", "stop": false}`
4. Android aplikacija prima podatke i prikazuje relevantne akcije

---

### Android Aplikacija

Aplikacija je napisana u Kotlinu sa Jetpack Compose za UI. Koristimo MVVM arhitekturu.

Osnovni flow:
- MainActivity je single activity koja hostuje sve ekrane
- MainViewModel drži trenutni state (mood, scene, stop status) kao StateFlow
- DriverContextApi je WebSocket client koji se konektuje na Flask server i prima real-time update-e
- LoggingAction komponenta se bavi reminder sistemom
- SharedPreferences čuva settings, statistiku i badge-eve

State management:
- StateFlow za reaktivne UI update-e
- SharedPreferences za perzistenciju podataka
- NotificationManager za Android notifikacije

Ključne komponente:

**DriverContextApi** - WebSocket klijent
- Koristi Socket.IO Client biblioteku (verzija 2.0.1)
- Konektuje se na server i prima JSON update-e
- Ima reconnect logiku ako padne veza
- Kad dobije novi podatak, apdejtuje MainViewModel

**ContextualActionManager** - "Mozak" aplikacije
- Prima trenutni kontekst (mood + scene + da li je vozač stao)
- Na osnovu toga vraća listu preporučenih akcija

Primeri logike:
- Ako je vozač nervozan → predloži DND mode, breathing vežbu, mirnu muziku
- Ako je vozač umoran → predloži energičnu muziku, stretch vežbe, pauzu za kafu
- Ako je vozač umoran NA AUTOPUTU → to je prioritet #1, odmah predloži rest stop (bezbednost)
- Ako je vozač stao (stop=true) → prikaži stretch vežbe i sve remindere

Sistem ima i filter:
- Korisnik može u Settings-ima isključiti neke feature-e
- Ako je nešto isključeno, akcije za taj feature se neće ni prikazati
- Sve je reaktivno - čim korisnik promeni setting, UI se odmah update-uje

**Tipovi akcija** (ukupno 12):

1. Spotify integracija:
   - Calm Music - mirna muzika za smirenje
   - Energetic Music - energična muzika da te probudi
   - Podcast - zanimljiv sadržaj za duge vožnje

2. Telefon kontrola:
   - Enable Do Not Disturb - blokira pozive i notifikacije da se ne ometaš

3. Navigacija:
   - Navigate Home - najbrža ruta kući
   - Rest Stop - pronađi najbliže odmorište (posebno važno ako si umoran na autoputu)
   - Coffee Break - pronađi najbližu kafeteriju

4. Wellbeing akcije:
   - Breathing Exercise - animirana vežba disanja (12-sekundni ciklusi)
   - Stretch Exercises - vežbe istezanja

5. Logovanje:
   - End Drive - završi vožnju i sačuvaj statistiku
   - Post-Drive Reminders - podsetnici koji se prikazuju tek kad staneš

**Statistika i Badge sistem**

Aplikacija prati koliko vremena si proveo u kom mood-u i na kojim lokacijama. To sve prikazuje na Stats ekranu sa progress bar-ovima i procentima.

Badge-evi (8 komada):
- First Drive - prva vožnja
- Experienced Driver - 5 ili više vožnji
- Long Hauler - proveo 2+ sata na autoputu
- City Navigator - proveo sat+ u gradu
- Zen Master - koristio breathing vežbu 30+ minuta
- Night Owl - vozio noću
- Safe Driver - 10+ vožnji bez stresa
- Explorer - bio na 3+ različite lokacije

Svaki badge ima progress bar koji pokazuje koliko ti još treba da ga otključaš. Npr. "3/5 drives remaining" za Experienced Driver badge.

**UI/UX Dizajn**

Koristimo Material3 sa dark temom. Boje su tamne (slate-950 za background, slate-800 za kartice) sa akcentima u purple, blue, green, gold i cyan bojama.

Aplikacija ima 4 ekrana:

1. **HomeScreen** - glavni ekran
   - Prikazuje trenutni mood sa emoji-jem (😌 za neutral, 😰 za nervous, 😴 za tired)
   - Prikazuje scenu takođe sa emoji-jem (🛣️ autoput, 🏙️ grad, 🌳 priroda, 🅿️ parking)
   - Lista pametnih akcija koje možeš kliknuti
   - Dugme za stats i test dugme (za debug)
   - Čist minimalistički dizajn bez nepotrebnih informacija

2. **SettingsScreen**
   - Lista od 10 feature-a koje možeš uključiti/isključiti
   - Svaki feature je predstavljen kao toggle switch
   - Čim promeniš nešto, automatski se čuva u SharedPreferences
   - Home screen se odmah update-uje nakon promene

3. **StatsScreen** - ima 2 tab-a
   - Stats tab pokazuje statistiku (vreme po mood-u, vreme po lokaciji, progress bar-ovi)
   - Badges tab prikazuje sve badge-eve u grid layout-u, sa statusom da li su otključani i koliko još treba

4. **BreathingScreen**
   - Animirani krug koji se širi i skuplja u ciklusu od 12 sekundi
   - Inhale (0-4s) - krug se širi
   - Hold (4-8s) - krug stoji
   - Exhale (8-12s) - krug se skuplja
   - Ima radial gradient i glow efekat za lepši vizual

**Notification sistem za remindere**

Reminderi rade ovako:
1. Tokom vožnje (stop: false) - aplikacija kreira remindere i čuva ih u SharedPreferences, ali ih NE prikazuje
2. Kad vozač stane (stop: true) - aplikacija prikazuje SVE sačuvane remindere kao Android notifikacije, pa ih briše

Razlog za ovo: Bezbednost. Ne želimo da vozač dobija notifikacije dok vozi jer to ometa pažnju. Reminderi se prikazuju tek kad je bezbedno.

Notifikacije koriste Android NotificationManager, imaju custom ikonicu i title + description.

---

## Tehnologije

**Backend:**
- Python 3.10+
- PyTorch za deep learning
- torchvision za computer vision
- EfficientNet-B0 kao base model
- Flask-SocketIO za WebSocket server
- Pillow za image processing

**Android:**
- Kotlin 1.9
- Jetpack Compose za UI
- Material3 design
- Navigation Compose za routing između ekrana
- Socket.IO Client 2.0.1 za WebSocket konekciju
- Coroutines i Flow za asinhroni kod
- StateFlow za state management
- SharedPreferences za local storage
- NotificationCompat za notifikacije
- Gradle 8.2 build sistem

**Kako sve radi zajedno:**
Vozilo šalje video frame-ove na Flask server → Server radi AI inference → Šalje JSON preko WebSocket-a → Android aplikacija prima i reaguje

---

## Šta čini ovo rešenje posebnim

**1. Dual-task AI model**
Većina modela radi samo jednu stvar (npr. samo mood ili samo scene). Mi koristimo jedan model koji radi oba istovremeno jer dele iste features. To je efikasnije i brže - oko 40% brži inference nego dva odvojena modela.

**2. Kontekstualne akcije**
Ne dajemo iste preporuke za sve situacije. Kombinujemo mood + scene + stop status da bi dobili prave akcije. 
Npr. ako si umoran u gradu → kafu, ali ako si umoran na autoputu → to je opasno, prioritet je rest stop.

**3. Gamifikacija**
Badge sistem podstiče ljude da koriste safety features više. Imaš osećaj postignuća kad otključaš badge, pa nastaviš da koristiš aplikaciju. Psihologija.

**4. Bezbednost na prvom mestu**
Reminderi se NE prikazuju tokom vožnje. Samo kad staneš. Ne želimo da odvlačimo pažnju vozača u pokretu.

**5. Sve je real-time**
WebSocket konekcija + StateFlow znači da sve promene na serveru se odmah vide na telefonu. Nema potrebe za pull-to-refresh ili slično. Latency je ispod 100ms.

---

## Testiranje

Dataset ima 4 foldera sa ukupno oko 2500+ frame-ova. Label-ovali smo range-based strategijom (npr. frame 0-110 su jedna klasa, 111-221 druga, itd).

Distribucija scena:
- City: ~35%
- Highway: ~30%
- Forest: ~20%
- Garage: ~10%
- Ostalo: ~5%

Model performance (očekujemo):
- Mood klasifikacija: oko 75-80% accuracy (teže jer nisu najčistiji label-i)
- Scene klasifikacija: oko 85-90% accuracy (lakše)
- Inference latency: 50-100ms po frame-u

Android aplikacija:
- Build prolazi uspešno
- Testirali na Android Emulator-u (API 34)
- WebSocket reconnect logic radi - testirali smo sa namerno prekidanjem veze više puta
- Notification sistem testiran sa 10+ reminders odjednom

---

## Budući pravci razvoja

Kratkoročno (sledećih par meseci):
- On-device inference - staviti PyTorch Mobile model direktno na telefon da ne treba server. To bi dalo 0ms network latency i radio bi offline.
- Voice commands - "Start breathing exercise", "Find rest stop" itd
- Spotify SDK integracija - stvarna integracija sa Spotify-em umesto samo placeholder-a
- Android Auto support - prikazivanje na dashboard ekranu vozila

Dugoročno (narednih 6-12 meseci):
- Multi-modal model - dodati audio (glas vozača) da se mood može bolje detektovati
- Personalizacija - model bi se prilagođavao navikama pojedinačnog vozača
- Fleet analytics - agregirana statistika za kompanije koje imaju flote vozila
- Emergency detection - automatski poziv hitne ako se detektuje kritično stanje

---

## Kako pokrenuti projekat

**Backend:**
```bash
cd pajton/
pip install torch torchvision flask-socketio pillow
python train.py      # Trenira model (opcionalno)
python inference.py  # Generiše predikcije
```

**Android aplikacija:**
```bash
cd rivianProject/
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Za testiranje, potrebno je pokrenuti Flask server na 192.168.40.152:5000 (ili promeniti IP u kodu). Android aplikacija će se automatski konektovati. Ima i mock test u TestActionsScreen za debug.

---

## Zaključak

Rivian Sense kombinuje AI model za real-time detekciju stanja vozača sa pametnim action engine-om koji predlaže kontekstualno relevantne akcije. Fokus je na bezbednosti (reminderi samo kad staneš), a gamifikacija kroz badge-eve podstiče upotrebu safety features.

Ključne stvari:
- Real-time detekcija preko WebSocket-a
- Pametne akcije koje zavise od mood + scene kombinacije
- Safety-first pristup
- Gamifikacija za bolje engagement
- Dark theme UI sa smooth animacijama

Sistem je spreman za integraciju sa Rivian vozilima - potreban je samo WebSocket endpoint i video stream sa vozila.

---

**Developer:** S-t-r-a-x  
**GitHub:** https://github.com/S-t-r-a-x/Rivian-Sense
