param(
    [ValidateSet("exe", "msi", "msix")] 
    [string]$Type = "exe"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\\..\\")).Path
Set-Location $ProjectRoot

if ($env:OS -ne "Windows_NT") {
    throw "Windows installers must be built on Windows. Run this script on Windows 10/11 or use the GitHub Actions workflow."
}

$Jpackage = Join-Path $env:JAVA_HOME "bin\\jpackage.exe"
if (-not (Test-Path $Jpackage)) {
    throw "jpackage.exe was not found. Install a full JDK 21 and set JAVA_HOME."
}
if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or
    -not (Get-Command light.exe -ErrorAction SilentlyContinue)) {
    throw "WiX Toolset 3 is required. Install it with: choco install wixtoolset"
}

[xml]$Pom = Get-Content (Join-Path $ProjectRoot "pom.xml")
$RawVersion = [string]$Pom.project.version
# Strip any suffix like -SNAPSHOT
$BaseVersion = $RawVersion -replace "-.*$", ""
# Ensure four‑part numeric version for MSIX manifest (e.g. 1.0.0.0)
$VersionParts = @($BaseVersion.Split('.'))
while ($VersionParts.Count -lt 4) { $VersionParts += "0" }
$Version4 = ($VersionParts[0..3]) -join "."

# Input and output directory setup
$InputDirectory = Join-Path $ProjectRoot "target\package-input"
$InstallerDirectory = Join-Path $ProjectRoot "target\installer"
$Version = $BaseVersion
$ApplicationJar = "bizora-$Version.jar"

if (-not (Test-Path (Join-Path $ProjectRoot "target\$ApplicationJar"))) {
    throw "Application JAR 'target\$ApplicationJar' was not found. Please build the Maven package first (e.g. mvn clean package -DskipTests)."
}

New-Item -ItemType Directory -Path $InputDirectory -Force | Out-Null
Copy-Item (Join-Path $ProjectRoot "target\$ApplicationJar") $InputDirectory -Force
New-Item -ItemType Directory -Path $InstallerDirectory -Force | Out-Null

# MSIX build path
if ($Type -eq "msix") {
    # Step A: generate app‑image using jpackage
    $AppImageDir = Join-Path $ProjectRoot "target\app-image"
    $Arguments = @(
        "--type", "app-image",
        "--name", "Bizora",
        "--dest", $AppImageDir,
        "--input", $InputDirectory,
        "--main-jar", $ApplicationJar,
        "--main-class", "com.innovatewithomer.bizora.Launcher",
        "--app-version", $BaseVersion,
        "--vendor", "InnovateWithOmer",
        "--description", "Offline-first business management and point-of-sale software",
        "--copyright", "Copyright $(Get-Date).Year InnovateWithOmer. All rights reserved.",
        "--app-content", (Join-Path $ProjectRoot "NOTICE.txt"),
        "--icon", (Join-Path $ProjectRoot "packaging\windows\Bizora.ico"),
        "--java-options", "-Dfile.encoding=UTF-8"
    )
    & $Jpackage @Arguments
    if ($LASTEXITCODE -ne 0) { throw "jpackage failed to create the app-image." }

    $AppRoot = Join-Path $AppImageDir "Bizora"
    if (-not (Test-Path $AppRoot)) {
        $AppRoot = $AppImageDir
    }

    # Step B: inject AppxManifest.xml
    $ManifestTemplate = Join-Path $ProjectRoot "packaging\windows\appxmanifest.xml"
    $ManifestTarget = Join-Path $AppRoot "AppxManifest.xml"
    if (-not (Test-Path $ManifestTemplate)) { throw "AppxManifest template not found at $ManifestTemplate" }
    $ManifestContent = Get-Content $ManifestTemplate -Raw
    # Read secrets from environment variables set by the workflow
    $PackageName = $env:MSIX_PACKAGE_NAME
    $PublisherId = $env:MSIX_PUBLISHER_ID
    $PublisherDisplayName = "InnovateWithOmer"
    $ManifestContent = $ManifestContent -replace "__PACKAGE_NAME__", $PackageName
    $ManifestContent = $ManifestContent -replace "__PUBLISHER_ID__", $PublisherId
    $ManifestContent = $ManifestContent -replace "__PUBLISHER_DISPLAY_NAME__", $PublisherDisplayName
    $ManifestContent = $ManifestContent -replace "__VERSION__", $Version4
    Set-Content -Path $ManifestTarget -Value $ManifestContent -Encoding UTF8

    # Step C: copy visual assets into package
    $AssetsSource = Join-Path $ProjectRoot "packaging\windows\assets"
    $AssetsTarget = Join-Path $AppRoot "Assets"
    if (Test-Path $AssetsSource) {
        New-Item -ItemType Directory -Path $AssetsTarget -Force | Out-Null
        Copy-Item -Path (Join-Path $AssetsSource "*") -Destination $AssetsTarget -Force
    }

    # Step D: pack MSIX using makeappx.exe
    $MakeAppxPath = (Get-Command makeappx.exe -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -First 1)
    if (-not $MakeAppxPath) {
        $MakeAppxPath = Get-ChildItem "${env:ProgramFiles(x86)}\Windows Kits\10\bin\*\x64\makeappx.exe" -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -ExpandProperty FullName -First 1
    }
    if (-not $MakeAppxPath) {
        $MakeAppxPath = Get-ChildItem "${env:ProgramFiles(x86)}\Windows Kits\10\bin\*\x86\makeappx.exe" -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -ExpandProperty FullName -First 1
    }
    if (-not $MakeAppxPath) { throw "makeappx.exe not found on this runner. Ensure Windows SDK is installed." }
    $MsixOutput = Join-Path $InstallerDirectory "Bizora.msix"
    & $MakeAppxPath pack -d $AppRoot -p $MsixOutput
    if ($LASTEXITCODE -ne 0) { throw "makeappx failed to create the MSIX package." }

    # SHA‑256 hash for the MSIX
    $Hash = Get-FileHash $MsixOutput -Algorithm SHA256
    "${Hash.Hash.ToLower()}  ${MsixOutput | Split-Path -Leaf}" | Set-Content "${MsixOutput}.sha256" -Encoding ascii
    Write-Host "Created $MsixOutput"
    Write-Host "SHA‑256 $($Hash.Hash)"
    exit 0
}

# Existing exe/msi flow (unchanged)
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
    "--icon", (Join-Path $ProjectRoot "packaging\\windows\\Bizora.ico"),
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
"${Hash.Hash.ToLower()}  ${Installer.Name}" | Set-Content (Join-Path $InstallerDirectory "${Installer.Name}.sha256") -Encoding ascii

Write-Host "Created $($Installer.FullName)"
Write-Host "SHA‑256 $($Hash.Hash)"
