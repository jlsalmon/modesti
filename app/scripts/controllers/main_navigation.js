'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:MainNavigationController
 * @description # MainNavigationController Controller of the modesti
 */
var app = angular.module('modesti');

app.controller('MainNavigationController', function($scope, $rootScope, $location) {
  $scope.isActive = function(viewLocation) {
    return $location.path().lastIndexOf(viewLocation, 0) === 0;
  };
  
  $rootScope.$on("server.error.connection", function(status) {
    console.log('caught server.error.connection: ' + status);
    $scope.serverConnectionError = true;
  });
});
