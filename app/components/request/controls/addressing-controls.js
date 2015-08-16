'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AddressingControlsController
 * @description # AddressingControlsController Controller of the modesti
 */
angular.module('modesti').controller('AddressingControlsController', AddressingControlsController);

function AddressingControlsController($state, $modal, $timeout, RequestService, TaskService, ValidationService, AlertService) {
  var self = this;

  self.parent = {};
  self.request = {};
  self.rows = {};
  self.tasks = {};
  self.signals = {};
  self.hot = {};

  self.submitting = undefined;
  self.addressed = true;

  self.init = init;
  self.isCurrentUserAuthorised = isCurrentUserAuthorised;
  self.isCurrentTaskClaimed = isCurrentTaskClaimed;
  self.isCurrentUserAssigned = isCurrentUserAssigned;
  self.getCurrentAssignee = getCurrentAssignee;
  self.claim = claim;
  self.rejectRequest = rejectRequest;
  self.canValidate = canValidate;
  self.canSubmit = canSubmit;
  self.validate = validate;
  self.submit = submit;
  self.getNumValidationErrors = getNumValidationErrors;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
    self.request = parent.request;
    self.rows = parent.rows;
    self.tasks = parent.tasks;
    self.signals = parent.signals;
    self.hot = parent.hot;

    // Make sure that only those column groups which match the point type are editable.
    self.parent.hot.updateSettings( {
      cells: function (row, col, prop) {
        var cellProperties = {};
        var pointType = self.parent.hot.getDataAtRowProp(row, 'properties.pointType');

        if (pointType !== self.parent.activeCategory.name) {
          cellProperties.readOnly = true;
        }

        // We want the "pointType" field to be more distinct when it matches the active category, so we set it to
        // non-editable rather than read-only (see http://docs.handsontable.com/0.16.1/demo-disable-cell-editing.html)
        // for the difference)
        if (pointType === self.parent.activeCategory.name && prop === 'properties.pointType') {
          cellProperties.editor = false;
        }

        return cellProperties;
      }
    });

    // Register hooks
    self.parent.hot.addHook('afterChange', afterChange);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAuthorised() {
    var task = self.tasks['edit'] ? self.tasks['edit'] : self.tasks['submit'];
    return TaskService.isCurrentUserAuthorised(task);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentTaskClaimed() {
    var task = self.tasks['edit'] ? self.tasks['edit'] : self.tasks['submit'];
    return TaskService.isTaskClaimed(task);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAssigned() {
    var task = self.tasks['edit'] ? self.tasks['edit'] : self.tasks['submit'];
    return TaskService.isCurrentUserAssigned(task);
  }

  /**
   *
   * @returns {*}
   */
  function getCurrentAssignee() {
    var task = self.tasks['edit'] ? self.tasks['edit'] : self.tasks['submit'];
    return task.assignee;
  }

  /**
   *
   * @returns {number}
   */
  function getNumValidationErrors() {
    var n = 0;

    for (var i in self.rows) {
      var point = self.rows[i];

      for (var j in point.errors) {
        n += point.errors[j].errors.length;
      }
    }

    return n;
  }

  /**
   *
   */
  function claim(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var taskName = self.tasks['edit'] ? self.tasks['edit'].name : self.tasks['submit'].name;

    TaskService.claimTask(taskName, self.request.requestId).then(function (task) {
      console.log('claimed task successfully');
      self.tasks[taskName] = task;
      self.parent.activateDefaultCategory();
    });
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
   * @returns {*}
   */
  function canValidate() {
    return self.tasks['edit'];
  }

  /**
   * The addressing can be submitted if all points that require addresses have them.
   **
   * TODO: only show those points which require addresses to the addresser
   *
   * @returns {boolean}
   */
  function canSubmit() {
    return self.tasks['submit'];
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
      ValidationService.validateRequest(self.request, self.parent.schema).then(function (valid) {
        // Render the table to show the error highlights
        self.hot.render();

        if (!valid) {
          self.validating = 'error';
          return;
        }

        // Validate server-side
        var task = self.tasks['edit'];

        if (!task) {
          console.log('warning: no validate task found');
          return;
        }

        // First save the request
        RequestService.saveRequest(self.request).then(function () {
          console.log('saved request before validation');

          // Complete the task associated with the request
          TaskService.completeTask(task.name, self.request.requestId).then(function () {
            console.log('completed task ' + task.name);

            // Clear the cache so that the state reload also pulls a fresh request
            RequestService.clearCache();

            $state.reload().then(function () {
              self.validating = 'success';
              AlertService.add('success', 'Request has been validated successfully');

              // The "edit" task will have changed to "submit"
              TaskService.getTasksForRequest(self.request).then(function (tasks) {
                self.tasks = tasks;
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

    })
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks['submit'];
    if (!task) {
      console.log('error addressing request: no task');
      return;
    }

    self.submitting = 'started';

    self.request.addressing = {addressed: self.addressed, message: ''};

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
    if (source == 'loadData') {
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
      if (newValue != oldValue) {
        console.log('dirty point: ' + self.rows[row].id);
        dirty = true;
        self.rows[row].dirty = true;
      }
    }

    // If nothing changed, there's nothing to do! Otherwise, save the request.
    if (dirty) {
      RequestService.saveRequest(self.request).then(function () {
        // If we are in the "submit" stage of the workflow and the form is modified, then it will need to be
        // revalidated. This is done by sending the "requestModified" signal.
        if (self.tasks['submit']) {
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
    var signal = self.signals['requestModified'];

    if (signal) {
      console.log('form modified whilst in submit state: sending signal');

      TaskService.sendSignal(signal).then(function () {
        // The "submit" task will have changed back to "edit".
        TaskService.getTasksForRequest(self.request).then(function (tasks) {
          self.tasks = tasks;
        });
      });
    }
  }
}
