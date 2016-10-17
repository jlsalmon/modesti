'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RejectionModalController
 * @description # RejectionModalController
 */
angular.module('modesti').controller('RejectionModalController', RejectionModalController);

function RejectionModalController($modalInstance, request) {
  var self = this;

  self.request = request;

  self.ok = ok;
  self.cancel = cancel;

  function ok() {
    $modalInstance.close();
  }

  function cancel() {
    $modalInstance.dismiss('cancel');
  }
}
