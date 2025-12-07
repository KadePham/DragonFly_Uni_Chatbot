@echo off
REM Firebase Database Rules Deployment Script for Windows
REM This script deploys the Realtime Database rules from database.rules.json

echo =========================================
echo Firebase Realtime Database Rules Deployment
echo =========================================
echo.

REM Check if firebase CLI is installed
where firebase >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Firebase CLI is not installed or not in PATH
    echo.
    echo Please install Firebase CLI:
    echo   npm install -g firebase-tools
    echo.
    echo Then try again.
    pause
    exit /b 1
)

echo ✓ Firebase CLI found
echo.

REM Check if database.rules.json exists
if not exist "database.rules.json" (
    echo ERROR: database.rules.json not found in current directory
    echo Please run this script from the project root directory
    pause
    exit /b 1
)

echo ✓ database.rules.json found
echo.

REM Show current rules file content
echo Current rules in database.rules.json:
echo =====================================
type database.rules.json
echo.
echo =====================================
echo.

REM Ask for confirmation
set /p confirm="Do you want to deploy these rules? (y/n): "
if /i not "%confirm%"=="y" (
    echo Deployment cancelled.
    pause
    exit /b 0
)

echo.
echo Deploying Realtime Database rules...
echo.

REM Deploy the rules
firebase deploy --only database

if %ERRORLEVEL% equ 0 (
    echo.
    echo ✓ Rules deployed successfully!
    echo.
    echo The Firebase Realtime Database rules have been updated.
    echo Users should now be able to send messages without permission errors.
) else (
    echo.
    echo ✗ Deployment failed!
    echo Please check the error message above and ensure:
    echo   1. You are logged in to Firebase (firebase login)
    echo   2. The correct project is selected (firebase use --add)
    echo   3. Your internet connection is working
)

echo.
pause

