#include <ArduinoBLE.h>
#include "LSM6DS3.h"
#include <Wire.h>

LSM6DS3 imu(I2C_MODE, 0x6A);

// BLE service and characteristic UUIDs
BLEService imuService("12345678-1234-5678-1234-56789ABC0001");

BLECharacteristic imuChar(
  "12345678-1234-5678-1234-56789ABC0002",
  BLERead | BLENotify,
  12
);

// CCCD descriptor – MÅSTE finnas
BLEDescriptor imuCCC("2902", NULL, 0);

const int ledPin = LED_BUILTIN;

const uint32_t PERIOD_MS = 20;   // 50 Hz är stabilt, 25 Hz blir 40 ms men 20 är bättre
uint32_t lastSend = 0;

void setup() {
  Serial.begin(115200);
  Wire.begin();
  Wire.setClock(400000);

  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, HIGH);

  if (imu.begin() != 0) {
    Serial.println("IMU init failed");
  }

  if (!BLE.begin()) {
    Serial.println("BLE init failed");
    while (1);
  }

  BLE.setLocalName("XIAO_IMU");
  BLE.setAdvertisedService(imuService);

  // Lägg till descriptoren (superviktigt)
  imuChar.addDescriptor(imuCCC);

  imuService.addCharacteristic(imuChar);
  BLE.addService(imuService);

  BLE.advertise();

  Serial.println("READY");
}

void loop() {
  BLE.poll();

  BLEDevice central = BLE.central();
  bool connected = central && central.connected();
  digitalWrite(ledPin, connected ? LOW : HIGH);

  if (!connected) return;

  uint32_t now = millis();
  if (now - lastSend < PERIOD_MS) return;
  lastSend = now;

  float ax = imu.readFloatAccelX();
  float ay = imu.readFloatAccelY();
  float az = imu.readFloatAccelZ();
  float gx = imu.readFloatGyroX();
  float gy = imu.readFloatGyroY();
  float gz = imu.readFloatGyroZ();

  int16_t ax_mg = ax * 1000;
  int16_t ay_mg = ay * 1000;
  int16_t az_mg = az * 1000;
  int16_t gx_cdps = gx * 100;
  int16_t gy_cdps = gy * 100;
  int16_t gz_cdps = gz * 100;

  uint8_t pkt[12];
  memcpy(pkt, &ax_mg, 2);
  memcpy(pkt + 2, &ay_mg, 2);
  memcpy(pkt + 4, &az_mg, 2);
  memcpy(pkt + 6, &gx_cdps, 2);
  memcpy(pkt + 8, &gy_cdps, 2);
  memcpy(pkt + 10, &gz_cdps, 2);

  imuChar.writeValue(pkt, sizeof(pkt));

  Serial.println("Sent packet");
}