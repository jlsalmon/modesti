'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:ApprovalControlsController
 * @description # ApprovalControlsController Controller of the modesti
 */
angular.module('modesti').controller('ApprovalControlsController', ApprovalControlsController);

function ApprovalControlsController($state, $modal, $location, RequestService, TaskService, ValidationService) {
  var self = this;

  self.request = {};
  self.rows = {};
  self.tasks = {};
  self.parent = {};

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
  self.canSubmit = canSubmit;
  self.submit = submit;

  /**
   *
   */
  function init(parent) {
    self.request = parent.request;
    self.rows = parent.rows;
    self.tasks = parent.tasks;
    self.parent = parent;

    // Initialise the approval state of the request itself
    if (!self.request.approval) {
      self.request.approval = {approved: undefined, message: ''};
      ;
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
    return TaskService.isCurrentUserAuthorised(self.tasks['approve']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentTaskClaimed() {
    return TaskService.isTaskClaimed(self.tasks['approve']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAssigned() {
    return TaskService.isCurrentUserAssigned(self.tasks['approve']);
  }

  /**
   *
   */
  function claim() {
    TaskService.claimTask(self.tasks['approve'].name, self.request.requestId).then(function (task) {
      console.log('claimed task successfully');
      self.tasks['approve'] = task;
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
   * The approval may only be submitted if all points in the request have been either approved or rejected. If they have
   * been rejected, there must be an accompanying comment.
   *
   * @returns {boolean}
   */
  function canSubmit() {
    var point;
    for (var i = 0, len = self.rows.length; i < len; i++) {
      point = self.rows[i];

      if (!point.approval || point.approval.approved === null) {
        return false;
      }

      else if (point.approval.approved === false && !point.approval.message) {
        return false;
      }
    }

    return true;
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

    var task = self.tasks['approve'];
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

    var change, row, property, oldValue, newValue;
    for (var i = 0, len = changes.length; i < len; i++) {
      change = changes[i];
      row = change[0];
      property = change[1];
      oldValue = change[2];
      newValue = change[3];

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
  }
}