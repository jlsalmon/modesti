'use strict';

/**
 * @ngdoc service
 * @name modesti.SchemaService
 * @description # SchemaService Service in the modesti.
 */
angular.module('modesti').service('SchemaService', SchemaService);

function SchemaService($q, $http) {
  var self = this;

  // Public API
  var service = {
    getSchema: getSchema
  };

  /**
   *
   * @param request
   * @returns {*}
   */
  function getSchema(request) {
    console.log('fetching schema');
    var q = $q.defer();

    $http.get(request._links.schema.href).then(function(response) {
        console.log('fetched schema: ' + response.data.name);
        q.resolve(response.data);
      },

      function(error) {
        console.log('error fetching schema: ' + error);
        q.reject();
      });

    return q.promise;
  }

  return service;
}
