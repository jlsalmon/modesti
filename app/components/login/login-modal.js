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
  self.loggingIn = undefined;

  self.login = login;

  /**
   *
   */
  function login() {
    self.loggingIn = 'started';

    AuthService.doLogin(self.credentials).then(function() {
      self.loginError = false;
      self.loggingIn = 'success';

      // Close the modal
      $modalInstance.close();
    },

    function() {
      self.loginError = true;
      self.loggingIn = 'error';
    });
  }
}
