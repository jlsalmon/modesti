'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:ActivityModalController
 * @description # ActivityModalController Controller of the modesti
 */
angular.module('modesti').controller('ActivityModalController', ActivityModalController);

function ActivityModalController($modalInstance, request) {
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
