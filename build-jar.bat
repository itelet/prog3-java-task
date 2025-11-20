@echo off
setlocal enabledelayedexpansion

set JAVAFX_PATH=C:\javafx-sdk-25.0.1\lib
set GSON_JAR=gson-2.10.1.jar
set APP_NAME=TaskManager
set MAIN_CLASS=app.Main

echo ===================================
echo Building %APP_NAME%.jar
echo ===================================

REM Step 1: Check/Download Gson
echo.
echo [1/4] Checking for Gson library...
if not exist "%GSON_JAR%" (
    echo Gson library not found. Downloading...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar' -OutFile '%GSON_JAR%'"
    if not exist "%GSON_JAR%" (
        echo Failed to download Gson.
        pause
        exit /b 1
    )
)
echo Gson library ready.

REM Step 2: Compile
echo.
echo [2/4] Compiling application...
if not exist "out" mkdir out
javac -d out --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "%GSON_JAR%" ^
    src\app\Main.java ^
    src\app\controllers\BoardController.java ^
    src\app\controllers\TaskDetailController.java ^
    src\app\controllers\LoginController.java ^
    src\app\controllers\RegisterController.java ^
    src\app\models\Task.java ^
    src\app\models\Comment.java ^
    src\app\models\User.java ^
    src\app\services\StorageService.java ^
    src\app\services\UserService.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Step 3: Copy resources
echo.
echo [3/4] Copying resources...
xcopy /E /I /Y src\app\views out\app\views >nul

REM Step 4: Create JAR with dependencies
echo.
echo [4/4] Creating JAR file...
if not exist "dist" mkdir dist

REM Extract Gson into out directory
echo Extracting Gson library...
cd out
jar xf "..\%GSON_JAR%"
cd ..

REM Create manifest
echo Main-Class: %MAIN_CLASS% > manifest.txt

REM Create JAR
jar cfm dist\%APP_NAME%.jar manifest.txt -C out .
del manifest.txt

if exist "dist\%APP_NAME%.jar" (
    echo.
    echo ===================================
    echo Build successful!
    echo ===================================
    echo JAR location: dist\%APP_NAME%.jar
    echo.
    echo To run: java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -jar dist\%APP_NAME%.jar
    echo.
) else (
    echo Failed to create JAR!
    pause
    exit /b 1
)

pause
endlocal