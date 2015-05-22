'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CablingControlsController
 * @description # CablingControlsController Controller of the modesti
 */
angular.module('modesti').controller('CablingControlsController', CablingControlsController);

function CablingControlsController($state, RequestService, TaskService) {
  var self = this;

  self.submitting = undefined;

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
    var task = self.parent.tasks['cable'];
    if (!task) {
      console.log('error cabling request: no task');
      return;
    }

    self.submitting = 'started';

    TaskService.completeTask(task.id, []).then(function (task) {
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