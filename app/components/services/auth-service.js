'use strict';

/**
 * @ngdoc service
 * @name modesti.AuthService
 * @description # AuthService Service in the modesti.
 */
angular.module('modesti').service('AuthService', AuthService);

function AuthService($http, $q, $localStorage, $cookies, $modal, authService) {
  var self = this;

  self.loginModalOpened = false;

  /**
   * Public API for the auth service.
   */
  var service = {
    login: login,
    doLogin: doLogin,
    logout: logout,
    getCurrentUser: getCurrentUser,
    isCurrentUserAuthenticated: isCurrentUserAuthenticated
  };

  /**
   *
   * @returns {*}
   */
  function login() {
    var q = $q.defer();

    if (self.loginModalOpened) {
      return $q.when();
    }

    self.loginModalOpened = true;

    var modalInstance = $modal.open({
      animation: false,
      keyboard: false,
      backdrop: 'static',
      templateUrl: 'components/login/login-modal.html',
      controller: 'LoginModalController as ctrl'
    });

    modalInstance.result.then(function () {
      self.loginModalOpened = false;
      q.resolve($localStorage.user);
    });

    return q.promise;
  }

  /**
   *
   * @param credentials
   * @returns {*}
   */
  function doLogin(credentials) {
    var q = $q.defer();

    // Build a basic auth header
    var headers = credentials ? {
      authorization: "Basic " + btoa(credentials.username + ":" + credentials.password)
    } : {};

    // Set ignoreAuthModule so that angular-http-auth doesn't show another modal
    // if the authentication fails
    $http.get(BACKEND_BASE_URL + '/login', {headers: headers, ignoreAuthModule: true}).then(function (response) {
      console.log('authenticated');

      // Set data in local storage for other parts of the app to use
      $localStorage.user = response.data;

      // Confirm the login, so that angular-http-auth can resume any ajax requests
      // that were suspended due to 401s
      authService.loginConfirmed();

      q.resolve();
    },

    function (error) {
      console.log('failed to authenticate');
      $localStorage.user = undefined;
      q.reject(error);
    });

    return q.promise;
  }

  /**
   *
   * @returns {*}
   */
  function logout() {
    $localStorage.user = undefined;
    delete $cookies["JSESSIONID"];
    return $q.when();
  }

  /**
   *
   * @returns {T|*}
   */
  function getCurrentUser() {
    return $localStorage.user;
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAuthenticated() {
    return $localStorage.user !== undefined;
  }

  return service;
}