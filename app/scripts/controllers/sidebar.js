'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:SidebarController
 * @description # SidebarController Controller of the modesti
 */
var app = angular.module('modesti');

app.controller('SidebarController', function($scope, $rootScope, $location) {
  $scope.isActive = function(viewLocation) {
    return $location.path().lastIndexOf(viewLocation, 0) === 0;
  };
  
  $rootScope.$on("server.error.connection", function(status) {
    console.log('caught server.error.connection: ' + status);
    $scope.serverConnectionError = true;
  });
  
  $scope.search = function(q) {
    $location.path('/search/' + q);
  }
});
