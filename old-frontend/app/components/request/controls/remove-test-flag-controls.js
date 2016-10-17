'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RemoveTestFlagController
 * @description # RemoveTestFlagController
 */
angular.module('modesti').controller('RemoveTestFlagController', RemoveTestFlagController);

function RemoveTestFlagController($scope) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submitting = undefined;

  self.submit = submit;

  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.parent.submit();
  }
}
