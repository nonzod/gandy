#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
GRADLE_FILE="$PROJECT_DIR/app/build.gradle.kts"

usage() {
    echo "Usage: $0 [--update-version major|release|fix]"
    echo ""
    echo "Builds the project for production (assembleRelease)."
    echo ""
    echo "Options:"
    echo "  --update-version TYPE   Increment version before building."
    echo "                          major   → X.y.y"
    echo "                          release → x.X.y"
    echo "                          fix     → x.y.X"
    exit 1
}

update_version=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --update-version)
            [[ $# -lt 2 ]] && usage
            update_version="$2"
            if [[ "$update_version" != "major" && "$update_version" != "release" && "$update_version" != "fix" ]]; then
                echo "Error: --update-version must be major, release, or fix"
                exit 1
            fi
            shift 2
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "Unknown option: $1"
            usage
            ;;
    esac
done

if [[ -n "$update_version" ]]; then
    current_name=$(grep -oP 'versionName\s*=\s*"\K[^"]+' "$GRADLE_FILE")
    current_code=$(grep -oP 'versionCode\s*=\s*\K[0-9]+' "$GRADLE_FILE")

    IFS='.' read -r major minor patch <<< "$current_name"

    case "$update_version" in
        major)   major=$((major + 1)); minor=0; patch=0 ;;
        release) minor=$((minor + 1)); patch=0 ;;
        fix)     patch=$((patch + 1)) ;;
    esac

    new_name="$major.$minor.$patch"
    new_code=$((current_code + 1))

    sed -i "s/versionCode = $current_code/versionCode = $new_code/" "$GRADLE_FILE"
    sed -i "s/versionName = \"$current_name\"/versionName = \"$new_name\"/" "$GRADLE_FILE"

    echo "Version: $current_name → $new_name (code: $current_code → $new_code)"
fi

echo "Building release APK..."
cd "$PROJECT_DIR"
./gradlew assembleRelease

version=$(grep -oP 'versionName\s*=\s*"\K[^"]+' "$GRADLE_FILE")
apk_path=$(find "$PROJECT_DIR/app/build/outputs/apk/release" -name "*.apk" 2>/dev/null | head -1)
if [[ -n "$apk_path" ]]; then
    dest="$PROJECT_DIR/app/build/outputs/apk/release/gandy-${version}.apk"
    mv "$apk_path" "$dest"
    echo ""
    echo "APK: $dest"
fi
