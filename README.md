# modesti-server

Monitoring Data Entry System for Technical Infrastructure (backend server)

## Development

The MODESTI server is designed to be runnable out-of-the-box without any configuration.
To get the server up and running (with no plugins loaded):

```
$ git clone ssh://git@gitlab.cern.ch:7999/modesti/modesti-server.git
$ cd modesti-server
$ ./gradlew build
$ java -jar build/libs/modesti-server-*.jar
```

In the default configuration, the server will use an in-memory database and no
real authentication (a fake test user is created).

### Writing a plugin

For technical documentation about writing a plugin, please refer to the
[MODESTI wiki](https://gitlab.cern.ch/modesti/modesti/wikis/home).

### Deploying a plugin

To use a plugin in a development environment, you must pass the path to the plugin
JAR (and any other necessary files, e.g. config files) using the `loader.path` JVM
argument:

```
java -Dloader.path=/path/to/modesti-plugin-dir/ -jar build/libs/modesti-server-*.jar
```

You can pass multiple directories with `loader.path` separated by commas. Be careful
to pass the argument *before* `-jar` or it will be ignored.

## Publishing

Build, test, package and publish a snapshot to Artifactory with Gradle: `./gradlew publish`

Release a new version and publish it: `./gradlew release` (note that you will need to set `artifactoryUser` and `artifactoryPassword` 
in your ~/.gradle/gradle.properties)
