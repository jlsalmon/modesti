'use strict';

angular.module('modesti').controller('CsamSyncController', CsamSyncController);

function CsamSyncController($scope) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submit = submit;

  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.parent.submit();
  }
}
