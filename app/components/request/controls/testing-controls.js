'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TestingControlsController
 * @description # TestingControlsController Controller of the modesti
 */
angular.module('modesti').controller('TestingControlsController', TestingControlsController);

function TestingControlsController($state, RequestService, TaskService) {
  var self = this;

  self.parent = {};
  self.request = {};
  self.tasks = {};

  self.submitting = undefined;
  self.tested = true;

  self.init = init;
  self.isCurrentUserAuthorised = isCurrentUserAuthorised;
  self.isCurrentTaskClaimed = isCurrentTaskClaimed;
  self.isCurrentUserAssigned = isCurrentUserAssigned;
  self.getCurrentAssignee = getCurrentAssignee;
  self.claim = claim;
  self.testSelectedPoints = testSelectedPoints;
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
    return TaskService.isCurrentUserAuthorised(self.tasks['test']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentTaskClaimed() {
    return TaskService.isTaskClaimed(self.tasks['test']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAssigned() {
    return TaskService.isCurrentUserAssigned(self.tasks['test']);
  }

  /**
   *
   * @returns {*}
   */
  function getCurrentAssignee() {
    return self.tasks['test'].assignee;
  }

  /**
   *
   */
  function claim() {
    TaskService.claimTask(self.tasks['test'].name, self.request.requestId).then(function (task) {
      console.log('claimed task successfully');
      self.tasks['test'] = task;
      self.parent.activateDefaultCategory();
    });
  }

  /**
   *
   */
  function testSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.tested = true;
  }

  /**
   *
   */
  function rejectSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.tested = false;
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks['test'];
    if (!task) {
      console.log('error testing request: no task');
      return;
    }

    self.submitting = 'started';

    self.request.testing = {tested: self.tested, message: ''};

    // Save the request
    RequestService.saveRequest(self.request).then(function () {

      TaskService.completeTask(task.name, self.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          self.submitting = 'success';
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
