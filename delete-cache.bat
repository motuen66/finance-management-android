@echo off
cd /d "%~dp0"
echo ========================================
echo DELETING ALL BUILD CACHES
echo ========================================
echo.

echo [1/4] Deleting app\build...
if exist "app\build" (
    rd /s /q "app\build"
    echo      OK - app\build deleted
) else (
    echo      SKIP - app\build not found
)

echo [2/4] Deleting .gradle...
if exist ".gradle" (
    rd /s /q ".gradle"
    echo      OK - .gradle deleted
) else (
    echo      SKIP - .gradle not found
)

echo [3/4] Deleting root build...
if exist "build" (
    rd /s /q "build"
    echo      OK - build deleted
) else (
    echo      SKIP - build not found
)

echo [4/4] Deleting app\.cxx (if exists)...
if exist "app\.cxx" (
    rd /s /q "app\.cxx"
    echo      OK - app\.cxx deleted
) else (
    echo      SKIP - app\.cxx not found
)

echo.
echo ========================================
echo CACHE DELETED SUCCESSFULLY!
echo ========================================
echo.
echo Next: Run clean-build.bat to build
pause
