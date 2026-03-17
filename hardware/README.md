# Hardware — Système de Pointage RFID

## Architecture
```
ESP8266 NodeMCU
    │
    │ SPI
    ↓
RC522 RFID Reader
    │
    │ WiFi HTTP
    ↓
Kong Gateway :8000
    │
    ↓
ms-attendance :8082
```

## Bibliothèques Arduino requises

| Bibliothèque      | Version | Source                        |
|-------------------|---------|-------------------------------|
| MFRC522           | 1.4.11  | Arduino IDE Library Manager   |
| ESP8266WiFi       | incluse | Board ESP8266 package         |
| ESP8266HTTPClient | incluse | Board ESP8266 package         |

## Installation Arduino IDE

### 1. Ajouter le support ESP8266
```
File → Preferences → Additional Board Manager URLs :
https://arduino.esp8266.com/stable/package_esp8266com_index.json
```

### 2. Installer le board
```
Tools → Board → Board Manager → chercher "esp8266" → Install
```

### 3. Installer MFRC522
```
Tools → Manage Libraries → chercher "MFRC522" → Install
```

### 4. Configuration upload
```
Tools → Board → NodeMCU 1.0 (ESP-12E Module)
Tools → Port  → ton port COM
Tools → Speed → 115200
```

## Configuration

Ouvrir `arduino/rfid_scanner.ino` et modifier :
```cpp
const char* WIFI_SSID     = "TON_WIFI";
const char* WIFI_PASSWORD = "TON_MOT_DE_PASSE";
const char* BACKEND_IP    = "IP_DE_TON_PC";
```

## Test

1. Upload le code dans Arduino IDE
2. Ouvrir Serial Monitor à **115200 baud**
3. Vérifier :
    - `[OK] WiFi connecte` ✅
    - IP ESP8266 affichée ✅
    - `Approche une carte...` ✅
4. Approcher une carte → UID affiché ✅
5. Vérifier dans Postman ou Angular ✅

## Documentation

- Schéma de câblage : [docs/CABLAGE.md](docs/CABLAGE.md)