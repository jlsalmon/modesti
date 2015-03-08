'use strict';

/**
 * @ngdoc function
 * @name verity.controller:NewRequestController
 * @description # NewRequestController Controller of the verity
 */
var app = angular.module('verity');

app.controller('NewRequestController', function($scope, $http) {
  $scope.text = 'text';
  
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
});