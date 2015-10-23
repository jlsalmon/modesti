'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CloneModalController
 * @description # CloneModalController
 */
angular.module('modesti').controller('CloneModalController', CloneModalController);

function CloneModalController($modalInstance, request) {
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
