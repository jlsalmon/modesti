'use strict';

/**
 * @ngdoc overview
 * @name modesti
 * @description
 * # modesti
 *
 * Main module of the application.
 */
var app = angular.module('modesti',
  [
    'ngCookies',
    'ngSanitize',
    'ngStorage',
    'ngHandsontable',
    'ui.bootstrap',
    'ui.router',
    'ui.unique',
    'restangular',
    'angularFileUpload',
    'http-auth-interceptor'
  ]);