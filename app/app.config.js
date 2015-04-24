'use strict';

/**
 * @ngdoc function
 * @name modesti.config:configure
 * 
 * @description
 * Configures the various parts of the application.
 */
angular.module('modesti').config(configure);

function configure($httpProvider, RestangularProvider) {
  
  configureRestangular(RestangularProvider);
  configureErrorInterceptors($httpProvider);
}

/**
 * 
 * @param RestangularProvider
 */
function configureRestangular(RestangularProvider) {
  // Set the base URL
  RestangularProvider.setBaseUrl('http://localhost:8080/');
  
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
}