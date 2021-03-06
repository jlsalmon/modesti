# MODESTI

Monitoring Data Entry System for Technical Infrastructure

## Development

For technical documentation about how to build MODESTI from source and how to write MODESTI plugins, please refer to the
[Wiki](https://gitlab.cern.ch/modesti/modesti/wikis/home).

## Publishing

The GitLab Pipeline is automatically publishing SNAPSHOT artifacts from MASTER branch. It also takes care of deploying the new SNAPSHOT release to https://modesti-test.cern.ch, as well as to restart the test server.

However, you can also manually build, test, package and publish a snapshot to Artifactory with Gradle: `./gradlew publish`
(note that you will need to set `artifactoryUser` and `artifactoryPassword` 
in your ~/.gradle/gradle.properties)


## Releasing a stable version

Before you trigger a release of a new stable version, please make sure that:
- the [Changelog](Changelog.md) file is up to date
- all related issues and Merge request to that version are closed and correctly assigned to the given [Milestone]
- the [Milestone] for the version to release is closed
- a new [Milestone] is created, in order to assign issues from the backlog to it


To release a new version clone this project and run locally: `./gradlew release`

The actual publishing of the stable release is done by GitLab [Pipelines]. Therefore it is important to check, if the triggered [Pipelines] are passing successfully.


## Deploying to Production

- `ssh timoper@modesti.cern.ch`
- Execute the following script to deploy the latest stable MODESTI server release: `~/scripts/deploy-modesti-server-pro.sh`
- Run this command to update in addition all plugins: `cd /opt/modesti-plugins/; ./get-all-plugins-release`
- Restart the server with wreboot command: `wreboot -N MODESTI-SERVER-PRO.jvm`
- The restart usally takes about one minute. To check if the system is back go to https://modesti.cern.ch
- In addition you should also check that the log file does not contain any errors: `less /opt/modesti-server/log/modesti.log`

Finally, it is good practice to inform the modesti-users about the deployed release.

### Rollback

To rollback the MODESTI server it simply requires redirecting the static link to the backup directory:
```bash
rm -f /opt/modesti-server; ln -s /opt/modesti-server-pro.1 /opt/modesti-server
wreboot -N MODESTI-SERVER-PRO.jvm
```

To rollback one of the plugin's, please use the following script and restart once more the MODESTI server:
```bash
cd /opt/modesti-plugins/
> ./get-plugin 
usage: ./get-plugin --name <plugin> [--version (snapshot|release|0.1.2)]
```

[Milestone]: https://gitlab.cern.ch/modesti/modesti/milestones
[Pipelines]: https://gitlab.cern.ch/modesti/modesti/pipelines