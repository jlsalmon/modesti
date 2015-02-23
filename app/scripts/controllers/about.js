'use strict';

/**
 * @ngdoc function
 * @name yoTestApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the yoTestApp
 */
angular.module('yoTestApp')
  .controller('AboutCtrl', function ($scope) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
