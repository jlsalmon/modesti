'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AlarmApprovalControlsController
 * @description # AlarmApprovalControlsController Controller of the modesti
 */
angular.module('modesti').controller('AlarmApprovalControlsController', AlarmApprovalControlsController);

function AlarmApprovalControlsController($state, TaskService) {
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
  function approveRequest() {
    var request = self.parent.request;
    
    TaskService.getTaskForRequest(request.requestId).then(function(task) {
      TaskService.completeTask(task.id).then(function(task) {
        $state.reload();
      },

      function(error) {
        console.log('error claiming task ' + id);
      });
    },

    function(error) {
      console.log('error querying tasks');
    });
  }
}