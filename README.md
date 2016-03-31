# modesti-server

Monitoring Data Entry System for Technical Infrastructure (backend server)

## Development

For technical documentation about how to build the MODESTI server from source and how to write MODESTI plugins, please refer to the
[MODESTI wiki](https://gitlab.cern.ch/modesti/modesti/wikis/home).

## Publishing

Build, test, package and publish a snapshot to Artifactory with Gradle: `./gradlew publish`

Release a new version and publish it: `./gradlew release` (note that you will need to set `artifactoryUser` and `artifactoryPassword` 
in your ~/.gradle/gradle.properties)
