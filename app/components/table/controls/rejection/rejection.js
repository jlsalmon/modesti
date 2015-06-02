'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CommentsController
 * @description # CommentsController Controller of the modesti
 */
angular.module('modesti').controller('CommentsController', CommentsController);

function CommentsController($modalInstance, selectedPointIds, approvalResult) {
  var self = this;

  self.selectedPointIds = selectedPointIds;
  self.approvalResult = approvalResult;

  self.ok = ok;
  self.cancel = cancel;

  function ok() {
    $modalInstance.close(approvalResult);
  }

  function cancel() {
    $modalInstance.dismiss('cancel');
  }
}
