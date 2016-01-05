#include <string.h>
/*Base CODE obtained from Rover's manual */
int E1 = 6; //M1 Speed Control
int E2 = 5; //M2 Speed Control
int M1 = 8; //M1 Direction Control
int M2 = 7; //M2 Direction Control
String inputString = "";// a string to hold incoming data
int leftspeed = 0; //255 is maximum speed 
int rightspeed = 0;
char val='0';
boolean stringComplete = false;  // whether the string is complete

void setup(void){
 int i;
 for(i=5;i<=8;i++)
   pinMode(i, OUTPUT);
 Serial.begin(115200);
}
void loop(void){
// while (Serial.available() < 1) {} // Wait until a character is received
// char val = Serial.read();
// Serial.println(val);
  if(stringComplete){
    Serial.println(inputString);
    if(inputString.startsWith("*")){
      loadVals();
       switch(val){ // Perform an action depending on the command
         case 'w'://Move Forward
           forward(rightspeed,leftspeed);
           break;
         case 's'://Move Backwards
           reverse(rightspeed,leftspeed);
           break;
         case 'a'://Turn Left
            left(rightspeed,leftspeed);
           break;
         case 'd'://Turn Right
            right(rightspeed,leftspeed);
           break;
         default:
           stop();
         break;
       }
    }
    inputString="";
    stringComplete=false;  
  }
}
void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read(); 
    // add it to the inputString:
    inputString += inChar;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
    } 
  }
}
void loadVals(){
  int token=0;
  inputString = inputString.substring(1, inputString.length());
  token=inputString.indexOf('/');
  val=inputString.charAt(0);//inputString.substring(0,token);
  inputString=inputString.substring(token+1,inputString.length());
  token=inputString.indexOf('/');
  leftspeed=(inputString.substring(0,token)).toInt();
  inputString=inputString.substring(token+1,inputString.length());
  rightspeed=(inputString.substring(0,inputString.length())).toInt();
  
  Serial.println("----------------------------------------");
//        Serial.println("val = "+val);
//        Serial.println("leftspeed = "+leftspeed);
//        Serial.println("rightspeed = "+rightspeed);
}
void stop(void) //Stop
{
 digitalWrite(E1,LOW);
 digitalWrite(E2,LOW);
}
void reverse(int a,int b)
{
 analogWrite (E1,a);
 digitalWrite(M1,LOW);
 analogWrite (E2,b);
 digitalWrite(M2,LOW);
}
void  forward(int a,int b)
{
 analogWrite (E1,a);
 digitalWrite(M1,HIGH);
 analogWrite (E2,b);
 digitalWrite(M2,HIGH);
}
void left(int a,int b)
{
 analogWrite (E1,b);
 digitalWrite(M1,LOW);
 analogWrite (E2,a);
 digitalWrite(M2,HIGH);
}
void right(int a,int b)
{
 analogWrite (E1,b);
 digitalWrite(M1,HIGH);
 analogWrite (E2,a);
 digitalWrite(M2,LOW);
}
