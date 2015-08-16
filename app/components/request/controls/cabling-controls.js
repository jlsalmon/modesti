'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CablingControlsController
 * @description # CablingControlsController Controller of the modesti
 */
angular.module('modesti').controller('CablingControlsController', CablingControlsController);

function CablingControlsController($state, RequestService, TaskService) {
  var self = this;

  self.parent = {};
  self.request = {};
  self.tasks = {};

  self.submitting = undefined;
  self.cabled = true;

  self.init = init;
  self.isCurrentUserAuthorised = isCurrentUserAuthorised;
  self.isCurrentTaskClaimed = isCurrentTaskClaimed;
  self.isCurrentUserAssigned = isCurrentUserAssigned;
  self.getCurrentAssignee = getCurrentAssignee;
  self.claim = claim;
  self.cableSelectedPoints = cableSelectedPoints;
  self.rejectSelectedPoints = rejectSelectedPoints;
  self.submit = submit;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
    self.request = parent.request;
    self.tasks = parent.tasks;
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
   * @returns {*}
   */
  function getCurrentAssignee() {
    return self.tasks['cable'].assignee;
  }

  /**
   *
   */
  function claim() {
    TaskService.claimTask(self.tasks['cable'].name, self.request.requestId).then(function (task) {
      console.log('claimed task successfully');
      self.tasks['cable'] = task;
      self.parent.activateDefaultCategory();
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

    self.cabled = true;
  }

  /**
   *
   */
  function rejectSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.cabled = false;
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

    self.request.cabling = {cabled: self.cabled, message: ''};

    // Save the request
    RequestService.saveRequest(self.request).then(function () {

      TaskService.completeTask(task.name, self.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        var previousStatus = self.request.status;

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          self.submitting = 'success';

          // Show a page with information about what happens next
          $state.go('submitted', {id: self.request.requestId, previousStatus: previousStatus});
        });
      },

      function (error) {
        console.log('error completing task ' + task.name);
        self.submitting = 'error';
      });
    },

    function (error) {
      console.log('error saving request ' + task.name + ': ' + error.data.message);
      self.submitting = 'error';
    });
  }
}
