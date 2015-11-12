# modesti-api

Monitoring Data Entry System for Technical Infrastructure (backend API)

## Development

Requirements for running backend:
* MongoDB 2.6.10+ running
* Connection to TN (for C2MON and TIM connections) or appropriate tunnels

Instructions:
* Compile and run: `java cern.modesti.Application -Dspring.profiles.active=(test|dev|prod)`

Profiles:
* `test`: uses in-memory h2 database, in-memory MongoDB and in-memory LDAP authentication
* `dev`: uses timdb-test, modestidb-test and xldap.cern.ch
* `prod`: uses timdb, modestidb and xldap.cern.ch

## Build and deployment

Build, test, package and publish a snapshot to Artifactory with Gradle: `./gradlew publish`

Release a new version and publish it: `./gradlew release` (note that you will need to set `artifactoryUser` and `artifactoryPassword` 
in your ~/.gradle/gradle.properties)