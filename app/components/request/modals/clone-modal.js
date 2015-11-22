'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CloneModalController
 * @description # CloneModalController
 */
angular.module('modesti').controller('CloneModalController', CloneModalController);

function CloneModalController($modalInstance, $state, request, RequestService, AlertService) {
  var self = this;

  self.request = request;
  self.cloning = undefined;

  self.clone  = clone;
  self.cancel = cancel;

  function clone() {
    self.cloning = 'started';

    RequestService.cloneRequest(request).then(function (location) {
      // Strip request ID from location
      var id = location.substring(location.lastIndexOf('/') + 1);
      console.log('cloned request ' + request.requestId + ' to new request ' + id);

      $state.go('request', {id: id}).then(function () {
        self.cloning = 'success';
        $modalInstance.close();
        AlertService.add('success', 'Request was cloned successfully with id ' + id);
      });
    },

    function (error) {
      console.log('clone failed: ' + error.statusText);
      self.cloning = 'error';
    });
  }

  function cancel() {
    $modalInstance.dismiss();
  }
}
