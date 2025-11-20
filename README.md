## Programozás 3 beadandó feladat

A projekt egy feladat menedzser applikációt készít el - hasonló a Trello, Monday. A UI JavaFX keretrendszer segítségével készült. Tesztelve Java SDK 25.0.1-en.

A következő funkciókat tartalmazza:
- Bejelentkezés
- Regisztráció
- Feladatok felvétele, módosítása
- Jogosultság kezelés

-----

Szükséges:
- Java 14.x.x^
- GSON (ezt letölti a build-jar.bat fájl automatikusan a projektkönyvtárba)
- JavaFX Framework SDK (kompatibilis a telepített Java verzióval)

-----

Az app telepítéséhez, indításához készültek .bat segéd scriptek. 

Az applikáció indítása:
- ```build-jar.bat``` futtatása
- ```java --module-path "%PATH_TO_JAVA_FX_SDK_LIB%" --add-modules javafx.controls,javafx.fxml -jar dist\TaskManager.jar```

------


Neptun kód: NX5JSL