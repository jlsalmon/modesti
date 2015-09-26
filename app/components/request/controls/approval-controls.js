'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:ApprovalController
 * @description # ApprovalController Controller of the modesti
 */
angular.module('modesti').controller('ApprovalController', ApprovalController);

function ApprovalController($scope, $state, $modal, RequestService, TaskService, ValidationService, AlertService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submitting = undefined;

  self.approveSelectedPoints = approveSelectedPoints;
  self.approveAll = approveAll;
  self.rejectSelectedPoints = rejectSelectedPoints;
  self.rejectAll = rejectAll;
  self.validate = validate;
  self.submit = submit;

  init();

  /**
   *
   */
  function init() {
    // Initialise the approval state of the request itself
    if (!self.parent.request.approval) {
      self.parent.request.approval = {approved: undefined, message: ''};
    }

    // Initialise the approval state of each point
    self.parent.rows.forEach(function (point) {
      if (!point.approval) {
        point.approval = {approved: undefined, message: ''};
      }
    });

    // Register hooks
    self.parent.hot.addHook('afterChange', afterChange);

    //self.parent.hot.updateSettings({ minSpareRows: 50 });
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
          return self.parent.request;
        }
      }
    });

    // Callback fired when the user clicks 'ok'. Not fired if 'cancel' clicked.
    modalInstance.result.then(function () {
      self.parent.request.approval.approved = false;

      var point;
      for (var i = 0, len = self.parent.rows.length; i < len; i++) {
        point = self.parent.rows[i];
        point.approval.approved = false;
        point.approval.message = self.parent.request.approval.message;
      }

      // Save the request
      RequestService.saveRequest(self.parent.request);
    });

  }

  /**
   *
   * @param pointIds
   */
  function rejectPoints(pointIds) {
    var point;
    for (var i = 0, len = self.parent.rows.length; i < len; i++) {
      point = self.parent.rows[i];

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
    RequestService.saveRequest(self.parent.request).then(function () {
      self.parent.hot.render();
    });
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

    var pointIds = self.parent.rows.map(function (row) {
      return row.id;
    });
    approvePoints(pointIds);

    self.parent.request.approval = {approved: true, message: ''};
  }

  /**
   *
   * @param pointIds
   */
  function approvePoints(pointIds) {
    self.parent.rows.forEach(function (point) {
      if (pointIds.indexOf(point.id) > -1) {

        // TODO: there may be other errors not related to approval that we don't want to nuke
        point.errors = [];

        point.approval = {
          approved: true,
          message: null
        };
      }
    });

    // Save the request
    RequestService.saveRequest(self.parent.request);
  }

  /**
   *
   */
  function validate(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.parent.validating = 'started';
    AlertService.clear();

    // The request is only valid if all points in the request have been either approved or rejected. If they have
    // been rejected, there must be an accompanying comment.

    var point, valid = true;
    for (var i = 0, len = self.parent.rows.length; i < len; i++) {
      point = self.parent.rows[i];
      point.errors = [];

      if (!point.approval || point.approval.approved === null) {
        ValidationService.setErrorMessage(point, 'approval.message', 'Each point in the request must be either approved or rejected');
        valid = false;
      }

      else if (point.approval.approved === false &&
      (point.approval.message === undefined || point.approval.message === null || point.approval.message === '')) {
        ValidationService.setErrorMessage(point, 'approval.message', 'Reason for rejection must be given in the comment field');
        valid = false;
      }
    }

    if (!valid) {
      self.parent.validating = 'error';
      AlertService.add('danger', 'Request failed validation with ' + self.parent.getNumValidationErrors() + ' errors');
      self.parent.hot.render();
      return;
    }

    // Run the generic validations
    self.parent.validate();
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

    var task = self.parent.tasks.submit;
    if (!task) {
      console.log('error approving request: no task');
      return;
    }

    self.submitting = 'started';

    // Determine whether the entire request is approved or not
    var point, entireRequestApproved = true;
    for (var i = 0, len = self.parent.rows.length; i < len; i++) {
      point = self.parent.rows[i];

      if (point.approval && point.approval.approved === false) {
        entireRequestApproved = false;
      }
    }

    self.parent.request.approval.approved = entireRequestApproved;

    // Save the request
    RequestService.saveRequest(self.parent.request).then(function () {

      // Complete the task
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
          self.parent.sendModificationSignal();
        }
      });
    }
  }
}
