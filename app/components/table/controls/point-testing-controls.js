'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:PointTestingControlsController
 * @description # PointTestingControlsController Controller of the modesti
 */
angular.module('modesti').controller('PointTestingControlsController', PointTestingControlsController);

function PointTestingControlsController($window, Restangular, TaskService) {
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
    var request = self.parent.request;
    
    TaskService.getTaskForRequest(request.requestId).then(function(task) {
      TaskService.completeTask(task.id).then(function(task) {
        $window.location.reload(true);
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