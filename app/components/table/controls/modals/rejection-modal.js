'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RejectionModalController
 * @description # RejectionModalController Controller of the modesti
 */
angular.module('modesti').controller('RejectionModalController', RejectionModalController);

function RejectionModalController($modalInstance, selectedPointIds, rows) {
  var self = this;

  self.selectedPointIds = selectedPointIds;
  self.rows = rows;

  self.ok = ok;
  self.cancel = cancel;

  function ok() {
    $modalInstance.close();
  }

  function cancel() {
    $modalInstance.dismiss('cancel');
  }
}
