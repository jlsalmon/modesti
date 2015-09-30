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

## Build and deployment

Package configurations have been created for the following tasks: 

## `grunt build` 

Runs tests and does a simple, local build (no packaging or publishing). Build results in `dist/`.

## `grunt publish:dev`

Tests, builds, packages the results into a tarball and publishes it to Artifactory. 

Note: for the publication to work, you must create an `artifactory.json` file containing the repository details in the following format:


    {
      "dev":  { 
        "repository": "beco-development-local", 
        "username": "...", 
        "password": "..." 
      },
      "release": { 
        "repository": "...", 
        "username": "...", 
        "password": "..."
      },
    }


## `grunt release:dev`

Tests, builds, packages, creates a development release by incrementing the package version number and committing the released tag. Finally publishes the artifact.

By default, the patch version number will be incremented. To increment the minor version, use `grunt release:dev bump:minor`. To increment the major version, 
use `bump:major`.




