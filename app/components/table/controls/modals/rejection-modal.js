'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CommentsController
 * @description # CommentsController Controller of the modesti
 */
angular.module('modesti').controller('CommentsController', CommentsController);

function CommentsController($modalInstance, selectedPointIds, rows) {
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
