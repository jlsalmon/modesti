'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:LoginController
 * @description # LoginController Controller of the modesti
 */
angular.module('modesti').controller('LoginController', LoginController);

function LoginController($http, $localStorage, $modalInstance, authService) {
  var self = this;

  self.credentials = {};
  self.storage = $localStorage.$default();

  self.login = login;

  /**
   *
   */
  function login() {

    // Build a basic auth header
    var headers = self.credentials ? {
      authorization: "Basic " + btoa(self.credentials.username + ":" + self.credentials.password)
    } : {};

    // Set ignoreAuthModule so that angular-http-auth doesn't show another modal
    // if the authentication fails
    $http.get(BACKEND_BASE_URL + '/login', {headers: headers, ignoreAuthModule: true}).success(function (data) {
      console.log('authenticated');

      // Set data in local storage for other parts of the app to use
      $localStorage.user = data;
      self.loginError = false;

      // Confirm the login, so that angular-http-auth can resume any ajax requests
      // that were suspended due to 401s
      authService.loginConfirmed();

      // Close the modal
      $modalInstance.close();

    }).error(function () {
      console.log('failed to authenticate');

      $localStorage.user = undefined;
      self.loginError = true;
    });
  }
}
