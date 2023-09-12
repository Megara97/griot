#include <virtuabotixRTC.h> 
#include <SD.h>
#include <SoftwareSerial.h> 
#include <ArduinoJson.h>

//LEDs
#define LedFallo 5
#define LedEscritura 4

//Configuración del Reloj
// CLK -> 6, DAT -> 7, RST -> 8 
// SCLK -> 6, I/O -> 7, CE -> 8 
virtuabotixRTC myRTC(6, 7, 8);

//Configuración de Puerto Serial (recibe información)
SoftwareSerial mySerial(2, 3); 

//Archivo en SD
File myFile;

//Declaración de variables
float t ,h, moisp, lux, pres, v, rainp;
String dato,Fecha;

//Parpadeos de los LEDs
void parpadea(int pin, int veces){
    int k;
    for(k=0;k<veces;k++){
        digitalWrite(pin, HIGH);     delay(50);
        digitalWrite(pin, LOW);      delay(50);
    }
}

void setup() {
  Serial.begin(9600); 
  mySerial.begin(9600);

  pinMode(LedEscritura, OUTPUT);
  pinMode(LedFallo, OUTPUT); 
  digitalWrite(LedEscritura, LOW);
  digitalWrite(LedFallo, LOW);

  // Para ajustar la fecha y hora, debemos utilizar el siguiente formato:
  // segundos, minutos, horas, dia de la semana, numero de día, mes y año
  //myRTC.setDS1302Time(00, 17, 02, 06, 11, 8, 2023); // SS, MM, HH, DW, DD, MM, YYYY
  
  Serial.print("Iniciando SD ...");
  // CS en D10
  if (!SD.begin(10)) {
    Serial.println("No se pudo inicializar");
    digitalWrite(LedFallo, HIGH);
    return;
  }
  Serial.println("Inicializacion exitosa");
  parpadea(LedEscritura,2);
  
  if(!SD.exists("datos.csv"))  {
    myFile = SD.open("datos.csv", FILE_WRITE);
    if (myFile) {
      Serial.println("Archivo nuevo. Escribiendo encabezado");
      myFile.println("Id,Temperatura,Humedad del aire,Humedad del suelo,Luminosidad,Presion atmosferica,Velocidad del viento,Lluvia");
      myFile.close();
      parpadea(LedEscritura,2);
    } else {
      Serial.println("Error creando el archivo");
      digitalWrite(LedFallo, HIGH);
    }
    return;
  }
  Serial.println("Ya existe el archivo");
  parpadea(LedEscritura,2);
}

void loop() {
  if (mySerial.available()>0) {
    Serial.println("Mensaje recibido:");
    dato= mySerial.readString();
    parpadea(LedEscritura,1);
    Serial.print(dato);
    
    myRTC.updateTime();
    
    Fecha=String(myRTC.year);
    if ((myRTC.month)<10){Fecha=Fecha+"0"+String(myRTC.month);}
    else {Fecha=Fecha+String(myRTC.month);}
    if ((myRTC.dayofmonth)<10){Fecha=Fecha+"0"+String(myRTC.dayofmonth);}
    else {Fecha=Fecha+String(myRTC.dayofmonth);}
    if ((myRTC.hours)<10){Fecha=Fecha+"0"+String(myRTC.hours);}
    else {Fecha=Fecha+String(myRTC.hours);}
    if ((myRTC.minutes)<10){Fecha=Fecha+"0"+String(myRTC.minutes);}
    else {Fecha=Fecha+String(myRTC.minutes);}
    if ((myRTC.seconds)<10){Fecha=Fecha+"0"+String(myRTC.seconds);}
    else {Fecha=Fecha+String(myRTC.seconds);}
    Serial.print(Fecha);
    
    myFile = SD.open("datos.csv", FILE_WRITE);
    if (myFile) { 
      Serial.print("Escribiendo SD ");
      myFile.print(Fecha);
      myFile.print(",");
      myFile.print(dato);
      myFile.close(); 
      parpadea(LedEscritura,2);
    } else {
      Serial.println("Error al abrir el archivo");
      digitalWrite(LedFallo, HIGH);
    }  
  }
}
