Publishing to Sonatype
======================

Copied steps from here: https://dev.to/tschuehly/how-to-publish-a-kotlinjava-spring-boot-library-with-gradle-to-maven-central-complete-guide-402a#8-when-is-the-library-actually-available-to-use

The gpg steps had complications, so fixed versions are listed in full below

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

The gradle step needed an extra plugin:
```groovy
plugins {
    `maven-publish`
}
```

Creating a tag:
```bash
git tag -a v0.0.1-SNAPSHOT -m "Test Maven publishing toolchain"
```
