#include "Adafruit_HTU21DF.h"
#include <BH1750.h>
#include <Adafruit_BMP085.h>
#include <LiquidCrystal_I2C.h>
#include "SPIFFS.h"
#include <WiFiClientSecure.h>
#include <Wire.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <ezTime.h>
#include <WiFi.h>

//LEDs y Relays
#define LedWIFI     5
#define LedServer   17
#define LedLocal    16
#define LedSensors  4
#define Relay1      18
#define Relay2      19
//Sensores
#define MOISTURE  39 //ADC1_3
#define RAIN      34 //ADC1_6
#define WIND      26

//Configuración de Sensores I2C y Pantalla LCD
Adafruit_HTU21DF htu = Adafruit_HTU21DF();
BH1750 lightMeter;
Adafruit_BMP085 bmp;
LiquidCrystal_I2C lcd(0x27, 20, 4);  

//Variables auxiliares anemometro
int contadorA = 0;
long ultMsg = 0;

//Función de interrupuciones anemometro
void IRAM_ATTR vientointA() {
  contadorA++;
  //Serial.print("Flancos negativos A: ");
  //Serial.println(contadorA);
}

//Credenciales de red Wifi
//const char* ssid = "Aux_IoT";
//const char* password = "12345678";
//const char* ssid = "Red_IoT2";
//const char* password = "3VmzfZJVg7";
//String const ssids[]={"Meg","Red_IoT","Red_IoT2",};
//String const passwords[]={"12345678","RedIoT2020","3VmzfZJVg7"};
String const ssids[]={"Meg","Totalplay-1DA1","Red_IoT2",};
String const passwords[]={"12345678","1DA15A7AewUDnK85","3VmzfZJVg7"};
//String const ssids[]={"Red_IoT","Red_IoT2","MEGACABLE-2.4G-8BA3"};
//String const passwords[]={"RedIoT2020","3VmzfZJVg7","6472EUs79b"};
#define networks_number   3
char ssid[20], password[20];
int conexion_inicial=0;

//Servidor MQTT
const char* mqtt_server = "a3r4kpsuw5bnw7-ats.iot.us-east-2.amazonaws.com";
const int mqtt_port = 8883;

//Configuración de cliente MQTT
WiFiClientSecure espClient;
PubSubClient client(espClient);

//MQTT topics
#define AWS_IOT_PUBLISH_TOPIC "ESP32/pub"
#define AWS_IOT_SUBSCRIBE_TOPIC "ESP32/sub"

//Certificados
String Read_rootca;
String Read_cert;
String Read_privatekey;

//#define BUFFER_LEN  256
//char msg[BUFFER_LEN];

//Configuraciones variables
long T_Muestreo=30000;//30 seg
long T_MuestreoAWS=300000; //5 min
int Riego= 0;
String Modo_riego="Manual";
long T_MinimaHS=90;
File configFile;

//Configuraciones fijas del riego
long triegoman = 1800000; //30 min
long triegoaut = 600000; //10 min

//Variable auxiiar del riego
int regando=0;

//Variables para gestion del tiempo
long Now;
long lastMsgAWS = 0;
long lastMsg = 0;
long tinicioriego = 0;

//Cronometro
int segundos=0, minutos=0, horas=0;
long lastTime = 0;

//Zona horaria del reloj Wi-Fi
Timezone Mexico;

//Mensaje guardado en SD
String arduino;

//Cronometro
void setTime(){
  if(segundos==59){
    segundos=0;
    if(minutos==59){
      minutos=0;
      if(horas==720){
        horas=0;
      }
      else{
        horas++;
      }
    }
    else{
      minutos++;
    }
  }
  else{
    segundos++;
  }
}

//Parpadeos de los LEDs
void parpadea(int pin, int veces){
    int k;
    for(k=0;k<veces;k++){
        digitalWrite(pin, HIGH);     delay(50);
        digitalWrite(pin, LOW);      delay(50);
    }
}

//Conexion a red Wifi
void setup_wifi() {
  int red = 0;
  int intentos;
  String dat;

  delay(10);
  //WiFi.persistent(true);
  WiFi.mode(WIFI_STA);
  long InicioIntento = millis(); 
  long TiempoMaximo = 30000;
  
  while (WiFi.status() != WL_CONNECTED && millis() - InicioIntento < TiempoMaximo) {
    dat = ssids[red];       dat.toCharArray(ssid,dat.length()+1);
    dat = passwords[red];   dat.toCharArray(password,dat.length()+1);

    lcd.clear();
    lcd.setCursor(7, 0);    lcd.print("Wi-Fi:");
    lcd.setCursor(4, 1);    lcd.print("Conectando a ");
    lcd.setCursor(0, 2);    lcd.print(ssid);
    lcd.setCursor(0, 3);
    WiFi.begin(ssid, password);
    
    for(intentos=0; intentos<10; intentos++){
      lcd.print(".");
      parpadea(LedWIFI, 5); 
    }
    red++;
    if(red==networks_number)  red=0;
  }
  //WiFi.setAutoReconnect(true);
  lcd.clear();
  lcd.setCursor(7, 1);    lcd.print("Wi-Fi:");  
  if (WiFi.status() == WL_CONNECTED) {
    //Serial.println("");
    //Serial.println("WiFi conectado");
    //Serial.println("Direccion IP: ");
    //Serial.println(WiFi.localIP());
    lcd.setCursor(2, 2);    lcd.print("Conexion exitosa");
    digitalWrite(LedWIFI, LOW);
    conexion_inicial=1;
  } else {
    //Serial.println("");
    //Serial.println("No se pudo establecer la conexión WiFi");
    lcd.setCursor(3, 2);    lcd.print("Fallo conexion");
    digitalWrite(LedWIFI, HIGH); 
  }
  delay(1000);
}

//Reconexion a red WiFi
void check_wifi() {
  digitalWrite(LedWIFI, HIGH);
  long InicioIntento = millis(); 
  long TiempoMaximo = 3000; 
  if (conexion_inicial==1){
    //Serial.println("Reconectando WiFi...");
    WiFi.disconnect();
    WiFi.reconnect();
    while (WiFi.status() != WL_CONNECTED && millis() - InicioIntento < TiempoMaximo) {
      delay(500);
      //Serial.print(".");
    }
  } else {
    String dat;
    dat = ssids[2];       dat.toCharArray(ssid,dat.length()+1);
    dat = passwords[2];   dat.toCharArray(password,dat.length()+1);
    //Serial.println("Conectando WiFi...");
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED && millis() - InicioIntento < TiempoMaximo) {
      delay(500);
      //Serial.print(".");
    }
  }
  if (WiFi.status() == WL_CONNECTED) {
    //Serial.println("");
    //Serial.println("WiFi conectado");
    //Serial.println("Direccion IP: ");
    //Serial.println(WiFi.localIP());
    conexion_inicial=1;
    digitalWrite(LedWIFI, LOW);
    /*lcd.clear();
    lcd.setCursor(7, 1);
    lcd.print("Wi-Fi:");
    lcd.setCursor(2, 2);
    lcd.print("Conexion exitosa");*/
    reconnect();
    waitForSync();
  //} else {
    //Serial.println("");
    //Serial.println("No se pudo establecer la conexión WiFi");
  }
  delay(1000);
} 

//Conexion  a AWS
void connectToAWSIoT() {
  lcd.clear();
  lcd.setCursor(5, 1);
  lcd.print("MQTT-AWS:");
  lcd.setCursor(5, 2);
  lcd.print("Conectando");
  digitalWrite(LedServer, HIGH);
  if (!SPIFFS.begin(true)) {
    //Serial.println("Se ha producido un error al montar SPIFFS");
    return;
  }
  //**********************
  //Root CA leer archivo.
  File file2 = SPIFFS.open("/AmazonRootCA1.pem", "r");
  if (!file2) {
    //Serial.println("No se pudo abrir el archivo para leerlo");
    return;
  }
  //Serial.println("Root CA File Content:");
  while (file2.available()) {
    Read_rootca = file2.readString();
    //Serial.println(Read_rootca);
  }
  //*****************************
  // Cert leer archivo
  File file4 = SPIFFS.open("/51c151e68b-certificate.pem.crt", "r");
  if (!file4) {
    //Serial.println("No se pudo abrir el archivo para leerlo");
    return;
  }
  //Serial.println("Cert File Content:");
  while (file4.available()) {
    Read_cert = file4.readString();
    //Serial.println(Read_cert);
  }
  //***************************************
  //Privatekey leer archivo
  File file6 = SPIFFS.open("/51c151e68b-private.pem.key", "r");
  if (!file6) {
    //Serial.println("No se pudo abrir el archivo para leerlo");
    return;
  }
  //Serial.println("privateKey contenido:");
  while (file6.available()) {
    Read_privatekey = file6.readString();
    //Serial.println(Read_privatekey);
  }
  
  //=====================================================
  char* pRead_rootca;
  pRead_rootca = (char *)malloc(sizeof(char) * (Read_rootca.length() + 1));
  strcpy(pRead_rootca, Read_rootca.c_str());

  char* pRead_cert;
  pRead_cert = (char *)malloc(sizeof(char) * (Read_cert.length() + 1));
  strcpy(pRead_cert, Read_cert.c_str());

  char* pRead_privatekey;
  pRead_privatekey = (char *)malloc(sizeof(char) * (Read_privatekey.length() + 1));
  strcpy(pRead_privatekey, Read_privatekey.c_str());

  //Serial.println("================================================================================================");
  //Serial.println("Certificados que pasan adjuntan al espClient");
  //Serial.println();
  //Serial.println("Root CA:");
  //Serial.write(pRead_rootca);
  //Serial.println("================================================================================================");
  //Serial.println();
  //Serial.println("Cert:");
  //Serial.write(pRead_cert);
  //Serial.println("================================================================================================");
  //Serial.println();
  //Serial.println("privateKey:");
  //Serial.write(pRead_privatekey);
  //Serial.println("================================================================================================");

  espClient.setCACert(pRead_rootca);
  espClient.setCertificate(pRead_cert);
  espClient.setPrivateKey(pRead_privatekey);

  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);

  String clientId = "ESP32-";
  clientId += String(random(0xffff), HEX);
  client.connect(clientId.c_str());
  //Serial.print("Intentando la conexión MQTT...");
  long InicioIntento = millis(); 
  long TiempoMaximo = 10000;
  lcd.setCursor(0, 3);
  while (!client.connected() && millis() - InicioIntento < TiempoMaximo) {
    lcd.print(".");
    parpadea(LedServer,5);
    //delay(500);
    //Serial.print(".");
  } 
  
  lcd.clear();
  lcd.setCursor(5, 1);
  lcd.print("MQTT-AWS:");
  if (client.connected()) {
    //Serial.println("");
    //Serial.println("Conexion MQTT a AWS exitosa ");
    digitalWrite(LedServer, LOW);
    lcd.setCursor(2, 2);
    lcd.print("Conexion exitosa");
    client.subscribe(AWS_IOT_SUBSCRIBE_TOPIC);
  } else {
    //Serial.println("");
    //Serial.println("No se pudo establecer la conexión MQTT a AWS");
    //Serial.println(client.state());
    lcd.setCursor(3, 2);
    lcd.print("Fallo conexion");
    digitalWrite(LedServer, HIGH);
  }
}

//Callback de mensajes MQTT
void callback(char* topic, byte* payload, unsigned int length) {
  //Serial.print("Mensaje recibido [");
  //Serial.print(topic);
  //Serial.print("]: ");
  //for (int i = 0; i < length; i++) {
  //Serial.println((char)payload[i]);
  //}
  
  StaticJsonDocument<200> doc;
  deserializeJson(doc, payload);
  if (doc.containsKey("Tiempo")) {
    T_MuestreoAWS = doc["Tiempo"];
    T_Muestreo = T_MuestreoAWS/4;
  }
  if (doc.containsKey("Tolerancia")) {
    T_MinimaHS = doc["Tolerancia"];
  }
  if (doc.containsKey("Modo")) {
    Modo_riego = doc["Modo"].as<String>();
  }
  if (doc.containsKey("Estado")) {
    Riego = doc["Estado"];
  }
  
//  Serial.println("Nueva configuración recibida");
//  Serial.print("Tiempo de muestreo: ");
//  Serial.println(T_MuestreoAWS);
//  Serial.print("Estado de riego: ");
//  Serial.println(Riego);
//  Serial.print("Modo de riego: ");
//  Serial.println(Modo_riego);
//  Serial.print("Tolerancia Humedad del suelo: ");
//  Serial.println(T_MinimaHS);

  Guardarconf();
  Subirconf();
}

//Guardado de configuraciones principales
void Guardarconf() {
  configFile = SPIFFS.open("/config.txt", "w");
  if (!configFile) {
    //Serial.println("Error al abrir el archivo de Configuración");
    return;
  }
  configFile.println(T_MuestreoAWS);
  configFile.println(Riego);
  configFile.println(Modo_riego);
  configFile.println(T_MinimaHS);
  configFile.close();
  //Serial.println("Archivo de Configuración actualizado");
  parpadea(LedLocal,1);
}

//Subida de configuraciones principales a AWS
void Subirconf() {
  StaticJsonDocument<512> doc;
  JsonObject body= doc.createNestedObject("body");
  body["Id"] = "2";
  body["F"] = Mexico.dateTime("YmdHis");
  body["R"] = String(Riego); 
  body["M"] = Modo_riego; 
  body["Th"] = String(T_MinimaHS); 
  body["Tm"] = String(T_MuestreoAWS/60000); 
  body["C"] = "1";
  char jsonBuffer[512];
  serializeJson(doc, jsonBuffer);
  //Serial.print("Publicando mensaje: ");
  //Serial.println(jsonBuffer);
  client.publish(AWS_IOT_PUBLISH_TOPIC, jsonBuffer);
  //Serial.println("Nueva configuracion en la nube (confirmacion)");
  parpadea(LedServer,1);
}

//Lectura y asignación de las ultimas configuraciones
void Leerconf() {
  configFile = SPIFFS.open("/config.txt", "r");
  if (!configFile) {
    //Serial.println("Error al abrir el archivo de Configuración");
    return;
  }
  T_MuestreoAWS=configFile.readStringUntil('\n').toFloat();
  T_Muestreo = T_MuestreoAWS/4;
  Riego=configFile.readStringUntil('\n').toInt();
  Modo_riego=configFile.readStringUntil('\n');
  Modo_riego.trim();
  T_MinimaHS=configFile.readStringUntil('\n').toFloat();
  configFile.close();
  
  //Serial.println("Ultima configuración montada");
//  Serial.print("Tiempo de muestreo: ");
//  Serial.println(T_MuestreoAWS);
//  Serial.print("Estado de riego: ");
//  Serial.println(Riego);
//  Serial.print("Modo de riego: ");
//  Serial.println(Modo_riego);
//  Serial.print("Tolerancia Humedad del suelo: ");
//  Serial.println(T_MinimaHS);

  parpadea(LedLocal,1);
}

//Reconexion a broker MQTT
void reconnect() {
  if (WiFi.status() == WL_CONNECTED) {
    digitalWrite(LedWIFI, LOW);
    conexion_inicial=1;
  }
    
  digitalWrite(LedServer, HIGH);
  String clientId = "ESP32-";
  clientId += String(random(0xffff), HEX);
  client.connect(clientId.c_str());
  //Serial.print("Intentando la conexión MQTT...");
  long InicioIntento = millis(); 
  long TiempoMaximo = 3000;
  while (!client.connected() && millis() - InicioIntento < TiempoMaximo) {
    delay(500);
    //Serial.print(".");
  } 
  if (client.connected()) {
    //Serial.println("");
    //Serial.println("Reconexion MQTT a AWS exitosa ");
    digitalWrite(LedServer, LOW);
    /*lcd.clear();
    lcd.setCursor(5, 1);
    lcd.print("MQTT-AWS:");
    lcd.setCursor(2, 2);
    lcd.print("Conexion exitosa");*/
    client.subscribe(AWS_IOT_SUBSCRIBE_TOPIC);
  } else {
    //Serial.println("");
    //Serial.println("No se pudo reestablecer la conexión MQTT a AWS");
    //Serial.println(client.state());
  }
}

void setup() {
  Serial.begin(9600);
  delay(1000);
  
  pinMode(LedWIFI, OUTPUT);
  pinMode(LedServer, OUTPUT);  
  pinMode(LedLocal, OUTPUT);
  pinMode(LedSensors, OUTPUT);
  pinMode(Relay1, OUTPUT);
  pinMode(Relay2, OUTPUT);
  pinMode(WIND,INPUT);
  digitalWrite(LedWIFI, LOW);
  digitalWrite(LedServer, LOW);
  digitalWrite(LedLocal, LOW);
  digitalWrite(LedSensors, LOW);
  digitalWrite(Relay1, LOW);
  digitalWrite(Relay2, LOW);

  lcd.init();   
  lcd.backlight();// turn on LCD backlight 
  lcd.clear(); 
  lcd.setCursor(6, 1);//columna,fila
  lcd.print("Estacion");
  lcd.setCursor(3, 2);
  lcd.print("meteorologica");
  delay(1000);
  
  //Wire.begin();
  parpadea(LedSensors,1);
  if (!htu.begin()) {
    //Serial.println("Checa el circuito. HTU21D no encontrado");
    lcd.clear();
    lcd.setCursor(4, 1);
    lcd.print("Error HTU21D");
    digitalWrite(LedSensors, HIGH);
    //while (1);
    delay(1000);
  }
  parpadea(LedSensors,2);
  if (!lightMeter.begin()) {
    lcd.clear();
    lcd.setCursor(4, 1);
    //Serial.println("Checa el circuito. BH1750 no encontrado");
    lcd.print("Error BH1750");
    digitalWrite(LedSensors, HIGH);
    //while (1);
    delay(1000);
  }
  parpadea(LedSensors,3);
  if (!bmp.begin()) {
    lcd.clear();
    lcd.setCursor(4, 1);
    //Serial.println("Checa el circuito. BMP180 no encontrado");
    lcd.print("Error BMP180");
    digitalWrite(LedSensors, HIGH);
    //while (1);
    delay(1000);
  }

  setup_wifi();
  connectToAWSIoT();
  delay(1000);
  Leerconf();
  
  lcd.clear(); 
  lcd.setCursor(3, 1);
  lcd.print("Sincronizando");
  lcd.setCursor(8, 2);
  lcd.print("hora");
  if (WiFi.status() == WL_CONNECTED) {
    waitForSync();
  }
  //Mexico.setLocation("America/Mexico_City");//Problema con cambio de horario
  Mexico.setLocation("America/Mazatlan");
  lcd.clear(); 
  
  attachInterrupt(WIND, vientointA, FALLING); //FALLING  Los disparadores interrumpen cuando el pin va de HIGH a LOW y RISING  Los disparadores interrumpen cuando el pin va de LOW a HIGH
  ultMsg = millis();
}

void loop() {  
  if (WiFi.status() != WL_CONNECTED){
    check_wifi();
    //digitalWrite(LedWIFI, HIGH);
  //} else{
  //  digitalWrite(LedWIFI, LOW);
  }
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  char msg[30]; 
  Now = millis();
  if (Now - lastTime > 1000){
    lastTime = Now;
    setTime();
    sprintf(msg,"T%3d:%2d:%2d",horas,minutos,segundos);
    if(horas<10)    msg[2]='0';
    if(minutos<10)  msg[5]='0';
    if(segundos<10) msg[8]='0';  
    lcd.setCursor(10, 3);
    lcd.print(msg);
  }

  Now = millis();
  if (Modo_riego == "Manual"){
    if ((Riego==0 || Now - tinicioriego > triegoman) && regando==1){
      regando=0;
      digitalWrite(Relay1, LOW); 
      digitalWrite(Relay2, LOW);
      if (Now - tinicioriego > triegoman){
        Riego=0;
        Subirconf();
      }
    } else if (Riego==1 && regando==0){
          regando=1;
          tinicioriego = Now;
          digitalWrite(Relay1, HIGH);
          digitalWrite(Relay2, HIGH);
    }
  } else if (Modo_riego == "Automatico"){
    float mois = analogRead(MOISTURE);
    float moisp = map(mois,4095.0,1000.0,0,100.0);//map(mois, AirValue, WaterValue, 0, 100)
    if (moisp<T_MinimaHS && regando==0){
      regando=1;
      tinicioriego = Now;
      digitalWrite(Relay1, HIGH); 
      digitalWrite(Relay2, HIGH);
      Riego=1;
      Subirconf();
    }
    else if ((moisp>=100 || Now - tinicioriego > triegoaut) && regando==1 ) {
      regando=0;
      digitalWrite(Relay1, LOW); 
      digitalWrite(Relay2, LOW);
      Riego=0;
      Subirconf();
    }
  }

  Now = millis();
  if (Now - lastMsg > T_Muestreo) {
    lastMsg = Now;
    parpadea(LedSensors,1);
    float t= htu.readTemperature();
    float h= htu.readHumidity();
    parpadea(LedSensors,1);
    float lux = lightMeter.readLightLevel();
    parpadea(LedSensors,1);
    //float tempbmp = bmp.readTemperature();
    //La medida  Pa, al dividirla entre 100 hPa
    //float pres = bmp.readPressure()/100; //presión atmosférica en la ubicación física actual
    float sea = bmp.readSealevelPressure(1780)/100; //presión atmosférica a nivel del mar utilizada en estaciones meteorológicas (Altitud promedio Silao 1780 m)
    //float alt = bmp.readAltitude();
    //Calcula la altitud asumiendo una presión barométrica 'estándar' de 1013,25 milibares = 101325 Pascal
    //puede obtener una medida más precisa de la altitud si conoce la presión actual del nivel del mar, que variará con el clima y demás.
    //float real = bmp.readAltitude(102700);
    parpadea(LedSensors,1);
    float mois = analogRead(MOISTURE);
    float moisp = map(mois,4095.0,1000.0,0,100.0);//map(mois, AirValue, WaterValue, 0, 100)
    parpadea(LedSensors,1);
    float rain = analogRead(RAIN);
    float rainp = map(rain,4095.0,0.0,0,100.0);
  
    //Serial.println(""); 
    //Serial.print("Temperatura HTU21D(°C): "); 
    //Serial.print(t); 
    //Serial.print("\t");
    //Serial.print("Humedad del aire(%): "); 
    //Serial.println(h);

    //Serial.print("Luminosidad(lx): ");
    //Serial.println(lux);
    
    //Serial.print("Temperatura BMP180(°C): ");
    //Serial.print(tempbmp);
    //Serial.print("\t");
    //Serial.print("Presion(hPa): ");
    //Serial.print(pres);
    //Serial.print("\t");
    //Serial.print("Presión al nivel del mar calculada(hPa): ");
    //Serial.print(sea);
    //Serial.print("\t");
    //Serial.print("Altitud(m): ");
    //Serial.print(alt);
    //Serial.print("\t");
    //Serial.print("Altitud real(m): ");
    //Serial.println(real);

    //Serial.print("Humedad del suelo(%): ");
    //Serial.println(moisp);
  
    //Serial.print("Lluvia(%): ");
    //Serial.println(rain);

    parpadea(LedSensors,1);
    long ahora = millis();
    long T = ahora - ultMsg;
    ultMsg = ahora;
    float pps = contadorA/(T/1000.0);
    float v = pps*(1.75/20); //20 pulsos por segundo = 1.75 m/s
    contadorA = 0;
    ////Serial.print("Delta Tiempo(s): ");
    ////Serial.println(T/1000.0);
    ////Serial.print("Pulsos: ");
    ////Serial.println(contadorA);
    //Serial.print("Velocidad del viento(m/s): ");
    //Serial.println(v);
    
    if (isnan(h) || isnan(t)){
      //Serial.println("¡Error al leer el sensor HTU21D!");
      h=-1;
      t=-1;
      digitalWrite(LedSensors, HIGH);
      //return;
    }
    if (lux<0) {
      //Serial.println("¡Error al leer el sensor BH1750!");
      lux=-1;
      digitalWrite(LedSensors, HIGH);
      //return;
    } else if (lux==0){
      lightMeter.begin();
    }
    if (sea>=2000){
      //Serial.println("¡Error al leer el sensor BMP180!");
      sea=-1;
      digitalWrite(LedSensors, HIGH);
      //return;
    }
    
    lcd.clear();
    lcd.setCursor(0, 0);
    sprintf(msg,"Ta:%s%cC   Ha:%3d%c",(String)t,223,(int)h,37);       lcd.print(msg);
    lcd.setCursor(0, 1);
    sprintf(msg,"L:%5d lx   Hs:%3d%c",(int)lux,(int)moisp,37);        lcd.print(msg);
    lcd.setCursor(0, 2);
    sprintf(msg,"Pa:%4dhPa   Ll:%3d%c",(int)sea,(int)rainp,37);       lcd.print(msg);
    lcd.setCursor(0, 3);
    sprintf(msg,"V:%sm/s",(String)v);                                 lcd.print(msg);
//    lcd.setCursor(0, 2);
//    sprintf(msg,"Pa:%4dhPa   %sm/s",(int)sea,(String)v);             lcd.print(msg);
//    lcd.setCursor(0, 3);
//    sprintf(msg,"Rain:%3d%c",(int)rainp,37);                          lcd.print(msg);
  
    Now = millis();
    if (Now - lastMsgAWS > T_MuestreoAWS) {
      lastMsgAWS = Now;
      if (WiFi.status() == WL_CONNECTED) {
        StaticJsonDocument<512> doc;
        JsonObject body= doc.createNestedObject("body");
        body["Id"] = "1";
        body["F"] = Mexico.dateTime("YmdHis");
        body["T"] = String(t); 
        body["HA"] = String(h); 
        body["HS"] = String(moisp); 
        body["L"] = String(lux); 
        body["P"] = String(sea); 
        body["V"] = String(v); 
        body["Ll"] = String(rainp); 
        char jsonBuffer[512];
        serializeJson(doc, jsonBuffer);
        //Serial.print("Publicando mensaje: ");
        //Serial.println(jsonBuffer);
        client.publish(AWS_IOT_PUBLISH_TOPIC, jsonBuffer);
        parpadea(LedServer,1);
      }
      //arduino = "{\"T\":" + String(t) + ",\"HA\":" + String(h) + ",\"HS\":" + String(moisp) + ",\"L\":" + String(lux) + ",\"P\":" + String(sea) + ",\"V\":" + String(v) + ",\"R\":" + String(rainp)+ "}";
      arduino = String(t) + "," + String(h) + "," + String(moisp) + "," + String(lux) + "," + String(sea) + "," + String(v) + "," + String(rainp);
      Serial.println(arduino);
      parpadea(LedLocal,1);
    }
  }
}
