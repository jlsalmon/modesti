'use strict';

/**
 * @ngdoc function
 * @name verity.controller:NewRequestController
 * @description # NewRequestController Controller of the verity
 */
var app = angular.module('verity');

app.controller('NewRequestController', function($scope, $http, $location, RequestService) {

  $scope.request = {
    type : 'create',
    description : ''
  };

  $scope.getSystems = function(val) {
    return $http.get('data/systems.json', {
      params : {
        address : val,
        sensor : false
      }
    }).then(function(response) {
      return response.data.systems.map(function(item) {
        return item.name;
      });
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