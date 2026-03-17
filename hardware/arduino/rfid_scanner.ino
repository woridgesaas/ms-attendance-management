#include <SPI.h>
#include <MFRC522.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <WiFiClient.h>

// ─── Pins RFID ───────────────────────────────────────
#define SS_PIN  D2
#define RST_PIN D4
MFRC522 mfrc522(SS_PIN, RST_PIN);

// ─── Configuration WiFi ──────────────────────────────
const char* WIFI_SSID     = "Pixel";
const char* WIFI_PASSWORD = "mdp";

// ─── Configuration Backend via Kong ──────────────────
const char* BACKEND_IP   = "172.17.90.53";
const int   BACKEND_PORT = 8000;             // port Kong
const char* DEVICE_ID    = "ESP-ENTREE-01";

// ─── Anti-double-scan ────────────────────────────────
unsigned long lastScanTime = 0;
const int MIN_DELAY_MS = 3000;

// ─────────────────────────────────────────────────────
void setup() {
  Serial.begin(115200);
  SPI.begin();
  mfrc522.PCD_Init();

  Serial.println("\nConnexion WiFi...");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\n✅ WiFi connecté !");
  Serial.print("IP ESP8266 : ");
  Serial.println(WiFi.localIP());
  Serial.println("Approche une carte...");
}

// ─────────────────────────────────────────────────────
void loop() {

  if (millis() - lastScanTime < MIN_DELAY_MS) {
    return;
  }

  if (!mfrc522.PICC_IsNewCardPresent()) return;
  if (!mfrc522.PICC_ReadCardSerial())   return;

  // Lire l'UID
  String uid = "";
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    if (mfrc522.uid.uidByte[i] < 0x10) uid += "0";
    uid += String(mfrc522.uid.uidByte[i], HEX);
    if (i < mfrc522.uid.size - 1) uid += ":";
  }
  uid.toUpperCase();

  Serial.println("─────────────────────────");
  Serial.print("UID détecté : ");
  Serial.println(uid);

  sendScanToBackend(uid);

  lastScanTime = millis();

  mfrc522.PICC_HaltA();
  mfrc522.PCD_StopCrypto1();
}

// ─────────────────────────────────────────────────────
void sendScanToBackend(String uid) {

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("❌ WiFi déconnecté !");
    return;
  }

  WiFiClient client;
  HTTPClient http;

  // gateway
  String url = "http://" + String(BACKEND_IP) + ":"
             + String(BACKEND_PORT)
             + "/api/attendance/rfid/scan";

  Serial.print("Envoi vers : ");
  Serial.println(url);

  http.begin(client, url);
  http.addHeader("Content-Type", "application/json");
http.addHeader("X-Subdomain", "dev-local");
  //  Header Host pour Kong subdomain middleware
  // dev-local = tenant de développement

  String body = "{\"uid\":\"" + uid + "\","
              + "\"deviceId\":\"" + String(DEVICE_ID) + "\"}";

  Serial.print("Body : ");
  Serial.println(body);

  int httpCode = http.POST(body);

  if (httpCode > 0) {
    String response = http.getString();
    Serial.print("Status : ");
    Serial.println(httpCode);
    Serial.print("Réponse : ");
    Serial.println(response);
    handleResponse(response);
  } else {
    Serial.print("❌ Erreur HTTP : ");
    Serial.println(http.errorToString(httpCode));
  }

  http.end();
}

// ─────────────────────────────────────────────────────
void handleResponse(String response) {

  if (response.indexOf("CHECK_IN") > 0) {
    Serial.println("✅ CHECK_IN enregistré !");
  } else if (response.indexOf("CHECK_OUT") > 0) {
    Serial.println("✅ CHECK_OUT enregistré !");
  } else if (response.indexOf("ENROLLED") > 0) {
    Serial.println("✅ Carte enrollée !");
  } else if (response.indexOf("429") > 0) {
    Serial.println("⚠️ Scan trop rapide !");
  } else {
    Serial.println("❌ Carte inconnue !");
  }
}
