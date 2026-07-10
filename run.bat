@echo off
echo ===================================================
echo          Launching CodeGuard AI Studio
echo ===================================================
echo               Team ByteAnarchists
echo ===================================================
echo ___________________________________________________
echo             o   Mohammad Ali Mughal
echo             o   Muhammad Saim
echo             o   Muhammad Usman
echo ===================================================

:: Kill old instances (prevents stacking)
taskkill /F /IM java.exe 2>nul
taskkill /F /IM javaw.exe 2>nul

:: Build without clean (prevents lock errors)
call mvn compile javafx:run
pause