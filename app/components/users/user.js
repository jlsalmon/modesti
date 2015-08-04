'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:UserController
 * @description # UserController Controller of the modesti
 */
angular.module('modesti').controller('UserController', UserController);

function UserController($stateParams) {
  var self = this;

  self.user = $stateParams.id;
}
