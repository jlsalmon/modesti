'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:ApprovalControlsController
 * @description # ApprovalControlsController Controller of the modesti
 */
angular.module('modesti').controller('ApprovalControlsController', ApprovalControlsController);

function ApprovalControlsController($state, RequestService, TaskService) {
  var self = this;

  self.submitting = undefined;
  self.approved = true;

  self.init = init;
  self.submit = submit;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
  }

  /**
   *
   */
  function submit() {
    var task = self.parent.tasks['approve'];
    if (!task) {
      console.log('error approving request: no task');
      return;
    }

    self.submitting = 'started';

    var approvalResult;

    if (self.approved) {
      approvalResult = {
        approved: true,
        items: [
          {
            id: 1,
            approved: true,
            message: ''
          },
          {
            id: 2,
            approved: true,
            message: ''
          }
        ]
      };
    } else {
      approvalResult = {
        approved: false,
        items: [
          {
            id: 1,
            approved: false,
            message: 'Point 1 is rejected because reasons'
          },
          {
            id: 2,
            approved: false,
            message: 'Point 2 is rejected because reasons'
          }
        ]
      };
    }

    // Send the approval result as a JSON string
    var variables = [{
      "name": "approvalResult",
      "value": JSON.stringify(approvalResult),
      "type": "string"
    }];

    TaskService.completeTask(task.id, variables).then(function (task) {
        console.log('completed task ' + task.id);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function() {
          self.submitting = 'success';
        });
      },

      function (error) {
        console.log('error completing task ' + task.id);
        self.submitting = 'error';
      });
  }
}