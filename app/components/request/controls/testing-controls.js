'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TestingControlsController
 * @description # TestingControlsController Controller of the modesti
 */
angular.module('modesti').controller('TestingControlsController', TestingControlsController);

function TestingControlsController($state, RequestService, TaskService) {
  var self = this;

  self.parent = {}
  self.request = {};
  self.tasks = {};

  self.submitting = undefined;
  self.tested = true;

  self.init = init;
  self.isCurrentUserAuthorised = isCurrentUserAuthorised;
  self.isCurrentTaskClaimed = isCurrentTaskClaimed;
  self.isCurrentUserAssigned = isCurrentUserAssigned;
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

    var task = self.tasks['test'];
    if (!task) {
      console.log('error testing request: no task');
      return;
    }

    self.submitting = 'started';

    //var testResult;
    //
    //if (self.tested) {
    //  testResult = {
    //    passed: true,
    //    errors: []
    //  };
    //} else {
    //  testResult = {
    //    passed: false,
    //    errors: [
    //      'Point 1 failed test because reasons',
    //      'Point 2 failed test because reasons'
    //    ]
    //  };
    //}
    //
    //// Send the test result as a JSON string
    //var variables = [{
    //  "name": "testResult",
    //  "value": JSON.stringify(testResult),
    //  "type": "string"
    //}];

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