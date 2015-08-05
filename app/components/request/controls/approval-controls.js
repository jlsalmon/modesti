'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:ApprovalControlsController
 * @description # ApprovalControlsController Controller of the modesti
 */
angular.module('modesti').controller('ApprovalControlsController', ApprovalControlsController);

function ApprovalControlsController($state, $modal, RequestService, TaskService) {
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
  self.rejectSelectedPoints = rejectSelectedPoints;
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

    // By default, all points are approved.
    initialiseApprovalState();

    // Update the table settings to paint the row backgrounds depending on
    // if they have already been approved or rejected
    self.parent.renderRowBackgrounds();
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
    TaskService.claimTask(self.tasks['approve'].name, self.request.requestId).then(function(task) {
      console.log('claimed task successfully');
      self.tasks['approve'] = task;
    });
  }

  /**
   *
   */
  function initialiseApprovalState() {
    var point;
    for (var i = 0, len = self.rows.length; i < len; i++) {
      point = self.rows[i];

      if (!point.approval) {
        point.approval = {
          approved: true,
          message: ''
        };
      }
    }
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

    var point;
    for (var i = 0, len = self.rows.length; i < len; i++) {
      point = self.rows[i];

      if (selectedPointIds.indexOf(point.id) > -1) {
        if (!point.approval) {
          point.approval = {};
        }
      }
    }

    // The user must supply a comment for each rejected point. Display a modal
    // with a text field for each selected point.
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/controls/modals/rejection-modal.html',
      controller: 'RejectionModalController as ctrl',
      resolve: {
        selectedPointIds: function() {
          return selectedPointIds;
        },
        rows: function() {
          return self.rows;
        }
      }
    });

    // Callback fired when the user clicks 'ok'. Not fired if 'cancel' clicked.
    modalInstance.result.then(function() {
      self.request.approved = false;

      var point;
      for (var i = 0, len = self.rows.length; i < len; i++) {
        point = self.rows[i];

        if (selectedPointIds.indexOf(point.id) > -1) {
          point.approval.approved = false;
        }
      }

      // Save the request
      RequestService.saveRequest(self.request).then(function () {

        // Update the table settings to paint the approved rows with a red background
        self.parent.renderRowBackgrounds();
      });
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

    var point;
    for (var i = 0, len = self.rows.length; i < len; i++) {
      point = self.rows[i];

      if (selectedPointIds.indexOf(point.id) > -1) {
        point.approval = {
          approved: true,
          message: ''
        };
      }
    }

    // Save the request
    RequestService.saveRequest(self.request).then(function () {

      // Update the table settings to paint the approved rows with a red background
      self.parent.renderRowBackgrounds();
    });
  }

  /**
   *
   * @returns {boolean}
   */
  function canSubmit() {
    return self.parent.getSelectedPointIds().length > 0;
  }

  /**
   *
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

    self.request.approved = entireRequestApproved;

    // Save the request
    RequestService.saveRequest(self.request).then(function () {

      // Complete the task
      TaskService.completeTask(task.name, self.request.requestId).then(function () {
          console.log('completed task ' + task.name);

          // Clear the cache so that the state reload also pulls a fresh request
          RequestService.clearCache();

          $state.reload().then(function () {
            self.submitting = 'success';
          });
        },

        function (error) {
          console.log('error completing task ' + task.name);
          self.submitting = 'error';
        });
    });
  }
}