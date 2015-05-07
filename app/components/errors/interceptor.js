'use strict';

/**
 * @ngdoc function
 * @name modesti.factory:errorInterceptor
 *
 * @description
 */
angular.module('modesti').factory('errorInterceptor', errorInterceptor);

function errorInterceptor($q, $rootScope) {
  return {
    request : function(config) {
      return config || $q.when(config);
    },
    requestError : function(request) {
      return $q.reject(request);
    },
    response : function(response) {
      return response || $q.when(response);
    },
    responseError : function(response) {
      if (response && response.status === 0) {
        // Backend not connected
        console.log('error: backend not connected');
        $rootScope.$broadcast("server.error.connection", response.status);
      }
      if (response && response.status === 404) {
        console.log('error: page not found');
      }
      if (response && response.status >= 500) {
        console.log('error: ' + response.statusText);
      }
      return $q.reject(response);
    }
  };
}