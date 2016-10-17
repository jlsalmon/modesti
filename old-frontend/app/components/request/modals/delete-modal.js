'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:DeleteModalController
 * @description # DeleteModalController
 */
angular.module('modesti').controller('DeleteModalController', DeleteModalController);

function DeleteModalController($modalInstance, request) {
  var self = this;

  self.request = request;

  self.ok = ok;
  self.cancel = cancel;

  function ok() {
    $modalInstance.close();
  }

  function cancel() {
    $modalInstance.dismiss();
  }
}
