'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:LayoutController
 * @description # LayoutController
 */
angular.module('modesti').controller('LayoutController', LayoutController);

function LayoutController($scope, $rootScope, $location, $translate, AuthService) {
  var self = this;

  self.user = AuthService.getCurrentUser();

  self.isActivePage = isActivePage;
  self.getCurrentLanguage = getCurrentLanguage;
  self.changeLanguage = changeLanguage;
  self.search = search;
  self.isAuthenticated = isAuthenticated;
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
    $rootScope.$broadcast('event:languageChanged');
  }

  /**
   *
   */
  function search(q) {
    $location.path('/search/' + q);
  }

  /**
   *
   * @returns {boolean}
   */
  function isAuthenticated() {
    return AuthService.isCurrentUserAuthenticated();
  }

  /**
   *
   */
  function login() {
    AuthService.login().then(function (user) {
      self.user = user;
    });
  }

  /**
   *
   */
  function logout() {
    AuthService.logout().then(function () {
      $location.path('/');
    });
  }

  // When an API request returns 401 Unauthorized, angular-http-auth broadcasts
  // this event. We simply catch it and show the login modal.
  $scope.$on('event:auth-loginRequired', function () {
    login();
  });
}

// TODO move this to a separate file
angular.module('modesti').directive('title', ['$rootScope', '$timeout', '$translate',
  function ($rootScope, $timeout, $translate) {
    return {
      controller: function () {
        var self = this;

        var listener = function (event, toState) {

          //$timeout(function () {
          if (toState.data && toState.data.pageTitle) {
            self.title = toState.data.pageTitle;
            setTitle(self.title);
            //$translate(toState.data.pageTitle).then(function(title) {
            //  $rootScope.title = 'modesti · ' + title;
            //});
          }
          //});
        };

        $rootScope.$on('$stateChangeSuccess', listener);

        /**
         * When the global language is changed, this event will be fired. We catch it here and
         * update the columns to make sure the help text etc. is in the right language.
         */
        $rootScope.$on('event:languageChanged', function () {
          setTitle(self.title);
        });

        /**
         *
         * @param title
         */
        function setTitle(title) {

          $translate(title).then(function (translated) {
            $timeout(function () {
              $rootScope.title = 'modesti · ' + translated;
            });
          });

        }
      }
    };
  }
]);
