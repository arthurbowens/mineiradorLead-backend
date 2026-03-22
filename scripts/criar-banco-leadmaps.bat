@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM Cria o banco "leadmaps" (igual ao jdbc:postgresql://localhost:5432/leadmaps do application.yml).
REM Execute no CMD. Digite a senha do usuario postgres quando o psql pedir.

set "PSQL="

for %%V in (18 17 16 15 14) do (
  if exist "C:\Program Files\PostgreSQL\%%V\bin\psql.exe" (
    set "PSQL=C:\Program Files\PostgreSQL\%%V\bin\psql.exe"
    goto :found
  )
)

:found
if not defined PSQL (
  echo Nao encontrei psql em Program Files\PostgreSQL\14 a 18.
  echo Defina manualmente, por exemplo:
  echo   set "PSQL=C:\Program Files\PostgreSQL\16\bin\psql.exe"
  echo Depois rode de novo este .bat
  pause
  exit /b 1
)

echo Usando: !PSQL!
"!PSQL!" -U postgres -d postgres -c "CREATE DATABASE leadmaps ENCODING 'UTF8';"

if errorlevel 1 (
  echo.
  echo Se a mensagem for que o banco ja existe, pode ignorar e subir o Spring.
  echo Caso contrario: confira se o servico PostgreSQL esta rodando e a senha de postgres.
  pause
  exit /b 1
)

echo.
echo OK — banco leadmaps criado. Suba o LeadMaps Pro de novo.
pause
