import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    //region Funktionen:
    /*
    - Simulation des Verkehrsflusses
    - Pausieren/Fortsetzen der Simulation durch Drücken der Enter-Taste
    - Festlegen der maximalen Rundenzahl (alternativ auch unendlich Runden)
    - Anzahl der Autos festlegen
    - Darstellung der Autos als Emojis oder A[Fahrzeugnummer]([Geschwindigkeit])
    - Größe und Charakter der Lücke zwischen den Autos festlegen
    - Höchstgeschwindigkeit, Beschleunigungsfaktor, Störfaktor und Stör-Bremsfaktor anpassen
    - Verzögerung zwischen den Runden anpassen
     */
    //endregion

    //region Simulation Variables
    static int carAmount = 20;                                      //Anzahl der Fahrzeuge
    static boolean useCarEmojis = true;                             //Verwendung von Emojis (Zeigt nicht mehr die Geschwindigkeit an)

    static int defaultGapSize = 5;                                  //Größe der Lücke zwischen den Autos zu Beginn
    static char gapCharacter = '.';

    static int rounds = 45;                                         //Anzahl der Runden
    static boolean limitRounds = false;                              //True = Rundenzahl begrenzen
    static int roundTimer = 0;                                      //Aktuelle Runde
    static float delayAmount = 0.25f;                                //Verzögerung zwischen den Durchgängen in Sekunden

    static volatile boolean isPaused = false;                       //Zum Pausieren der Simulation durch die Enter Taste
                                                                    //volatile verhindert, dass durch die Verwendung in einem neuen Thread der Wert "veraltet" ist
                                                                    //und nicht korrekt ausgelesen wird. Stattdessen wird der Wert direkt aktualisiert
                                                                    //und in der Verzweigung if (!isPaused) korrekt ausgelesen
    //endregion

    //region Car Variables
    static int maxSpeed = 5;                                        //Maximale Geschwindigkeit
    static int _carAcceleration = 1;                                //Beschleunigung pro Durchgang
    static float maxDisturbanceFactor = 0.25f;                      //Maximaler Störfaktor
    static int disturbanceBreakValue = 3;                           //Stärke der durch den Störfaktor ausgelösten Bremsung
    //endregion

    public static Random random = new Random();                     //Für zufälligen Störfaktor
    public static List<Auto> street = new ArrayList<>();        //Liste, die die Straße repräsentiert (Fahrzeuge und leere Felder)

    public static void main(String[] args) {
        //region Input Thread
        Thread inputThread = new Thread(new Runnable() {            //Erzeugt einen Thread, der parallel zum eigentlichen Programm läuft
            @Override                                               //Überschreibt die run()-Methode aus dem Runnable-Interface
                                                                    //Notwendig, um eigene Logik in dieser Methode auszuführen
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = scanner.nextLine();              //Warten auf Input-Taste
                    if (input.isEmpty()) {                          //Wenn Enter gedrückt wird
                        isPaused = !isPaused;                       //Pausiert oder setzt fort
                        System.out.println(isPaused ? "Simulation pausiert" : "Simulation fortgesetzt"); //Debug:
                    }
                }
            }
        });
        //endregion

        inputThread.start();  //Thread starten

        //region Straße füllen
        for(int i = 0; i < carAmount; i++){
            if(useCarEmojis){
                street.add(new Auto(/*carName:*/getCarEmoji(), 3, random.nextFloat(0, maxDisturbanceFactor))); //neues Auto Objekt instanziieren mit Namen [zufälliges Auto Emoji], Anfangsgeschwindigkeit und zufälligem Störfaktor
            }
            else{
                street.add(new Auto("A" + (i +1), 3, random.nextFloat(0, maxDisturbanceFactor))); //neues Auto Objekt instanziieren mit Namen (A + Index), Anfangsgeschwindigkeit und zufälligem Störfaktor
            }
            for(int j = 0; j < defaultGapSize; j++){                //Nach jedem Auto leere Felder erzeugen
                street.add(null);
            }
        }
        ausgabe();                                                  //Ursprüngliche Kombination ausgeben
        roundTimer++;
        delay();
        //endregion

        while(true){
            if(!isPaused){
                if(limitRounds && roundTimer >= rounds + 1){
                    System.exit(0);                          //Programm bei erreichter Rundenzahl beenden
                }
                else{                                               //Neuer Durchlauf
                    //region NaSCH Regeln
                    for(Auto currentCar: street){
                        if(currentCar != null){                     //NaSCH-Regeln für jedes Auto aufrufen
                            accelerate(currentCar);
                            brake(currentCar);
                            timeWaste(currentCar);
                        }
                    }
                    drive();                                        //Regeln anwenden
                    //endregion

                    ausgabe();                                      //Neue Liste ausgeben
                    roundTimer++;

                    delay();
                }
            }
        }
    }

    //region Delay zwischen den Runden
    public static void delay(){
        try {
            Thread.sleep((long)(delayAmount * 1000L));              //delayAmount wird in Millisekunden umgewandelt, und für Thread.sleep() als long umgeschrieben (Thread.sleep will ein long (64-Bit) anstatt einem int (32-Bit))
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //NaSCH Regel-Methoden
    public static void accelerate(Auto car){
        if(car.getSpeed() < maxSpeed){                              //Wenn Auto noch nicht die Maximalgeschwindigkeit hat
            car.accelerate(_carAcceleration);                       //Auto um 1 beschleunigen
        }
    }

    public static void brake(Auto car){
        //region Method Variables
        int gapSize;
        //endregion

        gapSize = getGapSize(car);                                  //Lückengröße bestimmen

        //region Auto bremsen
        if(car.getSpeed() > gapSize){                               //Wenn das Auto dem nächsten Auffahren würde
            car.brake(gapSize);                                     //Fahrzeug abbremsen
        }
        //endregion
    }

    public static int getGapSize(Auto car){
        int gapSize = 0;
        //region Lückengröße bestimmen
            for(int i = 1; i <= maxSpeed; i++){                     //maxSpeed * prüfen, da maxSpeed maximale Anzahl an Feldern pro Schritt ist
            int targetIndex = street.indexOf(car) + i;              //Zu prüfendes Feld = Aktuelle Position + i

            if(targetIndex < street.size()){                        //Wenn das Feld vor dem Auto liegt
                if(street.get(targetIndex) == null){                //Kein Auto auf Feld
                    gapSize += 1;
                }
                else{
                    break;                                          //Auto auf Feld
                }
            }
            else{                                                   //Wenn das Feld hinter dem Auto (wieder am Anfang der Straße) liegt
                int newTargetIndex = targetIndex - street.size();   //Am Anfang der Straße anfangen
                if(street.get(newTargetIndex) == null){             //Kein Auto auf Feld
                    gapSize += 1;
                }
                else{
                    break;                                          //Auto auf Feld
                }
            }

        }
        return gapSize;                                             //Größe der Lücke zurückgeben
        //endregion
    }

    public static void timeWaste(Auto car){
        if (Math.random() < car.getTimeWasteFactor()) {             //Ist die Wahrscheinlichkeit erfüllt? (Math.random gibt zufällige Zahl zwischen 0 und 1 aus)
            int newSpeed = Math.max(0, car.getSpeed() - disturbanceBreakValue); //Neuer Wert ist car.getSpeed() - disturbanceBreakValue, falls dieser negativ ist, wird der Wert genau 0
            car.setSpeed(newSpeed);                                 //Geschwindigkeit setzen
        }
    }

    public static void drive() {
        List<Auto> originalStreet = new ArrayList<>(street);        //Verhindert, dass sich Autos beim Verschieben beeinflussen

        for (int i = 0; i < originalStreet.size(); i++) {
            Auto car = originalStreet.get(i);                       //Kopie von Auto Objekt instanziieren

            if (car != null && street.get(i) == car) {              //Verhindert, dass ein Auto in ein belegtes
                int targetIndex = (i + car.getSpeed()) % street.size();  //Neuer Index für das Auto (Modulo falls am Ende der Straße)

                if (street.get(targetIndex) == null) {
                    street.set(targetIndex, car);                   //Fahrzeug bewegen
                    street.set(i, null);                            //Alten Slot leeren
                } else {
                    car.brake(0);                        //Stoppen, wenn Ziel blockiert ist (Zur Sicherheit, wird eigentlich in brake() verhindert)
                }
            }
        }
    }
    //endregion

    //region Ausgabe und Emojis
    public static void ausgabe(){
        for(Auto auto: street){                                     //Alle Autos ausgeben
            if(auto != null){
                if(useCarEmojis){
                    System.out.print(auto.getName());
                }
                else{
                    System.out.print(auto.getName() + '(' + auto.getSpeed() + ')'); //Auto mit Autonamen und aktueller Geschwindigkeit
                }
            }
            else{
                System.out.print(gapCharacter);                     //Leere Felder als Punkte darstellen
            }
        }
        System.out.println("     Runde: " + roundTimer);            //Am Ende noch die aktuelle Runde anzeigen
    }

    public static String getCarEmoji() {
        String[] emojis = {"🚗", "🚙", "🚕", "🚌", "🚛", "🛻"};    //String Array für alle Emojis
        return emojis[random.nextInt(emojis.length)];               //Zufälligen Emoji zurückgeben
    }
    //endregion
}