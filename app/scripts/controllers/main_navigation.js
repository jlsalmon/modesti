'use strict';

/**
 * @ngdoc function
 * @name verity.controller:MainNavigationController
 * @description # MainNavigationController Controller of the verity
 */
var app = angular.module('verity');

app.controller('MainNavigationController', function($scope, $location) {
  $scope.isActive = function(viewLocation) {
    return $location.path().lastIndexOf(viewLocation, 0) === 0;
  };
});
