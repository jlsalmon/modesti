'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AlarmApprovalControlsController
 * @description # AlarmApprovalControlsController Controller of the modesti
 */
angular.module('modesti').controller('AlarmApprovalControlsController', AlarmApprovalControlsController);

function AlarmApprovalControlsController($state, RequestService, TaskService) {
  var self = this;

  self.init = init;
  self.approveRequest = approveRequest;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
  }

  /**
   *
   */
  function approveRequest(approved) {
    var task = self.parent.tasks['approve'];
    var variables = [{
      "name" : "approved",
      "value" : approved,
      "type" : "boolean"
    }];

    TaskService.completeTask(task.id, variables).then(function(task) {
      console.log('completed task ' + task.id);
      // Clear the cache so that the state reload also pulls a fresh request
      RequestService.clearCache();
      $state.reload();
    },

    function(error) {
      console.log('error completing task ' + task.id);
    });

  }
}