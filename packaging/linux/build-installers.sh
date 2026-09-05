#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(cd -- "$script_dir/../.." && pwd)"
cd "$project_root"

if [[ "$(uname -s)" != "Linux" ]]; then
    echo "Linux installers must be built on Linux." >&2
    exit 1
fi

app_version="$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)"
if [[ "$app_version" == *SNAPSHOT* ]]; then
    echo "Release installers cannot use a SNAPSHOT version." >&2
    exit 1
fi

./mvnw --batch-mode clean package

input_directory="$project_root/target/package-input"
installer_directory="$project_root/target/installer"
application_jar="bizora-$app_version.jar"
mkdir -p "$input_directory" "$installer_directory"
cp "$project_root/target/$application_jar" "$input_directory/$application_jar"

common_arguments=(
    --name Bizora
    --dest "$installer_directory"
    --input "$input_directory"
    --main-jar "$application_jar"
    --main-class com.innovatewithomer.bizora.Launcher
    --app-version "$app_version"
    --vendor InnovateWithOmer
    --description "Offline-first business management and point-of-sale software"
    --copyright "Copyright $(date +%Y) InnovateWithOmer. All rights reserved."
    --app-content "$project_root/NOTICE.txt"
    --icon "$project_root/packaging/linux/Bizora.png"
    --linux-package-name bizora
    --linux-app-category Office
    --linux-menu-group Office
    --linux-shortcut
    --java-options -Dfile.encoding=UTF-8
)

jpackage --type deb --linux-deb-maintainer support@innovatewithomer.dev "${common_arguments[@]}"
jpackage --type rpm "${common_arguments[@]}"

(
    cd "$installer_directory"
    sha256sum ./*.deb ./*.rpm > SHA256SUMS.txt
)

echo "Linux installers created in $installer_directory"
