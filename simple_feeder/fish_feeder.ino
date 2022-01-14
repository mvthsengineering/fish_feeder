/*************************************
  Fish Feeder Rates
  There are 50 counts per rotation
  The feeder produces 0.075 grams of food per rotation.
  There are 2.4 seconds per rotation.
*************************************/

#include "RTClib.h"
#include "Wire.h"
#include "Adafruit_LiquidCrystal.h"
#include <EEPROM.h>

// Fish feeder states
#define WAIT      0   // Waiting for feed time
#define SET_TIME  1   // Setting time of day
#define SET_FEED  2   // Setting the feed time
#define FEED      3   // Feeding
#define FEED_RATE 4   // The amount in grams to feed 
#define FAIL      5   // Feeding time exceeded maxium time

// Set EEPROM address for storing feed times and amounts
#define F_HRS_ADR 0   // Store hours for feed time
#define F_MNS_ADR 1   // Store minutes for feed time
#define GMS_ADR   2   // Store amount of feed

// Fish feeder I/O
#define PULSE_PIN   3   // Reads value from encoder wheel
#define B_HRS       A2  // Sets hours and feed(down)
#define B_MNS       A3  // Sets minutes and feed(up)
#define B_CST       A1  // Changes state

#define PRESS LOW     // Buttons all default to HIGH, a press is registered as LOW

#define MAX_GRAMS             50 // Set this maximum to control the fail safe
#define MLS_PER_CNT           8  // Number of milliseconds per count of the encoder
#define GMS_PER_CNT           0.062 // Number of grams dispensed per count of the encoder
#define FAIL_SAFE             (long)MAX_GRAMS/(float)GMS_PER_CNT*MLS_PER_CNT // Total number of milliseconds for maximum number of grams

RTC_DS1307 rtc;
Adafruit_LiquidCrystal lcd(0);

uint8_t state = SET_TIME;
volatile uint16_t count;

// Feed time and feed rate also stored in EEPROM and retrieved after restart
uint8_t feed_hrs;
uint8_t feed_mns;
uint8_t grams_feed;

// Drive motor either on or off
void drive_motor( uint8_t power ) {

  // Drive motor forward
  if (power) {
    digitalWrite(12, HIGH);
    digitalWrite(13, LOW);

    // Turn motor off
  } else {
    digitalWrite(12, LOW);
    digitalWrite(13, LOW);
  }
}

// Counts pulses from the encoder
void pulse_count() {
  count++;
}

// Prints time formated in hours and minutes. Seconds are added if last argument is true
void print_time(uint8_t col, String txt, uint8_t hs, uint8_t ms, uint8_t ss, bool sec) {
  lcd.setCursor(0, col);
  lcd.print(txt);
  lcd.print(hs);
  lcd.print(':');
  if (ms < 10)
    lcd.print('0');
  lcd.print(ms);

  // Add seconds
  if (sec == true) {
    lcd.print(':');
    if (ss < 10)
      lcd.print('0');
    lcd.print(ss);
    lcd.print("     ");
  } else {
    lcd.print("      ");
  }
}

// Clears remaining characters on LCD
void clear_line(uint8_t col) {
  lcd.setCursor(0, col);
  lcd.print("               ");
}

void setup () {
  // Set pins for motor
  pinMode(12, OUTPUT);
  pinMode(13, OUTPUT);

  // Set pins for buttons
  pinMode(B_MNS, INPUT_PULLUP);
  pinMode(B_HRS, INPUT_PULLUP);
  pinMode(B_CST, INPUT_PULLUP);

  Serial.begin(57600);

  // Set up real time clock
  if (! rtc.begin()) {
    Serial.println("Couldn't find RTC");
    Serial.flush();
    abort();
  }

  // Set up LCD
  lcd.begin(16, 2);
  lcd.setCursor(0, 1);
  lcd.setBacklight(HIGH);

  // Turn off motor
  drive_motor(false);

  // Turn on interrupt to capture pulses from encoder
  attachInterrupt(digitalPinToInterrupt(PULSE_PIN), pulse_count, RISING);

  // Retrives the stored feed time and feed amount from the EEPROM after restart
  feed_hrs =    EEPROM.read(F_HRS_ADR);
  feed_mns =    EEPROM.read(F_MNS_ADR);
  grams_feed =  EEPROM.read(GMS_ADR);

}

void loop () {
  // Get time from RTC
  DateTime now = rtc.now();
  
  // Gets an initial time from clock after restart
  static uint8_t hrs = now.hour();
  static uint8_t mns = now.minute();
  static uint8_t scs = now.second();

  static uint32_t timeStart;  // Start of feed time for fail safe check

  switch (state) {
    case SET_TIME:

      // Adjust minutes and hours
      if (digitalRead(B_MNS) == PRESS) {
        if (mns++ >= 59) mns = 0;
      }
      if (digitalRead(B_HRS) == PRESS) {
        if (hrs++ >= 23) hrs = 0;
      }

      // Set time on RTC and note that const values are arbitary
      rtc.adjust(DateTime(2014, 1, 21, hrs, mns, 30));

      // Print stored time
      print_time(0, "Set Time: ",  hrs, mns, scs, false);
      clear_line(1);

      // Change to set feed state
      if (digitalRead(B_CST) == PRESS) {
        state = SET_FEED;
        while (digitalRead(B_CST) == PRESS);
      }
      break;
    case SET_FEED:

      // Adjust feed time, minutes and hours
      if (digitalRead(B_MNS) == PRESS) {
        if (feed_mns++ >= 59) feed_mns = 0;
      }
      if (digitalRead(B_HRS) == PRESS) {
        if (feed_hrs++ >= 23) feed_hrs = 0;
      }

      // Print feed time
      print_time(0, "Feed Time: ", feed_hrs, feed_mns, scs, false);
      clear_line(1);

      // Store feed time in EEPROM and change to feed rate state
      if (digitalRead(B_CST) == PRESS) {
        EEPROM.write(F_HRS_ADR, feed_hrs);
        EEPROM.write(F_MNS_ADR, feed_mns);
        state = FEED_RATE;
        while (digitalRead(B_CST) == PRESS);
      }
      break;
    case WAIT:

      // Get updated seconds, minutes and hours
      scs = now.second();
      mns = now.minute();
      hrs = now.hour();

      // Print existing time and feed time
      print_time(0, "Time: ", hrs, mns, scs, true);
      print_time(1, "Feed: ", feed_hrs, feed_mns, scs, false);

      // Change to SET_TIME state
      if (digitalRead(B_CST) == PRESS) {
        state = SET_TIME;
        while (digitalRead(B_CST) == PRESS);
      }

      // Change to FEED state when hours and minutes match and capture start time
      if ((hrs == feed_hrs) && (mns == feed_mns)) {
        clear_line(1);
        timeStart = millis();
        state = FEED;
      }
      break;
    case FEED:

      // Continue to feed while count is less than set grams feed rate
      if (count < ((float)grams_feed / GMS_PER_CNT)) {
        lcd.setCursor(0, 0);
        lcd.print("Feeding: ");
        lcd.print(count);
        lcd.print("             ");
        drive_motor(true);
        
        // Fail safe check if amount of feed time exceeds FAIL_SAFE time
        if (millis() - timeStart > FAIL_SAFE) {
          drive_motor(false);
          state = FAIL;
        }
      }
      else  {

        // Stop feeding and wait for minute to elapse before returning to WAIT
        lcd.setCursor(0, 0);
        lcd.print("Reset in: ");
        lcd.print(59 - now.second());
        lcd.print("             ");
        drive_motor(false);
      }
      if (now.minute() > feed_mns) {
        count = 0;
        state = WAIT;
      }
      break;
    case FEED_RATE:

      // Increase or decrease feed rate based on user input
      if (digitalRead(B_MNS) == PRESS) {
        if (grams_feed++ >= MAX_GRAMS) grams_feed = MAX_GRAMS;
      }
      if (digitalRead(B_HRS) == PRESS) {
        if (grams_feed-- <= 0) grams_feed = 0;
      }
      lcd.setCursor(0, 0);
      lcd.print("Grams Feed: ");
      lcd.print(grams_feed);
      lcd.print("             ");
      clear_line(1);

      // Change to WAIT state when user is done setting feed rate
      if (digitalRead(B_CST) == PRESS) {
        EEPROM.write(GMS_ADR, grams_feed);
        state = WAIT;
        while (digitalRead(B_CST) == PRESS);
      }
      break;

    // FAIL state when feed time has exceeded FAIL_SAFE time
    case FAIL:
      lcd.setCursor(5, 0);
      lcd.print("ERROR");
      delay(300);
      clear_line(0);
      delay(300);
      break;
  }
}
