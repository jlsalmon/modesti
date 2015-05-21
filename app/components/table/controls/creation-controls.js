'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CreationControlsController
 * @description # CreationControlsController Controller of the modesti
 */
angular.module('modesti').controller('CreationControlsController', CreationControlsController);

function CreationControlsController($http, $state, RequestService, TaskService) {
  var self = this;

  self.validating = undefined;
  self.submitting = undefined;
  self.splitting = undefined;

  self.init = init;
  self.addRow = addRow;
  self.duplicateSelectedRows = duplicateSelectedRows;
  self.deleteSelectedRows = deleteSelectedRows;
  self.validate = validate;
  self.submit = submit;
  self.split = split;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
  }

  /**
   *
   */
  function addRow() {
    console.log('adding new row');
    var request = self.parent.request;

    var newRow = {
      'name': '',
      'description': '',
      'domain': request.domain
    };

    request.points.push(newRow);

    RequestService.saveRequest(request).then(function (request) {
      console.log('added new row');
      self.parent.request = request;

      // Reload the table data
      self.parent.tableParams.reload();

      // Move to the last page
      var pages = self.parent.tableParams.settings().$scope.pages;
      for (var i in pages) {
        if (pages[i].type == "last") {
          self.parent.tableParams.page(pages[i].number);
        }
      }

    }, function (error) {
      console.log('error adding new row: ' + error);
    });
  }

  /**
   *
   */
  function duplicateSelectedRows() {
    var points = self.parent.request.points;
    console.log('duplicating rows (before: ' + points.length + ' points)');

    // Find the selected points and duplicate them
    for (var i in points) {
      var point = points[i];

      if (self.parent.checkboxes.items[point.id]) {
        var duplicate = angular.copy(point);
        // Remove the ID of the duplicated point, as the backend will generate
        // us a new one when we save
        delete duplicate.id;
        // Add the new duplicate to the original points
        points.push(duplicate);
      }
    }

    // Save the changes
    RequestService.saveRequest(self.parent.request).then(function (savedRequest) {
      console.log('saved request after row duplication');
      console.log('duplicated rows (after: ' + savedRequest.points.length + ' points)');

      // Reload the table data
      self.parent.tableParams.reload();

    }, function (error) {
      console.log('error saving request after row duplication: ' + error);
    });
  }

  /**
   *
   */
  function deleteSelectedRows() {
    var points = self.parent.request.points;
    console.log('deleting rows (before: ' + points.length + ' points)');

    // Find the selected points and mark them as deleted
    for (var i in points) {
      var point = points[i];

      if (self.parent.checkboxes.items[point.id]) {
        point.deleted = true;
      }
    }

    // Save the changes
    RequestService.saveRequest(self.parent.request).then(function (savedRequest) {
      console.log('saved request after row deletion');
      console.log('deleted rows (after: ' + savedRequest.points.length + ' points)');

      // Reload the table data
      self.parent.tableParams.reload();

    }, function (error) {
      console.log('error saving request after row deletion: ' + error);
    });
  }

  /**
   *
   */
  function validate() {
    var task = self.parent.tasks['validate'];

    if (!task) {
      console.log('warning: no validate task found');
      return;
    }

    self.validating = 'started';

    // Complete the task associated with the request
    TaskService.completeTask(task.id).then(function (task) {
        console.log('completed task ' + task.id);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function() {
          self.validating = 'success';
        });
      },

      function (error) {
        console.log('error completing task: ' + error);
        self.validating = 'error';
      });
  }

  /**
   *
   */
  function submit() {
    var task = self.parent.tasks['submit'];

    if (!task) {
      console.log('warning: no submit task found');
      return;
    }

    self.submitting = 'started';

    // Complete the task associated with the request
    TaskService.completeTask(task.id).then(function (task) {
        console.log('completed task ' + task.id);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function() {
          self.submitting = 'success';
        });
      },

      function (error) {
        console.log('error completing task: ' + error);
        self.submitting = 'error';
      });
  }

  /**
   * TODO split only selected points
   */
  function split() {
    var task = self.parent.tasks['validate'];
    if (!task) {
      console.log('error splitting request: no task');
      return;
    }

    self.splitting = 'started';

    var url = task.executionUrl;
    var variables = [{
      "name": "points",
      "value": JSON.stringify([1, 2, 3]),
      "type": "string"
    }];

    var params = {
      "action": "signalEventReceived",
      "signalName": "splitRequest",
      "variables": variables
    };

    // TODO refactor this into a service
    $http.put(url, params).then(function () {
        console.log('sent split signal');

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function() {
          self.splitting = 'success';
        });
      },

      function (error) {
        console.log('error completing task: ' + error);
        self.splitting = 'error';
      });
  }
}