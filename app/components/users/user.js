'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:UserController
 * @description # UserController
 */
angular.module('modesti').controller('UserController', UserController);

function UserController(user) {
  var self = this;

  self.user = user;
}
