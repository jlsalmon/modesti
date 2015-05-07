'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:LayoutController
 * @description # LayoutController Controller of the modesti
 */
angular.module('modesti').controller('LayoutController', LayoutController);

function LayoutController($scope, $location, $localStorage, $cookies, $modal) {
  var self = this;

  var username = $localStorage.username;
  var authenticated = $localStorage.authenticated;

  self.storage = $localStorage.$default({
    authenticated : authenticated,
    username : username
  });

  self.isActivePage = isActivePage;
  self.search = search;
  self.login = login;
  self.logout = logout;

  /**
   *
   */
  function isActivePage(page) {
    return $location.path().lastIndexOf(page, 0) === 0;
  }

  /**
   *
   */
  function search(q) {
    $location.path('/search/' + q);
  }

  /**
   *
   */
  function login() {
    showLoginModal();
  }

  /**
   *
   */
  function logout() {
    $localStorage.authenticated = false;
    $localStorage.username = undefined;
    delete $cookies["JSESSIONID"];
    $location.path("/");
  }

  /**
   *
   */
  function showLoginModal() {
    $modal.open({
      animation: false,
      keyboard: false,
      backdrop: 'static',
      templateUrl: 'components/login/login-modal.html',
      controller: 'LoginController as ctrl'
    });
  }


  // When an API request returns 401 Unauthorized, angular-http-auth broadcasts
  // this event. We simply catch it and show the login modal.
  $scope.$on('event:auth-loginRequired', function() {
    showLoginModal();
  });
}
