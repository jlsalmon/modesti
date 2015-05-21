'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CablingControlsController
 * @description # CablingControlsController Controller of the modesti
 */
angular.module('modesti').controller('CablingControlsController', CablingControlsController);

function CablingControlsController($state, RequestService, TaskService) {
  var self = this;

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

    TaskService.completeTask(task.id, []).then(function (task) {
        console.log('completed task');
        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();
        $state.reload();
      },

      function (error) {
        console.log('error completing task ' + task.id);
      });
  }
}