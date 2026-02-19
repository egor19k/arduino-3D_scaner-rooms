
#include <Servo.h>
#include <HCSR04.h>

Servo ser1;
Servo ser2;

int pos = 0;
int pos2 = 0;

byte triggerPin = 12;
byte echoCount = 2;
byte* echoPins = new byte[echoCount]{ 12, 13 };

void setup() {
  ser1.attach(5);
  ser2.attach(6);
  HCSR04.begin(triggerPin, echoPins, echoCount);
  Serial.begin(115200);
}

void loop() {
  for (pos = 0; pos <= 180; pos += 5) {
  ser1.write(pos);
    for (pos2 = 0; pos2 <= 180; pos2 += 5) {
      ser2.write(pos2);
      double* dist = HCSR04.measureDistanceCm();
      if dist[0] != -1 
      {
        dist[0] = round(dist[0]);
        Serial.print(pos);
        Serial.print(";");
        Serial.print(pos2);
        Serial.print(";");
        Serial.println(dist[0]);
      }  
      delay(50);
   }
  }
}
