'use strict';

angular.module('modesti').controller('WinCCOAConfigController', WinCCOAConfigController);

function WinCCOAConfigController($scope) {
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
