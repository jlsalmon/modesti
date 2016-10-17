'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:InsertionController
 * @description # InsertionController
 */
angular.module('modesti').controller('InsertionController', InsertionController);

function InsertionController($scope, $state, $http, $timeout, RequestService, TaskService, AlertService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.inserting = undefined;

  self.insert = insert;

  /**
   *
   */
  function insert(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.parent.tasks.insert;
    if (!task) {
      console.log('error inserting request: no task');
      return;
    }

    self.inserting = 'started';

    // Complete the task
    TaskService.completeTask(task.name, self.parent.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function() {
          // Get the request once again from the cache
          RequestService.getRequest(self.parent.request.requestId).then(function (request) {
            self.parent.request = request;

            var insertionResult = self.parent.request.properties.insertionResult;

            // Show an alert if the configuration failed.
            if (!insertionResult || insertionResult.success === false) {
              self.inserting = 'error';
              AlertService.add('danger', 'Insertion failed, please see error log for details.');
            }
            else {
              self.inserting = 'success';
              AlertService.add('success', 'Request has been inserted successfully into the database.');
            }
          });
        });
      },

      function () {
        console.log('error completing task ' + task.name);
        self.inserting = 'error';
      });
  }
}
