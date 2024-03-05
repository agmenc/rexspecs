#!/opt/homebrew/bin/bash

set -o errexit # Exit on error
set -o pipefail # Exit on error in a pipeline
set -o nounset # Exit on undeclared variable

MINOR_VERSION=$(< minor_version.txt)
MINOR_VERSION=$((MINOR_VERSION + 1))
echo "$MINOR_VERSION" > "minor_version.txt"
echo "Incrementing minor version to $MINOR_VERSION"
cat build.gradle.kts | sed "s/\(version = \"[0-9]*\.[0-9]*\.\)\([0-9]*\)\"/\1$MINOR_VERSION\"/" > build.gradle.kts.new
rm build.gradle.kts
mv build.gradle.kts.new build.gradle.kts

# Commit the changes locally
git commit -am "Incremented minor version to $MINOR_VERSION"

# Create a tag for the version:
git tag -a v0.1.$MINOR_VERSION -m "Tagging version 0.1.$MINOR_VERSION"

# Push the tag to the remote repository
git push origin master --tags


