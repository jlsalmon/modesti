'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestSubmittedController
 * @description # RequestSubmittedController
 */
angular.module('modesti').controller('RequestSubmittedController', RequestSubmittedController);

function RequestSubmittedController($stateParams, request, RequestService, AuthService) {
  var self = this;

  self.request = request;
  self.previousStatus = $stateParams.previousStatus;

  self.isCurrentUserOwner = isCurrentUserOwner;
  self.isCurrentUserAdministrator = isCurrentUserAdministrator;

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserOwner() {
    return RequestService.isCurrentUserOwner(self.request);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAdministrator() {
    return AuthService.isCurrentUserAdministrator();
  }
}