# Build y levanta el contenedor Jenkins con cliente Docker incluido.
# Uso (desde la raíz del proyecto):
#   .\scripts\jenkins-up.ps1                # build + run
#   .\scripts\jenkins-up.ps1 -Network jenkins-net -ImageName jenkins-with-docker
#   .\scripts\jenkins-up.ps1 -Port 8080 -JnlpPort 50000
#   .\scripts\jenkins-up.ps1 -NoRecreate     # no eliminar el contenedor existente

param(
    [string]$ImageName = 'jenkins-with-docker',
    [string]$Tag        = 'latest',
    [string]$Network    = 'jenkins-net',
    [int]$Port          = 8080,
    [int]$JnlpPort      = 50000,
    [switch]$NoRecreate,
    [switch]$NoRestart
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Run-Command {
    param($cmd)
    Write-Host "Running: $cmd" -ForegroundColor Cyan
    & cmd /c $cmd
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed: $cmd"
    }
}

try {
    # 1. Construir la imagen Jenkins con el cliente Docker
    $imageFull = "$ImageName`:$Tag"
    Write-Host "Building Jenkins image: $imageFull" -ForegroundColor Green
    Run-Command "docker build -f Dockerfile.jenkins -t $imageFull ."

    # 2. (Opcional) eliminar el contenedor existente para recrearlo con la nueva imagen
    $exists = docker ps -a --filter "name=^jenkins$" --format '{{.Names}}'
    if ($exists) {
        if ($NoRecreate) {
            Write-Host "Contenedor 'jenkins' ya existe y -NoRecreate definido. Terminando." -ForegroundColor Yellow
            exit 0
        }
        Write-Host "Eliminando contenedor antiguo 'jenkins' (config persistida en el volumen jenkins_home)" -ForegroundColor Yellow
        Run-Command "docker rm -f jenkins"
    }

    # 3. Levantar el contenedor Jenkins
    $restart = if ($NoRestart) { '' } else { '--restart unless-stopped' }
    Write-Host "Levantando Jenkins en puerto $Port (jnlp $JnlpPort)" -ForegroundColor Green
    Run-Command "docker run -d $restart --name jenkins --network $Network -p ${Port}:8080 -p ${JnlpPort}:50000 -v jenkins_home:/var/jenkins_home -v //var/run/docker.sock:/var/run/docker.sock $imageFull"

    # 4. Verificación
    Write-Host "Verificando acceso al daemon Docker desde el contenedor..." -ForegroundColor Green
    Run-Command "docker exec jenkins docker info --format '{{.ServerVersion}}'"

    Write-Host "`nJenkins listo. Abre http://localhost:$Port" -ForegroundColor Green
} catch {
    Write-Error $_.Exception.Message
    exit 1
}