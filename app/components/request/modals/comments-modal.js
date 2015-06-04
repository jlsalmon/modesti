'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CommentsModalController
 * @description # CommentsModalController Controller of the modesti
 */
angular.module('modesti').controller('CommentsModalController', CommentsModalController);

function CommentsModalController($modalInstance, request) {
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
