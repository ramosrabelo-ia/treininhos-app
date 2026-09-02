@echo off
chcp 65001 >nul
title Treino da Luana - Instalador Galaxy Watch8
cd /d "%~dp0"

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0instalar-watch8.ps1"

echo.
echo Pressione qualquer tecla para fechar.
pause >nul
