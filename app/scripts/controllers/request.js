'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController($scope) {

  $scope.tabs = {
    active : 0,
    activate : function(id) {
      this.active = id;
    }
  };
}