'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:ApprovalControlsController
 * @description # ApprovalControlsController Controller of the modesti
 */
angular.module('modesti').controller('ApprovalControlsController', ApprovalControlsController);

function ApprovalControlsController($state, $modal, $timeout, RequestService, TaskService, ValidationService, AlertService) {
  var self = this;

  self.parent = {};
  self.request = {};
  self.rows = {};
  self.tasks = {};
  self.signals = {};
  self.hot = {};

  self.submitting = undefined;

  self.init = init;
  self.isCurrentUserAuthorised = isCurrentUserAuthorised;
  self.isCurrentTaskClaimed = isCurrentTaskClaimed;
  self.isCurrentUserAssigned = isCurrentUserAssigned;
  self.claim = claim;
  self.approveSelectedPoints = approveSelectedPoints;
  self.approveAll = approveAll;
  self.rejectSelectedPoints = rejectSelectedPoints;
  self.rejectAll = rejectAll;
  self.canValidate = canValidate;
  self.canSubmit = canSubmit;
  self.validate = validate;
  self.submit = submit;

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

    // Initialise the approval state of the request itself
    if (!self.request.approval) {
      self.request.approval = {approved: undefined, message: ''};
    }

    // Initialise the approval state of each point
    var point;
    for (var i = 0, len = self.rows.length; i < len; i++) {
      point = self.rows[i];

      if (!point.approval) {
        point.approval = {approved: undefined, message: ''};
      }
    }

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
   * Mark the currently selected points as rejected.
   */
  function rejectSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var selectedPointIds = self.parent.getSelectedPointIds();
    rejectPoints(selectedPointIds);
  }

  /**
   *
   * @param event
   */
  function rejectAll(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    // If the entire request is rejected, the user must supply a global comment describing the reason
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/controls/modals/rejection-modal.html',
      controller: 'RejectionModalController as ctrl',
      resolve: {
        request: function () {
          return self.request;
        }
      }
    });

    // Callback fired when the user clicks 'ok'. Not fired if 'cancel' clicked.
    modalInstance.result.then(function () {
      self.request.approval.approved = false;

      var point;
      for (var i = 0, len = self.rows.length; i < len; i++) {
        point = self.rows[i];
        point.approval.approved = false;
        point.approval.message = self.request.approval.message;
      }

      // Save the request
      RequestService.saveRequest(self.request);
    });

  }

  /**
   *
   * @param pointIds
   */
  function rejectPoints(pointIds) {
    var point;
    for (var i = 0, len = self.rows.length; i < len; i++) {
      point = self.rows[i];

      if (!point.approval) {
        point.approval = {};
      }

      if (pointIds.indexOf(point.id) > -1) {
        point.approval.approved = false;

        if (!point.approval.message) {
          ValidationService.setErrorMessage(point, 'approval.message', 'Reason for rejection must be given in the comment field');
        }
      }
    }

    // Save the request
    RequestService.saveRequest(self.request).then(function () {
      self.parent.hot.render();
    })
  }

  /**
   * Mark the currently selected points as rejected.
   */
  function approveSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var selectedPointIds = self.parent.getSelectedPointIds();
    approvePoints(selectedPointIds);
  }

  /**
   *
   * @param event
   */
  function approveAll(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var pointIds = self.rows.map(function (row) {
      return row.id
    });
    approvePoints(pointIds);

    self.request.approval = {approved: true, message: ''};
  }

  /**
   *
   * @param pointIds
   */
  function approvePoints(pointIds) {
    var point;
    for (var i = 0, len = self.rows.length; i < len; i++) {
      point = self.rows[i];

      if (pointIds.indexOf(point.id) > -1) {

        // TODO: there may be other errors not related to approval that we don't want to nuke
        point.errors = [];

        point.approval = {
          approved: true,
          message: null
        };
      }
    }

    // Save the request
    RequestService.saveRequest(self.request);
  }

  /**
   *
   */
  function canValidate() {
    return self.tasks['edit'];
  }

  /**
   * The approval may only be submitted if all points in the request have been either approved or rejected. If they have
   * been rejected, there must be an accompanying comment.
   *
   * @returns {boolean}
   */
  function canSubmit() {
    //var point;
    //for (var i = 0, len = self.rows.length; i < len; i++) {
    //  point = self.rows[i];
    //
    //  if (!point.approval || point.approval.approved === null) {
    //    return false;
    //  }
    //
    //  else if (point.approval.approved === false && !point.approval.message) {
    //    return false;
    //  }
    //}
    //
    //return true;

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
      ValidationService.validateRequest(self.rows, self.parent.schema).then(function (valid) {
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
                // Claim the submit task
                // claim();
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
   * @param event {Object}
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks['submit'];
    if (!task) {
      console.log('error approving request: no task');
      return;
    }

    self.submitting = 'started';

    // Determine whether the entire request is approved or not
    var point, entireRequestApproved = true;
    for (var i = 0, len = self.rows.length; i < len; i++) {
      point = self.rows[i];

      if (point.approval && point.approval.approved == false) {
        entireRequestApproved = false;
      }
    }

    self.request.approval.approved = entireRequestApproved;

    // Save the request
    RequestService.saveRequest(self.request).then(function () {

      // Complete the task
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

      var point = self.rows[row];
      if (property === 'approval.message' && point.approval && point.approval.approved === false) {

        // If a point is rejected and a comment has just been deleted, then add an error
        if (newValue === undefined || newValue === null || newValue === '') {
          ValidationService.setErrorMessage(point, 'approval.message', 'Reason for rejection must be given in the comment field');
        }

        // If a point is rejected and a comment has just been added, then remove the error
        else {
          point.errors = [];
        }

        self.parent.hot.render();
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