'use strict';

/**
 * @ngdoc service
 * @name modesti.ValidationService
 * @description # ValidationService
 */
angular.module('modesti').service('ValidationService', ValidationService);

function ValidationService($q, $http) {

  // Public API
  var service = {
    validateRequest: validateRequest,
    setErrorMessage: setErrorMessage
  };

  /**
   *
   * @param request
   * @returns {*}
   */
  function validateRequest(request) {
    var q = $q.defer();

    $http.post(BACKEND_BASE_URL + '/requests/' + request.requestId + '/validate').then(function (response) {
      request = response.data;
      q.resolve(request);
    },
    function (error) {
      console.log('error validating request: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  /**
   * Set an error message on a single field of a point.
   *
   * @param point
   * @param propertyName
   * @param message
   */
  function setErrorMessage(point, propertyName, message) {
    var exists = false;

    point.errors.forEach(function (error) {
      if (error.property === propertyName) {
        exists = true;
        if (!error.errors.indexOf(message > -1)) {
          error.errors.push(message);
        }
      }
    });

    if (!exists) {
      point.errors.push({property: propertyName, errors: [message]});
    }
  }

  return service;
}
