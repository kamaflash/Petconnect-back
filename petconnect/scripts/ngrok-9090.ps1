# Levanta un túnel público ngrok hacia Jenkins (puerto host 9090 -> 8080 del contenedor).
# Uso:
#   .\scripts\ngrok-9090.ps1                 # túnel al puerto 9090 (Jenkins)
#   .\scripts\ngrok-9090.ps1 -Port 9090
#
# NOTA sobre el banner de ngrok Free:
#   - La build de ngrok de la Microsoft Store no acepta flags de header (--request-header/--header).
#   - Para evitar la página de aviso (que rompería los POST de GitHub), define un header en el
#     archivo de configuración ~/.ngrok/ngrok.yml, p.ej:
#       version: "3"
#       agent:
#         headers:
#           - name: ngrok-skip-browser-warning
#             value: "true"
#     (o usa una URL de dominio fijo que no muestre el banner).
#
# Deja la terminal corriendo mientras quieras recibir webhooks. Copia la URL
# https://xxxx.ngrok-free.dev que imprime y úsala en el webhook de GitHub:
#   https://xxxx.ngrok-free.dev/generic-webhook-trigger/invoke?token=petconnect-main-webhook

param(
    [int]$Port = 9090
)

$ErrorActionPreference = 'Stop'

# 1. Comprobar que ngrok existe
if (-not (Get-Command ngrok -ErrorAction SilentlyContinue)) {
    throw "No se encontró 'ngrok'. Instálalo (scoop install ngrok / winget install ngrok) y vuelve a ejecutar."
}

# 2. Comprobar que algo escucha en el puerto de Jenkins antes de abrir el túnel
$listening = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
if (-not $listening) {
    Write-Warning "Nada escucha en localhost:$Port. Asegúrate de que el contenedor Jenkins esté up (scripts\jenkins-up.ps1)."
}

# 3. Lanzar ngrok hacia el puerto de Jenkins (no usa headers; ver nota en cabecera)
Write-Host "Abriendo túnel ngrok: http://localhost:$Port -> https://xxx.ngrok-free.dev" -ForegroundColor Green
Write-Host "Deja esta ventana abierta. Ctrl+C para detener." -ForegroundColor Yellow

ngrok http $Port --log stdout