'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:LayoutController
 * @description # LayoutController Controller of the modesti
 */
angular.module('modesti').controller('LayoutController', LayoutController);

function LayoutController($scope, $rootScope, $location, $translate, $localStorage, $cookies, $modal) {
  var self = this;

  var user = $localStorage.user;

  self.storage = $localStorage.$default({
    user : user
  });

  self.isActivePage = isActivePage;
  self.getCurrentLanguage = getCurrentLanguage;
  self.changeLanguage = changeLanguage;
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
   * @returns {*}
   */
  function getCurrentLanguage() {
    return $translate.use();
  }

  /**
   *
   * @param language
   * @returns {*}
   */
  function changeLanguage(language) {
    $translate.use(language);
    // Broadcast that the language changed for any parts of the application
    // that may be interested
    $rootScope.$broadcast('event:languageChanged')
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
    $localStorage.user = undefined;
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
