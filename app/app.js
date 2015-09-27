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
  'ng',
  'ngCookies',
  'ngSanitize',
  'ngStorage',
  'ngHandsontable',
  'ui.bootstrap',
  'ui.router',
  'ui.router.title',
  'ui.select',
  'restangular',
  'angularFileUpload',
  'http-auth-interceptor',
  'pascalprecht.translate',
  'angularSpinner',
  'angular.filter',
  'angular-bootstrap-select'
]);