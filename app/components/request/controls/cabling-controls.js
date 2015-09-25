'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CablingController
 * @description # CablingController Controller of the modesti
 */
angular.module('modesti').controller('CablingController', CablingController);

function CablingController($scope, $state, RequestService, TaskService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submitting = undefined;
  self.cabled = true;

  self.cableSelectedPoints = cableSelectedPoints;
  self.rejectSelectedPoints = rejectSelectedPoints;
  self.submit = submit;

  /**
   *
   */
  function cableSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.cabled = true;
  }

  /**
   *
   */
  function rejectSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.cabled = false;
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.parent.tasks.cable;
    if (!task) {
      console.log('error cabling request: no task');
      return;
    }

    self.submitting = 'started';

    self.parent.request.cabling = {cabled: self.cabled, message: ''};

    // Save the request
    RequestService.saveRequest(self.parent.request).then(function () {

      TaskService.completeTask(task.name, self.parent.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        var previousStatus = self.parent.request.status;

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          self.submitting = 'success';

          // Show a page with information about what happens next
          $state.go('submitted', {id: self.parent.request.requestId, previousStatus: previousStatus});
        });
      },

      function () {
        console.log('error completing task ' + task.name);
        self.submitting = 'error';
      });
    },

    function (error) {
      console.log('error saving request ' + task.name + ': ' + error.data.message);
      self.submitting = 'error';
    });
  }
}
