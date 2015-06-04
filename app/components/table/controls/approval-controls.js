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
  function rejectSelectedPoints() {
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
      templateUrl: 'components/table/controls/modals/rejection-modal.html',
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
  function approveSelectedPoints() {
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
  function submit() {
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
      TaskService.completeTask(task.id, []).then(function (task) {
          console.log('completed task ' + task.id);

          // Clear the cache so that the state reload also pulls a fresh request
          RequestService.clearCache();

          $state.reload().then(function () {
            self.submitting = 'success';
          });
        },

        function (error) {
          console.log('error completing task ' + task.id);
          self.submitting = 'error';
        });
    });
  }
}