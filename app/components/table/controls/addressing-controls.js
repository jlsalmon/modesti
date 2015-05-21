'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AddressingControlsController
 * @description # AddressingControlsController Controller of the modesti
 */
angular.module('modesti').controller('AddressingControlsController', AddressingControlsController);

function AddressingControlsController($state, RequestService, TaskService) {
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
  function submit(addressed) {
    var task = self.parent.tasks['address'];
    var variables = [{
      "name": "addressed",
      "value": addressed,
      "type": "boolean"
    }];

    TaskService.completeTask(task.id, variables).then(function (task) {
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