'use strict';

/**
 * @ngdoc service
 * @name modesti.ValidationService
 * @description # ValidationService Service in the modesti.
 */
angular.module('modesti').service('ValidationService', ValidationService);

function ValidationService() {
  var self = this;

  // Public API
  var service = {
    validateRequest: validateRequest
  };

  function validateRequest(rows, schema, hot) {

  }

  /**
   * Validate that the given value is unique within the given data
   *
   * @param value
   * @param data
   * @returns {boolean}
   */
  function validateUnique(value, data) {
    var index = data.indexOf(value);
    data.splice(index, 1);
    var second_index = data.indexOf(value);
    return !(index > -1 && second_index > -1);
  }

  return service;
}
