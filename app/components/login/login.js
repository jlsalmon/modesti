'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:LoginController
 * @description # LoginController Controller of the modesti
 */
angular.module('modesti').controller('LoginController', LoginController);

function LoginController($http, $rootScope, $location) {
  var self = this;

  self.credentials = {};

  self.login  = login;
  self.logout = logout;

  authenticate();

  /**
   *
   */
  function login() {
    authenticate(self.credentials, function () {
      if ($rootScope.authenticated) {
        $location.path("/");
        self.error = false;
      } else {
        $location.path("/login");
        self.error = true;
      }
    });
  }

  /**
   *
   */
  function logout() {
    $http.post('http://localhost:8080/logout', {}).success(function() {
      $rootScope.authenticated = false;
      $rootScope.username = undefined;
      $location.path("/");
    }).error(function(data) {
      $rootScope.authenticated = false;
      $rootScope.username = undefined;
    });
  }

  /**
   *
   * @param credentials
   * @param callback
   */
  function authenticate(credentials, callback) {
    $rootScope.authorization = credentials ? "Basic " + btoa(credentials.username + ":" + credentials.password) : '';

    var headers = credentials ? {
      authorization: $rootScope.authorization
    } : {};


    $http.get('http://localhost:8080/user', {headers: headers, withCredentials: true}).success(function (data) {
      if (data.name) {
        console.log('authenticated');
        $rootScope.authenticated = true;
        $rootScope.username = data.name;
      } else {
        console.log('failed to authenticate');
        $rootScope.authenticated = false;
        $rootScope.username = undefined;
      }
      callback && callback();
    }).error(function () {
      console.log('failed to authenticate');
      $rootScope.authenticated = false;
      $rootScope.username = undefined;
      callback && callback();
    });
  }
}
