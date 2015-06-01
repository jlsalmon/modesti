'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CreationControlsController
 * @description # CreationControlsController Controller of the modesti
 */
angular.module('modesti').controller('CreationControlsController', CreationControlsController);

function CreationControlsController($http, $state, RequestService, TaskService) {
  var self = this;

  self.parent = {};
  self.request = {};
  self.tasks = {};
  self.hot = {};

  self.selection = [];

  self.validating = undefined;
  self.submitting = undefined;
  self.splitting = undefined;

  self.init = init;
  self.validate = validate;
  self.submit = submit;
  self.split = split;
  self.canValidate = canValidate;
  self.canSubmit = canSubmit;
  self.canSplit = canSplit;

  /**
   *
   */
  function init(request, tasks, parent) {
    self.request = request;
    self.tasks = tasks;
    self.hot = parent.hot;
    self.parent = parent;

    // Register the afterChange() hook so that we can use it to send a signal to the backend if we are in 'submit'
    // state and the user makes a modification
    parent.hot.addHook('afterChange', afterChange);

    // Register the afterSelectionEnd() hook so that we can get the selected rows for splitting
    parent.hot.addHook('afterSelectionEnd', afterSelectionEnd);
  }

  /**
   *
   */
  function canValidate() {
    var task = self.tasks['validate'];
    // TODO reimplement this
    return task; //task && self.tableForm.$valid;
  }

  /**
   *
   */
  function canSubmit() {
    return self.tasks['submit'];
  }

  /**
   *
   */
  function canSplit() {
    var selection = self.hot.getSelected();
    return selection && selection.length > 0;
  }

  /**
   *
   */
  function validate() {
    var task = self.tasks['validate'];

    if (!task) {
      console.log('warning: no validate task found');
      return;
    }

    self.validating = 'started';

    // First save the request
    RequestService.saveRequest(self.request).then(function () {
        console.log('saved request before validation');

        // Complete the task associated with the request
        TaskService.completeTask(task.id).then(function (task) {
            console.log('completed task ' + task.id);

            // Clear the cache so that the state reload also pulls a fresh request
            RequestService.clearCache();

            $state.reload().then(function () {
              self.validating = 'success';
            });
          },

          function (error) {
            console.log('error completing task: ' + error.statusText);
            self.validating = 'error';
          });
      },

      function (error) {
        console.log('error saving before validation: ' + error.statusText);
        self.validating = 'error';
      });
  }

  /**
   *
   */
  function submit() {
    var task = self.tasks['submit'];

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

        $state.reload().then(function () {
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
    var task = self.tasks['validate'];
    if (!task) {
      console.log('error splitting request: no task');
      return;
    }

    self.splitting = 'started';

    var checkboxes = self.hot.getDataAtCol(self.parent.columns.length - 1);
    var pointIds = [];

    for (var i = 0, len = checkboxes.length; i < len; i++) {
      if (checkboxes[i]) {
        // Point IDs are 1-based
        pointIds.push(i + 1);
      }
    }

    if (!pointIds.length) {
      return;
    }

    console.log('splitting points: ' + pointIds);

    var url = task.executionUrl;
    var variables = [{
      "name": "points",
      "value": JSON.stringify(pointIds),
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

        $state.reload().then(function () {
          self.splitting = 'success';
        });
      },

      function (error) {
        console.log('error sending signal: ' + error);
        self.splitting = 'error';
      });
  }

  /**
   *
   * @param startRow
   * @param startCol
   * @param endRow
   * @param endCol
   */
  function afterSelectionEnd(startRow, startCol, endRow, endCol) {
    self.selection = [startRow, startCol, endRow, endCol];
  }

  /**
   * Watch the outer table for changes. If we are in the "submit" stage of the workflow and the form is modified,
   * then it will need to be revalidated. This is done by sending the "requestModified" signal.
   */
  function afterChange() {
    // Save the request
    RequestService.saveRequest(self.request).then(function() {
      console.log('afterChange()');
      var task = self.tasks['submit'];

      if (task) {
        console.log('form modified whilst in submit state: sending signal');

        var url = task.executionUrl;
        var params = {
          "action": "signalEventReceived",
          "signalName": "requestModified",
          "variables": []
        };

        // TODO refactor this into a service
        $http.put(url, params).then(function () {
            console.log('sent modification signal');

            // The "submit" task will have changed to "validate".
            TaskService.queryTasksForRequest(self.request).then(function (tasks) {
              self.tasks = tasks;
            });
          },

          function (error) {
            console.log('error sending signal: ' + error);
          });
      }
    });
  }
}