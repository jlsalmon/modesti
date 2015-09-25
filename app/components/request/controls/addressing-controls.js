'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AddressingController
 * @description # AddressingController Controller of the modesti
 */
angular.module('modesti').controller('AddressingController', AddressingController);

function AddressingController($scope, $state, $modal, $timeout, RequestService, TaskService, ValidationService, AlertService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submitting = undefined;
  self.addressed = true;

  self.init = init;
  self.rejectRequest = rejectRequest;
  self.validate = validate;
  self.submit = submit;

  init();

  /**
   *
   */
  function init() {
    // Register hooks
    self.parent.hot.addHook('afterChange', afterChange);

    // TODO fill the table with empty, read-only rows
    //self.parent.hot.updateSettings( { minSpareRows: 50 } );
  }

  /**
   *
   */
  function rejectRequest(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    // TODO: show comment modal with text box for rejection reason

    self.addressed = false;
  }

  /**
   *
   */
  function validate(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.validating = 'started';
    AlertService.clear();

    $timeout(function () {
      ValidationService.validateRequest(self.parent.request, self.parent.schema).then(function (valid) {
        // Render the table to show the error highlights
        self.parent.hot.render();

        if (!valid) {
          self.validating = 'error';
          AlertService.add('danger', 'Request failed validation with ' + self.parent.getNumValidationErrors() + ' errors');
          return;
        }

        // Validate server-side
        var task = self.parent.tasks.edit;

        if (!task) {
          console.log('warning: no validate task found');
          return;
        }

        // First save the request
        RequestService.saveRequest(self.parent.request).then(function () {
          console.log('saved request before validation');

          // Complete the task associated with the request
          TaskService.completeTask(task.name, self.parent.request.requestId).then(function () {
            console.log('completed task ' + task.name);

            // Clear the cache so that the state reload also pulls a fresh request
            RequestService.clearCache();

            $state.reload().then(function () {
              self.validating = 'success';
              AlertService.add('success', 'Request has been validated successfully');

              // The "edit" task will have changed to "submit"
              TaskService.getTasksForRequest(self.parent.request).then(function (tasks) {
                self.parent.tasks = tasks;
              });
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
      });

    });
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.parent.tasks.submit;
    if (!task) {
      console.log('error addressing request: no task');
      return;
    }

    self.submitting = 'started';

    self.parent.request.addressing = {addressed: self.addressed, message: ''};

    // Save the request
    RequestService.saveRequest(self.parent.request).then(function () {

      TaskService.completeTask(task.name, self.parent.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        var previousStatus = self.parent.request.status;

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          self.submitting = 'success';

          // Show a page with information about what happens next
          $state.go('submitted', {id: self.parent.request.requestId, previousStatus: previousStatus});
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

  /**
   * Called after a change is made to the table (edit, paste, etc.)
   *
   * @param changes a 2D array containing information about each of the edited cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: "alter", "empty", "edit", "populateFromArray", "loadData", "autofill", "paste"
   */
  function afterChange(changes, source) {
    console.log('afterChange()');

    // When the table is initially loaded, this callback is invoked with source == 'loadData'. In that case, we don't
    // want to save the request or send the modification signal.
    if (source === 'loadData') {
      return;
    }

    var change, row, property, oldValue, newValue, dirty = false;
    for (var i = 0, len = changes.length; i < len; i++) {
      change = changes[i];
      row = change[0];
      property = change[1];
      oldValue = change[2];
      newValue = change[3];

      // Mark the point as dirty.
      if (newValue !== oldValue) {
        console.log('dirty point: ' + self.parent.rows[row].id);
        dirty = true;
        self.parent.rows[row].dirty = true;
      }
    }

    // If nothing changed, there's nothing to do! Otherwise, save the request.
    if (dirty) {
      RequestService.saveRequest(self.parent.request).then(function () {
        // If we are in the "submit" stage of the workflow and the form is modified, then it will need to be
        // revalidated. This is done by sending the "requestModified" signal.
        if (self.parent.tasks.submit) {
          sendModificationSignal();
        }
      });
    }
  }

  /**
   * Sends the "requestModified" signal when in the "submit" stage of the workflow in order to force the request
   * back to the "validate" stage.
   */
  function sendModificationSignal() {
    var signal = self.parent.signals.requestModified;

    if (signal) {
      console.log('form modified whilst in submit state: sending signal');

      TaskService.sendSignal(signal).then(function () {
        // The "submit" task will have changed back to "edit".
        TaskService.getTasksForRequest(self.parent.request).then(function (tasks) {
          self.parent.tasks = tasks;
        });
      });
    }
  }
}
