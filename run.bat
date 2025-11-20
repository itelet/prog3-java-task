@echo off
set JAVAFX_PATH=C:\javafx-sdk-25.0.1\lib
set GSON_JAR=gson-2.10.1.jar

echo Checking for Gson library...
if not exist "%GSON_JAR%" (
    echo Gson library not found. Downloading...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar' -OutFile '%GSON_JAR%'"
    if not exist "%GSON_JAR%" (
        echo Failed to download Gson. Please download it manually from:
        echo https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
        pause
        exit /b 1
    )
)

echo Compiling...
if not exist "out" mkdir out
javac -d out --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "%GSON_JAR%" src\app\Main.java src\app\controllers\BoardController.java src\app\controllers\TaskDetailController.java src\app\controllers\LoginController.java src\app\controllers\RegisterController.java src\app\models\Task.java src\app\models\Comment.java src\app\models\User.java src\app\services\StorageService.java src\app\services\UserService.java

if %ERRORLEVEL% EQU 0 (
    echo Copying resources...
    xcopy /E /I /Y src\app\views out\app\views >nul
    echo Running...
    java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp "out;%GSON_JAR%" app.Main
) else (
    echo Compilation failed!
    pause
)