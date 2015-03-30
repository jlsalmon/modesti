'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the modesti
 */
angular.module('modesti')
  .controller('AboutCtrl', function ($scope) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
