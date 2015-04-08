'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:NewRequestController
 * @description # NewRequestController Controller of the modesti
 */
var app = angular.module('modesti');

app.controller('NewRequestController', function($scope, $http, $location, $filter, RequestService) {

  $scope.request = {
    type : 'create',
    description : ''
  };

  $scope.getSystems = function(value) {
    return $http.get('data/systems.json', {
      params : {}
    }).then(function(response) {
      return $filter('filter')(response.data, value);
    });
  };

  $scope.submit = function(form, request) {
    console.log(form);
    if (form.$invalid) {
      console.log('form invalid');
      return;
    }

    else {
      console.log('form valid');

      // Post form to server to create new request.
      RequestService.createRequest(request).then(function(location) {
        // Strip request ID from location.
        var id = location.substring(location.lastIndexOf('/') + 1);
        // Redirect to point entry page.
        $location.path("/requests/" + id);
      },

      function(error) {
        // what do do here?
      });

    }
  };
});