'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:LoginModalController
 * @description # LoginModalController Controller of the modesti
 */
angular.module('modesti').controller('LoginModalController', LoginModalController);

function LoginModalController($modalInstance, AuthService) {
  var self = this;

  self.credentials = {};

  self.login = login;

  /**
   *
   */
  function login() {

    AuthService.doLogin(self.credentials).then(function() {
      self.loginError = false;

      // Close the modal
      $modalInstance.close();
    },

    function(error) {
      self.loginError = true;
    });
  }
}
