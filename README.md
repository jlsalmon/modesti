# modesti

Monitoring and Data Entry System for Technical Infrastructure (frontend UI)

## Development

Requirements:

* Install Node.js on your system
* Install the Grunt CLI: `npm install -g grunt-cli'
* Install the package dependencies: 'npm install && bower install'

Now you can run `grunt serve` to run the development server on localhost:9000. It will watch all source files for changes and automatically reload them in the browser.

## Testing

Running `grunt test` will run the unit tests with karma.

## Building and releasing

To build the source, publish the build artifact and create releases, the following grunt tasks are used:

## `grunt build:test` 

Runs tests and does a simple, local build (no packaging or publishing). Build results in `dist/`.

Note: should you need to modify the backend base URL, you can do so in the `modesti.yaml` configuration file.

## `grunt publish:test`

Tests, builds, packages the results into a tarball and publishes it to Artifactory ready for deployment on the test server.

Note: for the publication to work, you need to modify the `modesti.yaml` configuration file and replace the dummy username and password with real values. 

## `grunt release:test`

Tests, builds, packages, creates a test release by incrementing the package version number and committing the released tag. Finally publishes the tarball to Artifactory ready for deployment on the test server.

By default, the patch version number will be incremented. To increment the minor version, use `grunt release:dev bump:minor`. To increment the major version, 
use `bump:major`.




