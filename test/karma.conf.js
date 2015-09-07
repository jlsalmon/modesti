// Karma configuration
// http://karma-runner.github.io/0.12/config/configuration-file.html
// Generated on 2015-02-21 using
// generator-karma 0.9.0

module.exports = function(config) {
  'use strict';

  config.set({
    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // base path, that will be used to resolve files and exclude
    basePath: '../',

    // testing framework to use (jasmine/mocha/qunit/...)
    frameworks: ['jasmine'],

    // list of files / patterns to load in the browser
    files: [
      // bower:js
      'app/bower_components/modernizr/modernizr.js',
      'app/bower_components/jquery/dist/jquery.js',
      'app/bower_components/angular/angular.js',
      'app/bower_components/angular-cookies/angular-cookies.js',
      'app/bower_components/angular-sanitize/angular-sanitize.js',
      'app/bower_components/bootstrap/dist/js/bootstrap.js',
      'app/bower_components/slimscroll/jquery.slimscroll.min.js',
      'app/bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
      'app/bower_components/lodash/dist/lodash.compat.js',
      'app/bower_components/restangular/dist/restangular.js',
      'app/bower_components/angular-ui-router/release/angular-ui-router.js',
      'app/bower_components/angular-file-upload/angular-file-upload.min.js',
      'app/bower_components/ngstorage/ngStorage.js',
      'app/bower_components/angular-local-storage/dist/angular-local-storage.js',
      'app/bower_components/angular-http-auth/src/http-auth-interceptor.js',
      'app/bower_components/angular-ui-utils/ui-utils.js',
      'app/bower_components/autofill-event/src/autofill-event.js',
      'app/bower_components/zeroclipboard/dist/ZeroClipboard.js',
      'app/bower_components/moment/moment.js',
      'app/bower_components/pikaday/pikaday.js',
      'app/bower_components/handsontable/dist/handsontable.js',
      'app/bower_components/ngHandsontable/dist/ngHandsontable.js',
      'app/bower_components/angular-translate/angular-translate.js',
      'app/bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.js',
      'app/bower_components/angular-translate-storage-cookie/angular-translate-storage-cookie.js',
      'app/bower_components/angular-translate-storage-local/angular-translate-storage-local.js',
      'app/bower_components/spin.js/spin.js',
      'app/bower_components/angular-spinner/angular-spinner.js',
      'app/bower_components/angular-filter/dist/angular-filter.min.js',
      'app/bower_components/angular-ui-select/dist/select.js',
      'app/bower_components/select2/select2.js',
      'app/bower_components/angular-ui-router-title/angular-ui-router-title.js',
      'app/bower_components/angular-mocks/angular-mocks.js',
      'app/bower_components/chai/chai.js',
      'app/bower_components/mocha/mocha.js',
      // endbower
      'app/app.js',
      'app/app.config.js',
      'app/app.routes.js',
      'app/components/services/*.js',
      'app/components/**/*.js',
      'test/mock/**/*.js',
      'test/spec/**/*.js'
    ],

    // list of files / patterns to exclude
    exclude: [
    ],

    // web server port
    port: 8080,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: [
      'PhantomJS'
    ],

    // Which plugins to enable
    plugins: [
      'karma-phantomjs-launcher',
      'karma-jasmine',
      'karma-spec-reporter',
      'karma-atlassian-bamboo-reporter'
    ],

    // reporters configuration
    reporters: ['progress', 'spec', 'bamboo'],

    bambooReporter:{
      filename: 'mocha.json'
    },

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,

    colors: true,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,

    // Uncomment the following lines if you are using grunt's server to run the tests
    // proxies: {
    //   '/': 'http://localhost:9000/'
    // },
    // URL root prevent conflicts with the site root
    // urlRoot: '_karma_'
  });
};
