@echo off
REM Build script for Patchwork app

echo Building Patchwork APK...
echo.

call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo  BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Debug APK location:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on device:
    echo adb install -r app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo ========================================
    echo  BUILD FAILED
    echo ========================================
    echo.
)

pause
