'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TestingControlsController
 * @description # TestingControlsController Controller of the modesti
 */
angular.module('modesti').controller('TestingControlsController', TestingControlsController);

function TestingControlsController($state, RequestService, TaskService) {
  var self = this;

  self.request = {};
  self.tasks = {};

  self.submitting = undefined;
  self.tested = true;

  self.init = init;
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
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    
    var task = self.tasks['test'];
    if (!task) {
      console.log('error testibg request: no task');
      return;
    }

    self.submitting = 'started';

    var testResult;

    if (self.tested) {
      testResult = {
        passed: true,
        errors: []
      };
    } else {
      testResult = {
        passed: false,
        errors: [
          'Point 1 failed test because reasons',
          'Point 2 failed test because reasons'
        ]
      };
    }

    // Send the test result as a JSON string
    var variables = [{
      "name": "testResult",
      "value": JSON.stringify(testResult),
      "type": "string"
    }];

    TaskService.completeTask(task.id, variables).then(function (task) {
        console.log('completed task ' + task.id);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function() {
          self.submitting = 'success';
        });
      },

      function (error) {
        console.log('error completing task ' + task.id);
        self.submitting = 'error';
      });
  }
}