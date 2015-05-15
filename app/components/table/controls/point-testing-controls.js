'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:PointTestingControlsController
 * @description # PointTestingControlsController Controller of the modesti
 */
angular.module('modesti').controller('PointTestingControlsController', PointTestingControlsController);

function PointTestingControlsController($state, RequestService, TaskService) {
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
  function submit(accepted) {
    var task = self.parent.tasks['test'];
    var variables = [{
      "name" : "accepted",
      "value" : accepted,
      "type" : "boolean"
    }];

    TaskService.completeTask(task.id, variables).then(function(task) {
      console.log('completed task');
      // Clear the cache so that the state reload also pulls a fresh request
      RequestService.clearCache();
      $state.reload();
    },

    function(error) {
      console.log('error completing task ' + task.id);
    });
  }
}