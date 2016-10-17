'use strict';

angular.module('modesti').controller('CsamSyncController', CsamSyncController);

function CsamSyncController($scope, $state, RequestService, AlertService, TaskService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submit = submit;

  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.parent.tasks[Object.keys(self.parent.tasks)[0]];

    AlertService.clear();
    self.submitting = 'started';

    RequestService.saveRequest(self.parent.request).then(function () {
      console.log('saved request before submitting');

      // Complete the task associated with the request
      TaskService.completeTask(task.name, self.parent.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        $state.reload().then(function () {

          if (self.parent.request.status === 'FOR_CSAM_SYNC') {
            self.submitting = 'error';
            AlertService.add('danger', 'Synchronisation failed See the error log for details.');
          } else {
            self.submitting = 'success';
            AlertService.add('success', 'Synchronisation completed successfully.');
          }
        });
      },

      function (error) {
        console.log('error completing task: ' + error.statusText);
        self.submitting = 'error';
      });
    },

    function (error) {
      console.log('error completing task: ' + error.statusText);
      self.submitting = 'error';
    });
  }
}
