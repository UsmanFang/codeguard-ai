@echo off
echo ===================================================
echo   Launching CodeGuard AI Studio - Hackathon Walkthrough
echo ===================================================

:: Kill old instances (prevents stacking)
taskkill /F /IM java.exe 2>nul
taskkill /F /IM javaw.exe 2>nul

:: Build without clean (prevents lock errors)
call mvn compile javafx:run
pause