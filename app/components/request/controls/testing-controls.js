'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TestingController
 * @description # TestingController Controller of the modesti
 */
angular.module('modesti').controller('TestingController', TestingController);

function TestingController($scope, $state, RequestService, TaskService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submitting = undefined;
  self.tested = true;

  self.testSelectedPoints = testSelectedPoints;
  self.rejectSelectedPoints = rejectSelectedPoints;
  self.submit = submit;

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

    var task = self.parent.tasks.test;
    if (!task) {
      console.log('error testing request: no task');
      return;
    }

    self.submitting = 'started';

    self.parent.request.testing = {tested: self.tested, message: ''};

    // Save the request
    RequestService.saveRequest(self.parent.request).then(function () {

      TaskService.completeTask(task.name, self.parent.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          self.submitting = 'success';
        });
      },

      function () {
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
