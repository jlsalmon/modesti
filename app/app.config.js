'use strict';

/**
 * @ngdoc function
 * @name modesti.config:configure
 *
 * @description
 * Configures the various parts of the application.
 */
angular.module('modesti').config(configure);

var BACKEND_BASE_URL = 'http://localhost:8080';

function configure($httpProvider, $translateProvider, RestangularProvider) {

  // Needed so that Spring Security sends us back a WWW-Authenticate header,
  // which will prevent th browser from showing a basic auth popup
  $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

  // Needed to make sure that the JSESSIONCOOKIE is sent with every request
  $httpProvider.defaults.withCredentials = true;

  configureRestangular(RestangularProvider);
  configureTranslations($translateProvider);
  configureErrorInterceptors($httpProvider);
}

/**
 *
 * @param RestangularProvider
 */
function configureRestangular(RestangularProvider) {
  // Set the base URL
  RestangularProvider.setBaseUrl(BACKEND_BASE_URL);

  // Enable access to the response headers
  RestangularProvider.setFullResponse(true);

  // Add a response interceptor
  RestangularProvider.addResponseInterceptor(function(data, operation, what, url, response, deferred) {

    var extractedData;

    if (operation === "getList") {
      if (data.hasOwnProperty('_embedded')) {
        extractedData = data //._embedded.requests;
      } else {
        extractedData = data.data;
      }
    } else {
      extractedData = data;
    }
    return extractedData;
  });

  // Set the self link
  RestangularProvider.setRestangularFields({
    selfLink : "_links.self.href"
  });
}

/**
 *
 * @param $translateProvider
 */
function configureTranslations($translateProvider) {
  $translateProvider.useStaticFilesLoader({
    prefix: 'translations/locale-',
    suffix: '.json'
  });
  $translateProvider.useSanitizeValueStrategy('escapeParameters');
  $translateProvider.useLocalStorage();
  $translateProvider.preferredLanguage('en');
}

/**
 *
 * @param $httpProvider
 */
function configureErrorInterceptors($httpProvider) {
  $httpProvider.interceptors.push('errorInterceptor');
}