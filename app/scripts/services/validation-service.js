'use strict';

/**
 * @ngdoc service
 * @name modesti.ValidationService
 * @description # ValidationService Service in the modesti.
 */
var app = angular.module('modesti');

app.service('ValidationService', function($q, $rootScope, Restangular) {

  var service = {
      
    validateRequest: function(request) {
      $rootScope.validating = "started";
      var q = $q.defer();
      
      Restangular.one('validate/search/plus1BackedByOtherNamedStoredProcedure').get({arg: "2"}).then(function(result) {
        $rootScope.validating = "success";
        q.resolve(result.data);
      },
      
      function(error) {
        console.log('error: ' + error);
        $rootScope.validating = "error";
        q.reject(error);
      });
      
      return q.promise;
    }

  };

  return service;
});