Publishing to Sonatype
======================

I copied most of the steps from [this excellent article][1], and added a few notes and a couple of extra steps.

Repeated Steps (Every time I publish a new version)
---
1. Make sure the version number in `build.gradle.kts` is correct
2. Commit the changes locally
3. Create a tag for the version:
```bash
git tag -a v0.0.1-SNAPSHOT -m "Test Maven publishing toolchain"
```
4. Wait a few minutes, then do a [quick Sonatype Search][2] for the artifact

[1]: https://dev.to/tschuehly/how-to-publish-a-kotlinjava-spring-boot-library-with-gradle-to-maven-central-complete-guide-402a#8-when-is-the-library-actually-available-to-use
[2]: https://s01.oss.sonatype.org/#nexus-search;quick~agmenc

Modifications/Clarifications for One-time Publishing Setup
---

### GPG setup
The gpg steps had complications, mostly around getting the correct address of the key server, so updated versions are listed in full below

```bash
# Generate a key, using your name, email address, and a descriptive comment that reminds you WTF it is for
gpg --full-generate-key

# IN BROWSER:
# Save the passphrase you used in your password manager 
 
# Check the key has save (look for today's date) and the key ID is the long hash between the `sec` and `uid` lines
gpg --list-secret-keys

# Find the IP address of the gpg server, because using the domain name doesn't work
host keyserver.ubuntu.com

# Publish the key to the IP address you found
gpg --keyserver <IP address of keyserver.ubuntu.com> --send-keys <key ID>

# Get the hash of the key
gpg --export <key ID> | base64

# IN BROWSER:
# Add the resulting exported key to GitHub as JRELEASER_GPG_PUBLIC_KEY

# Export the secret key (!?! - yes, I know)
gpg --export-secret-keys <key ID> | base64

# IN BROWSER:
# Add the resulting exported key to GitHub as JRELEASER_GPG_SECRET_KEY

# IN BROWSER:
# JRELEASER_GPG_PASSPHRASE - the passphrase you used when you created <key ID>
# JRELEASER_NEXUS2_USERNAME - your Sonatype username
# JRELEASER_NEXUS2_PASSWORD - your Sonatype password

```

### Missing Plugin
The gradle step needed an extra plugin:
```groovy
plugins {
    `maven-publish`
}
```

### Manual release in Sonatype
When getting everything set up, you can set `jreleaser` `closeRepository` and `releaseRepository` to false, and then
manually release the repository in Sonatype. Like this: 
* Go to https://s01.oss.sonatype.org/#stagingRepositories
* `Close` the repository
* `Release` the repository