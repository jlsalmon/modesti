# modesti

Monitoring Data Entry System for Technical Infrastructure (frontend UI)

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

## `grunt publish:test`

Tests, builds, packages the results into a tarball and publishes it to the snapshot repo ready for deployment on the test server.

Note: for the publication to work, you need to pass `--artifactoryUser` and `--artifactoryPassword`.

## `grunt release:prod`

Tests, builds and packages a production release, then publishes the tarball to Artifactory ready for deployment on the production server. Finally bumps the
development version to e.g. 0.0.1-SNAPSHOT and publishes it to the snapshot repo.

Note: you need to pass the release version via `--setversion`. 
