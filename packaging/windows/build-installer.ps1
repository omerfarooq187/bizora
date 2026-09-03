param(
    [ValidateSet("exe", "msi")]
    [string]$Type = "exe"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $ProjectRoot

if ($env:OS -ne "Windows_NT") {
    throw "Windows installers must be built on Windows. Run this script on Windows 10/11 or use the GitHub Actions workflow."
}

$Jpackage = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
if (-not (Test-Path $Jpackage)) {
    throw "jpackage.exe was not found. Install a full JDK 21 and set JAVA_HOME."
}
if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or
    -not (Get-Command light.exe -ErrorAction SilentlyContinue)) {
    throw "WiX Toolset 3 is required. Install it with: choco install wixtoolset"
}

[xml]$Pom = Get-Content (Join-Path $ProjectRoot "pom.xml")
$Version = [string]$Pom.project.version
if ($Version -match "SNAPSHOT") {
    throw "Release installers cannot use a SNAPSHOT version."
}

& .\mvnw.cmd -B clean package
if ($LASTEXITCODE -ne 0) { throw "Maven build or tests failed." }

$InputDirectory = Join-Path $ProjectRoot "target\package-input"
$InstallerDirectory = Join-Path $ProjectRoot "target\installer"
$ApplicationJar = "bizora-$Version.jar"
Copy-Item (Join-Path $ProjectRoot "target\$ApplicationJar") $InputDirectory -Force
New-Item -ItemType Directory -Path $InstallerDirectory -Force | Out-Null

$CopyrightYear = (Get-Date).Year
$Arguments = @(
    "--type", $Type,
    "--name", "Bizora",
    "--dest", $InstallerDirectory,
    "--input", $InputDirectory,
    "--main-jar", $ApplicationJar,
    "--main-class", "com.innovatewithomer.bizora.Launcher",
    "--app-version", $Version,
    "--vendor", "InnovateWithOmer",
    "--description", "Offline-first business management and point-of-sale software",
    "--copyright", "Copyright $CopyrightYear InnovateWithOmer. All rights reserved.",
    "--app-content", (Join-Path $ProjectRoot "NOTICE.txt"),
    "--icon", (Join-Path $ProjectRoot "packaging\windows\Bizora.ico"),
    "--win-menu",
    "--win-menu-group", "Bizora",
    "--win-shortcut",
    "--win-shortcut-prompt",
    "--win-dir-chooser",
    "--win-per-user-install",
    "--win-help-url", "https://innovatewithomer.dev",
    "--win-upgrade-uuid", "9475a604-45ac-4ac1-b3d8-86c367b10b30",
    "--java-options", "-Dfile.encoding=UTF-8"
)

& $Jpackage @Arguments
if ($LASTEXITCODE -ne 0) { throw "jpackage failed to create the Windows installer." }

$Installer = Get-ChildItem $InstallerDirectory -Filter "*.$Type" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if (-not $Installer) { throw "The installer was not created." }

$Hash = Get-FileHash $Installer.FullName -Algorithm SHA256
"$($Hash.Hash.ToLower())  $($Installer.Name)" |
    Set-Content (Join-Path $InstallerDirectory "$($Installer.Name).sha256") -Encoding ascii

Write-Host "Created $($Installer.FullName)"
Write-Host "SHA-256 $($Hash.Hash)"
