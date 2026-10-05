# Run with: powershell -ExecutionPolicy Bypass -File .\setup-windows.ps1
# Downloads official Gradle and generates standard Gradle wrapper files.
$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot
$version = "8.11.1"
$cache = Join-Path $projectRoot ".gradle-bootstrap"
New-Item -ItemType Directory -Force -Path $cache | Out-Null
if (-not $env:JAVA_HOME) {
    $studioJdk = Join-Path $env:ProgramFiles "Android\Android Studio\jbr"
    if (Test-Path $studioJdk) { $env:JAVA_HOME = $studioJdk }
}
if (-not $env:JAVA_HOME -and -not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "Install Android Studio or JDK 17, then rerun."
}
$baseUrl = "https://services.gradle.org/distributions/gradle-$version-bin.zip"
$archive = Join-Path $cache "gradle-$version-bin.zip"
$expected = (Invoke-RestMethod "$baseUrl.sha256").Trim().ToLower()
if ($expected -notmatch '^[0-9a-f]{64}$') { throw "Invalid Gradle checksum response." }
if (-not (Test-Path $archive)) { Invoke-WebRequest -UseBasicParsing $baseUrl -OutFile $archive }
if ((Get-FileHash $archive -Algorithm SHA256).Hash.ToLower() -ne $expected) {
    throw "Gradle checksum mismatch. Delete the ZIP in .gradle-bootstrap and try again."
}
$gradle = Join-Path $cache "gradle-$version\bin\gradle.bat"
if (-not (Test-Path $gradle)) { Expand-Archive -Path $archive -DestinationPath $cache -Force }
# Generate wrapper in an empty build, so Android SDK is not needed for this step.
$wrapperProject = Join-Path $cache "wrapper-project"
New-Item -ItemType Directory -Force -Path $wrapperProject | Out-Null
Set-Content -Path (Join-Path $wrapperProject "settings.gradle") -Value "rootProject.name = 'wrapper-bootstrap'"
& $gradle -p $wrapperProject wrapper --gradle-version $version --distribution-type bin --gradle-distribution-sha256-sum $expected
if ($LASTEXITCODE -ne 0) { throw "Gradle wrapper generation failed." }
Copy-Item (Join-Path $wrapperProject "gradlew") $projectRoot -Force
Copy-Item (Join-Path $wrapperProject "gradlew.bat") $projectRoot -Force
Copy-Item (Join-Path $wrapperProject "gradle") $projectRoot -Recurse -Force
Write-Host "Ready. Open this CatDogAndroid folder in Android Studio and sync."
