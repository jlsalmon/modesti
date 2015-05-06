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

function configure($httpProvider, $sceDelegateProvider, RestangularProvider) {

  // Needed so that Spring Security sends us back a WWW-Authenticate header
  //$httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

  // Needed so that the browser doesn't "preflight" the backend server
  //$sceDelegateProvider.resourceUrlWhitelist(['self', BACKEND_BASE_URL + '/**']);

  //$httpProvider.defaults.useXDomain = true;
  //delete $httpProvider.defaults.headers.common['X-Requested-With'];


  configureRestangular(RestangularProvider);
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
        extractedData = data._embedded.requests;
      } else {
        extractedData = [];
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
 * @param $httpProvider
 */
function configureErrorInterceptors($httpProvider) {
  $httpProvider.interceptors.push('errorInterceptor');
  $httpProvider.interceptors.push('requestInterceptor');
}