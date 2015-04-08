'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
var app = angular.module('modesti');

app.controller('RequestController', function($scope) {

  $scope.tabs = {
    active : 0,
    activate : function(id) {
      this.active = id;
    }
  };

});
