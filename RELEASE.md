# Release Process

The project uses [sbt-ci-release](https://github.com/sbt/sbt-ci-release) to automate deployment to Sonatype.

In order to publish, do the following:

- Go to master
- create a tag with the new version (e.g., `git tag -a v0.4.5 -m "v0.4.5"`)
- push the tag with `git push origin v0.1.0`

