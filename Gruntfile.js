'use strict';

module.exports = function (grunt) {

  // Load grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  var semver = require('semver');
  var modRewrite = require('connect-modrewrite');

  // Configurable paths for the application
  var appConfig = {
    app: require('./bower.json').appPath || 'app',
    dist: 'dist',
    version: require('./package.json').version
  };

  // Define the configuration for all the tasks
  grunt.initConfig({

    // Project settings
    yeoman: appConfig,

    // Profile-specific settings
    config: {
      test: {
        options: {
          variables: {
            backendBaseUrl: 'https://modesti-test.cern.ch:8443'
          }
        }
      },
      prod: {
        options: {
          variables: {
            backendBaseUrl: 'https://modesti.cern.ch:8443'
          }
        }
      }
    },

    // Watches files for changes and runs tasks based on the changed files
    watch: {
      options: {
        spawn: false
      },
      bower: {
        files: ['bower.json'],
        tasks: ['wiredep']
      },
      js: {
        files: ['<%= yeoman.app %>/components/**/*.js'],
        tasks: ['newer:jshint:all'],
        options: {
          livereload: '<%= connect.options.livereload %>'
        }
      },
      jsTest: {
        files: ['test/spec/**/*.js'],
        tasks: ['newer:jshint:test', 'karma']
      },
      styles: {
        files: ['<%= yeoman.app %>/styles/**/*.css'],
        tasks: ['newer:copy:styles', 'autoprefixer']
      },
      gruntfile: {
        files: ['Gruntfile.js']
      },
      livereload: {
        options: {
          livereload: '<%= connect.options.livereload %>'
        },
        files: [
          '<%= yeoman.app %>/**/*.html',
          '.tmp/styles/**/*.css',
          '<%= yeoman.app %>/images/**/*.{png,jpg,jpeg,gif,webp,svg}'
        ]
      }
    },

    // The actual grunt server settings
    connect: {
      options: {
        port: 9000,
        // Change this to '0.0.0.0' to access the server from outside.
        hostname: 'localhost',
        livereload: 35729
      },
      livereload: {
        options: {
          open: true,
          middleware: function (connect) {
            return [
              modRewrite(['^[^\\.]*$ /index.html [L]']),
              connect.static('.tmp'),
              connect().use(
                '/bower_components',
                connect.static('./bower_components')
              ),
              connect().use(
                '/app/styles',
                connect.static('./app/styles')
              ),
              connect.static(appConfig.app)
            ];
          }
        }
      },
      test: {
        options: {
          port: 9001,
          middleware: function (connect) {
            return [
              connect.static('.tmp'),
              connect.static('test'),
              connect().use(
                '/bower_components',
                connect.static('./bower_components')
              ),
              connect.static(appConfig.app)
            ];
          }
        }
      },
      dist: {
        options: {
          open: true,
          base: '<%= yeoman.dist %>'
        }
      }
    },

    // Make sure code styles are up to par and there are no obvious mistakes
    jshint: {
      options: {
        jshintrc: '.jshintrc',
        reporter: require('jshint-stylish')
      },
      all: {
        src: [
          //'Gruntfile.js',
          '<%= yeoman.app %>/components/**/*.js'
        ]
      },
      test: {
        options: {
          jshintrc: 'test/.jshintrc'
        },
        src: ['test/spec/**/*.js']
      }
    },

    // Empties folders to start fresh
    clean: {
      dist: {
        files: [{
          dot: true,
          src: [
            '.tmp',
            'build/**/*',
            '<%= yeoman.dist %>/**/*',
            '!<%= yeoman.dist %>/.git**/*'
          ]
        }]
      },
      server: '.tmp'
    },

    // Add vendor prefixed styles
    autoprefixer: {
      options: {
        browsers: ['last 1 version']
      },
      server: {
        options: {
          map: true,
        },
        files: [{
          expand: true,
          cwd: '.tmp/styles/',
          src: '**/*.css',
          dest: '.tmp/styles/'
        }]
      },
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/styles/',
          src: '**/*.css',
          dest: '.tmp/styles/'
        }]
      }
    },

    // Automatically inject Bower components into the app
    wiredep: {
      app: {
        src: ['<%= yeoman.app %>/index.html'],
        ignorePath:  /\.\.\//
      },
      test: {
        devDependencies: true,
        src: '<%= karma.unit.configFile %>',
        ignorePath:  /\.\.\//,
        fileTypes:{
          js: {
            block: /(([\s\t]*)\/{2}\s*?bower:\s*?(\S*))(\n|\r|.)*?(\/{2}\s*endbower)/gi,
              detect: {
                js: /'(.*\.js)'/gi
              },
              replace: {
                js: '\'{{filePath}}\','
              }
            }
          }
      }
    },

    // Renames files for browser caching purposes
    filerev: {
      dist: {
        src: [
          '<%= yeoman.dist %>/components/**/*.js',
          '<%= yeoman.dist %>/styles/**/*.css',
          '<%= yeoman.dist %>/images/**/*.{png,jpg,jpeg,gif,webp,svg}',
          '<%= yeoman.dist %>/styles/fonts/*'
        ]
      }
    },

    // Reads HTML for usemin blocks to enable smart builds that automatically
    // concat, minify and revision files. Creates configurations in memory so
    // additional tasks can operate on them
    useminPrepare: {
      html: '<%= yeoman.app %>/index.html',
      options: {
        dest: '<%= yeoman.dist %>',
        flow: {
          html: {
            steps: {
              js: ['concat', 'uglifyjs'],
              css: ['cssmin']
            },
            post: {}
          }
        }
      }
    },

    // Performs rewrites based on filerev and the useminPrepare configuration
    usemin: {
      html: ['<%= yeoman.dist %>/**/*.html'],
      css: ['<%= yeoman.dist %>/styles/**/*.css'],
      options: {
        assetsDirs: [
          '<%= yeoman.dist %>',
          '<%= yeoman.dist %>/images',
          '<%= yeoman.dist %>/styles'
        ]
      }
    },

    // The following *-min tasks will produce minified files in the dist folder
    // By default, your `index.html`'s <!-- Usemin block --> will take care of
    // minification. These next options are pre-configured if you do not wish
    // to use the Usemin blocks.
    // cssmin: {
    //   dist: {
    //     files: {
    //       '<%= yeoman.dist %>/styles/main.css': [
    //         '.tmp/styles/**/*.css'
    //       ]
    //     }
    //   }
    // },
    // uglify: {
    //   dist: {
    //     files: {
    //       '<%= yeoman.dist %>/scripts/scripts.js': [
    //         '<%= yeoman.dist %>/scripts/scripts.js'
    //       ]
    //     }
    //   }
    // },
    // concat: {
    //   dist: {}
    // },

    imagemin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '**/*.{png,jpg,jpeg,gif}',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },

    svgmin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '**/*.svg',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },

    htmlmin: {
      dist: {
        options: {
          collapseWhitespace: true,
          conservativeCollapse: true,
          collapseBooleanAttributes: true,
          removeCommentsFromCDATA: true,
          removeOptionalTags: true
        },
        files: [{
          expand: true,
          cwd: '<%= yeoman.dist %>',
          src: ['*.html', 'components/**/*.html'],
          dest: '<%= yeoman.dist %>'
        }]
      }
    },

    // ng-annotate tries to make the code safe for minification automatically
    // by using the Angular long form for dependency injection.
    ngAnnotate: {
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/concat/scripts',
          src: '*.js',
          dest: '.tmp/concat/scripts'
        }]
      }
    },

    // Replace Google CDN references
    //cdnify: {
    //  dist: {
    //    html: ['<%= yeoman.dist %>/*.html']
    //  }
    //},

    // Copies remaining files to places other tasks can use
    copy: {
      dist: {
        files: [{
          expand: true,
          dot: true,
          cwd: '<%= yeoman.app %>',
          dest: '<%= yeoman.dist %>',
          src: [
            '*.{ico,png,txt}',
            '.htaccess',
            '*.html',
            'components/**/*.html',
            'images/**/*.{webp}',
            'styles/fonts/**/*.*',
            'fonts/*.*',
            'translations/*.json'
          ]
        }, {
          expand: true,
          cwd: '.tmp/images',
          dest: '<%= yeoman.dist %>/images',
          src: ['generated/*']
        }, {
          expand: true,
          cwd: '<%= yeoman.app %>/bower_components/bootstrap/dist',
          src: 'fonts/*',
          dest: '<%= yeoman.dist %>'
        }, {
          expand: true,
          dot: true,
          cwd: '<%= yeoman.app %>/bower_components/font-awesome',
          src: ['fonts/*.*'],
          dest: '<%= yeoman.dist %>'
        }, {
          expand: true,
          dot: true,
          cwd: '<%= yeoman.app %>/bower_components/famfamfam-flags-sprite/src',
          src: ['*.png'],
          dest: '<%= yeoman.dist %>/styles'
        }, {
          expand: true,
          dot: true,
          cwd: '<%= yeoman.app %>/bower_components/select2',
          src: ['*.{png,gif}'],
          dest: '<%= yeoman.dist %>/styles'
        }]
      },
      styles: {
        expand: true,
        cwd: '<%= yeoman.app %>/styles',
        dest: '.tmp/styles/',
        src: '**/*.css'
      }
    },

    // Replaces the backend base URL placeholder with the real value based on
    // the profile in use (test, prod)
    replace: {
      dist: {
        options: {
          patterns: [
            {
              match: 'BACKEND_BASE_URL',
              replacement: '<%= grunt.config.get("backendBaseUrl") %>'
            }
          ]
        },
        files: [
          {expand: true, flatten: true, src: '<%= yeoman.dist %>/scripts/*.js', dest: '<%= yeoman.dist %>/scripts'}
        ],
        pedantic: true
      }
    },

    // Bumps the version number of the application. The version number is made
    // available to other tasks via <%= yeoman.version %>.
    bump: {
      options: {
        files: ['package.json', 'bower.json', '<%= yeoman.app %>/app.js', '<%= yeoman.dist %>/scripts/scripts.js'],
        commitFiles: ['package.json', 'bower.json', '<%= yeoman.app %>/app.js'],
        pushTo: 'origin',
        updateConfigs: ['yeoman'],
        createTag: grunt.option('createTag') !== false
      }
    },

    // Publishes a tarball of the built application to artifactory
    artifactory: {
      /*jshint camelcase: false */
      options: {
        id: 'cern.modesti:modesti:tar.gz',
        version: '<%= yeoman.version %>',
        path: 'build/',
        base_path: '',
        username: grunt.option('artifactoryUser'),
        password: grunt.option('artifactoryPassword')
      },
      test: {
        options: {
          url: 'http://artifactory/beco-development-local'
        },
        files: [
          { src: ['dist/**/*', 'dist/.htaccess', 'deployment.xml'] }
        ]
      },
      prod: {
        options: {
          url: 'http://artifactory/beco-release-local'
        },
        files: [
          { src: ['dist/**/*', 'dist/.htaccess', 'deployment.xml'] }
        ]
      }
    },

    // Run some tasks in parallel to speed up the build process
    concurrent: {
      server: [
        'copy:styles'
      ],
      test: [
        'copy:styles'
      ],
      dist: [
        'copy:styles',
        //'imagemin:newer',
        'svgmin'
      ]
    },

    // Test settings
    karma: {
      unit: {
        configFile: 'test/karma.conf.js',
        singleRun: true
      }
    }
  });

  grunt.registerTask('serve', 'Compile then start a connect web server', function (target) {
    if (target === 'dist') {
      return grunt.task.run(['build', 'connect:dist:keepalive']);
    }

    grunt.task.run([
      'clean:server',
      'wiredep',
      'concurrent:server',
      'autoprefixer:server',
      'connect:livereload',
      'watch'
    ]);
  });

  grunt.registerTask('server', 'DEPRECATED TASK. Use the "serve" task instead', function (target) {
    grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
    grunt.task.run(['serve:' + target]);
  });

  grunt.registerTask('test', [
    'clean:server',
    'wiredep',
    'concurrent:test',
    'autoprefixer',
    'connect:test',
    'karma'
  ]);

  grunt.registerTask('build', [
    'newer:jshint',
    'test',
    'clean:dist',
    'wiredep',
    'useminPrepare',
    'concurrent:dist',
    'autoprefixer',
    'concat',
    'ngAnnotate',
    'copy:dist',
    //'cdnify',
    'cssmin',
    'uglify',
    'filerev',
    'usemin',
    'htmlmin',
    'replace'
  ]);

  grunt.registerTask('build:test', [
    'config:test',
    'build'
  ]);

  grunt.registerTask('build:prod', [
    'config:prod',
    'build'
  ]);

  grunt.registerTask('publish:test', 'Publish a test tarball.', function () {
    checkPublicationOptions();

    grunt.task.run([
      'build:test',
      'artifactory:test:publish'
    ]);
  });

  grunt.registerTask('release:prod', 'Release a production tarball.', function (version) {
    version || grunt.fail.fatal('You must supply a release version');
    checkPublicationOptions();

    grunt.log.writeln('Setting release version to ' + version);
    grunt.option('setversion', version);

    grunt.task.run([
      'build:prod',
      'bump',
      'artifactory:prod:publish',
      'bump-snapshot:' + version,
      'publish:test'
    ]);
  });

  grunt.registerTask('bump-snapshot', 'Bump the version to ', function (version) {
    checkPublicationOptions();

    var nextSnapshotVersion = semver.inc(version, 'patch') + '-SNAPSHOT';
    grunt.log.writeln('Bumping development version to ' + nextSnapshotVersion);
    grunt.option('setversion', nextSnapshotVersion);
    grunt.option('createTag', false);

    grunt.task.run('bump');
  });

  function checkPublicationOptions() {
    grunt.option('artifactoryUser')     || grunt.fail.fatal('Publishing artifact requires --artifactoryUser');
    grunt.option('artifactoryPassword') || grunt.fail.fatal('Publishing artifact requires --artifactoryPassword');
  }
};
