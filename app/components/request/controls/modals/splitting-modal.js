'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:SplittingModalController
 * @description # SplittingModalController
 */
angular.module('modesti').controller('SplittingModalController', SplittingModalController);

function SplittingModalController($modalInstance, selectedPointIds, rows) {
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
