param(
    [switch]$SkipTests
)

Set-StrictMode -Version Latest

function Run-Command {
    param($cmd)
    Write-Host "Running: $cmd"
    $res = & cmd /c $cmd
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed: $cmd"
    }
    return $res
}

try {
    $skipArg = if ($SkipTests) { '-DskipTests' } else { '' }
    # Prefer mvn if available, fallback to mvnw.cmd in repo root
    $mvnCmd = $null
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        $mvnCmd = 'mvn'
    } elseif (Test-Path .\mvnw.cmd) {
        $mvnCmd = '.\mvnw.cmd'
    } else {
        throw "Neither 'mvn' nor '.\mvnw.cmd' found. Install Maven or run this script from the project root where 'mvnw.cmd' exists."
    }

    Write-Host "Building project with: $mvnCmd"
    Run-Command "$mvnCmd clean package $skipArg"

    Write-Host "Building Docker image 'petconnect:latest'..."
    Run-Command "docker build -t petconnect:latest ."

    Write-Host "Starting services with docker-compose..."
    Run-Command "docker-compose up -d"

    Write-Host "Done. Use 'docker-compose logs -f' to follow logs."
} catch {
    Write-Error $_.Exception.Message
    exit 1
}
