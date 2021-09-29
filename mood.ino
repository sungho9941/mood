//라이브러리 선언
#include <SoftwareSerial.h>
#include <Adafruit_NeoPixel.h>
//네오픽셀 led개수 설정
#define count 4
// 블루투스 모듈 11,12번핀으로 사용
// 4 LED 네오픽셀 D13번핀으로 제어
SoftwareSerial BT_Serial (11, 12);
Adafruit_NeoPixel rgbneo = Adafruit_NeoPixel(count, 13, NEO_GRB + NEO_KHZ800);
String RGBString;
int red,green,blue;
void setup() {
 BT_Serial.begin(9600);
 rgbneo.begin();
 rgbneo.show();
}
void loop() 
 if(BT_Serial.available()) 
 {
 RGBString = BT_Serial.readStringUntil(‘\n’);
 //Serial.println(RGBString);
 //블루투스 연결이 해제되어도 LED출력하기 위한 코드 
 while(RGBString==”OK+LOST”)
 { 
 //color_print함수 출력
 color_print(count);
 
 if(BT_Serial.available()) 
 {
 RGBString = BT_Serial.readStringUntil(‘\n’);
 
 if(RGBString==”OK+CONN”)
 {
 break;
 }
 }
 }
 // 블루투스 통신으로 온 데이터값을 RGB값으로 나눠주는 코드
 red = RGBString.substring(0,3).toInt();
 green = RGBString.substring(3,6).toInt();
 blue = RGBString.substring(6,9).toInt();
 //color_print함수 출력
 color_print(count);
 }
}
// RGB값으로 네오픽셀 출력하는 함수
int color_print(int neopixel_count)
{
 for ( int i = 0; i < neopixel_count; i++ )  {
 rgbneo.setPixelColor(i, red, green, blue);
 }
 rgbneo.show();
}
