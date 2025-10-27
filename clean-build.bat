@echo off
echo ===================================
echo Cleaning ALL build caches...
echo ===================================

echo [1/5] Deleting app\build folder...
if exist app\build (
    rmdir /s /q app\build
    echo     ✓ Deleted app\build
) else (
    echo     - app\build not found
)

echo [2/5] Deleting .gradle cache...
if exist .gradle (
    rmdir /s /q .gradle
    echo     ✓ Deleted .gradle
) else (
    echo     - .gradle not found
)

echo [3/5] Deleting root build folder...
if exist build (
    rmdir /s /q build
    echo     ✓ Deleted build
) else (
    echo     - build not found
)

echo [4/5] Running Gradle clean...
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo     ✗ Clean failed!
    pause
    exit /b 1
)
echo     ✓ Gradle clean successful

echo [5/5] Building project (assembleDebug)...
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ===================================
    echo ✗ BUILD FAILED!
    echo ===================================
    pause
    exit /b 1
)

echo.
echo ===================================
echo ✓ BUILD SUCCESSFUL!
echo ===================================
echo APK location: app\build\outputs\apk\debug\app-debug.apk
echo.
pause
