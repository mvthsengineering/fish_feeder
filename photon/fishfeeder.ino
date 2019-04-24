#include <Particle.h>

SYSTEM_MODE(SEMI_AUTOMATIC);

#define motor1 D6
#define motor2 D5
#define encoder D1
#define encoder2 D2
#define OFF 0
#define ON 1
#define STUCK 2
#define CHECK 3
#define FORWARD 4
#define REVERSE 5
#define MOTOR_OFF 6
volatile unsigned int count = 0;
volatile unsigned int direction;
Timer t(1000, print);

int hour;
int minute;
int rotations;
long now;
long last;
int state = CHECK;
int stuck_timer;
uint8_t retry_count = 0;
unsigned long old_time = millis();

void setup() {
  pinMode(motor1, OUTPUT);
  pinMode(motor2, OUTPUT);
  pinMode(encoder, INPUT_PULLUP);
  pinMode(encoder2, INPUT_PULLUP);
  attachInterrupt(encoder, pulse, RISING);
  Time.zone(-5);
  rotations = 5;
  t.start();
  hour = 14;
  minute = 00;
  WiFi.on();
}

void loop() {
  Particle.function("fish_feeder", feeder);

  int currentHour = Time.hour();
  int currentMinute = Time.minute();
  int ping = Time.second();

  if (millis() - old_time >= 2000) {
    if (retry_count < 10) {
      if (!WiFi.ready()) {
        WiFi.connect();
        retry_count++;
      } else if (!Particle.connected()) {
        Particle.connect();
        retry_count++;
      }
    } else {
      retry_count = 0;
    }
    old_time = millis();
  }

  switch (state) {
    case CHECK:
      if ((currentHour == hour) && (currentMinute == minute)) {
          feed(FORWARD, rotations);
          //drive(f)
          //state = FEEDING
          Particle.publish("log", "Daily feed @" + String(hour) + ":" + String(minute) + " for " + String(rotations) + " rotations.");
      }
      break;
    //case FEEDING
        //if (count > rotations)
        //state = OFF
    case OFF:
      count = 0;
      if (currentMinute != minute) {
        state = CHECK;
      }
      break;
    case ON:
      minute = currentMinute;
      feed(FORWARD, rotations);
      break;
    case STUCK:
      Particle.publish("stuck", "", PRIVATE);
      Particle.publish("log", "In reverse for 5 seconds (Feeder got stuck!)");
      feed(REVERSE, 600 * 5);
      break;
  }

  if (ping == 00) {
    Particle.publish("ping", "le ping per minute");
    delay(1000);
  }
}

int feeder(String command) {
  if (strstr(command, "feed=")) {
    int equal = command.indexOf("=") + 1;
    int seconds = command.remove(0, equal).toInt();
    rotations = seconds;
    Particle.publish("log", "Fed for " + String(seconds) + " rotations.");
    state = ON;
    return 1;
  }
  if (strstr(command, "daily=")) {
    int equal = command.indexOf("=") + 1;
    String vars = command.remove(0, equal);
    rotations = getValue(vars, ',', 0).toInt();
    hour = getValue(vars, ',', 1).toInt();
    minute = getValue(vars, ',', 2).toInt();
    Particle.publish("log", "Feeding daily at " + String(hour) + ":" + String(minute) + " for " + String(rotations) + " rotations.");
    return 2;
  }
  if (strstr(command, "reverse=")) {
    int equal = command.indexOf("=") + 1;
    int seconds = command.remove(0, equal).toInt();
    feed(REVERSE, seconds);
    return 1;
  }
}

void feed(int motor, int rotations) {
  if (motor == FORWARD) {
    digitalWrite(motor1, HIGH);
    digitalWrite(motor2, LOW);
    while (count < rotations);
    digitalWrite(motor1, LOW);
    digitalWrite(motor2, LOW);
    state = OFF;
  }
}

void print(){
    Particle.publish("count", String(count));
}

void pulse()
{
  count++;
  if (digitalRead(encoder2) == LOW) {
    direction = 1;
  } else {
    direction = 2;
  }
}

void print_every_second()
{
  Particle.publish("Count", String(stuck_timer));
}

String getValue(String data, char separator, int index) {
  int found = 0;
  int strIndex[] = {0, -1};
  int maxIndex = data.length() - 1;

  for (int i = 0; i <= maxIndex && found <= index; i++) {
    if (data.charAt(i) == separator || i == maxIndex) {
      found++;
      strIndex[0] = strIndex[1] + 1;
      strIndex[1] = (i == maxIndex) ? i + 1 : i;
    }
  }

  return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
}
