'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CablingControlsController
 * @description # CablingControlsController Controller of the modesti
 */
angular.module('modesti').controller('CablingControlsController', CablingControlsController);

function CablingControlsController($state, RequestService, TaskService) {
  var self = this;

  self.request = {};
  self.tasks = {};

  self.submitting = undefined;

  self.init = init;
  self.isCurrentUserAuthorised = isCurrentUserAuthorised;
  self.isCurrentTaskClaimed = isCurrentTaskClaimed;
  self.isCurrentUserAssigned = isCurrentUserAssigned;
  self.claim = claim;
  self.cableSelectedPoints = cableSelectedPoints;
  self.rejectSelectedPoints = rejectSelectedPoints;
  self.submit = submit;

  /**
   *
   */
  function init(request, tasks) {
    self.request = request;
    self.tasks = tasks;
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAuthorised() {
    return TaskService.isCurrentUserAuthorised(self.tasks['cable']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentTaskClaimed() {
    return TaskService.isTaskClaimed(self.tasks['cable']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAssigned() {
    return TaskService.isCurrentUserAssigned(self.tasks['cable']);
  }

  /**
   *
   */
  function claim() {
    TaskService.claimTask(self.tasks['cable'].name, self.request.requestId).then(function (task) {
      console.log('claimed task successfully');
      self.tasks['cable'] = task;
    });
  }

  /**
   *
   */
  function cableSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
  }

  /**
   *
   */
  function rejectSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks['cable'];
    if (!task) {
      console.log('error cabling request: no task');
      return;
    }

    self.submitting = 'started';

    TaskService.completeTask(task.name, self.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function() {
          self.submitting = 'success';
        });
      },

      function (error) {
        console.log('error completing task ' + task.name);
        self.submitting = 'error';
      });
  }
}