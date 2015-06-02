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
  self.tasks = {};
  self.parent = {};
  self.approvalResult = {};

  self.submitting = undefined;

  self.init = init;
  //self.approveSelectedPoints = approveSelectedPoints;
  self.rejectSelectedPoints = rejectSelectedPoints;
  self.canSubmit = canSubmit;
  self.submit = submit;

  /**
   *
   */
  function init(request, tasks, parent) {
    self.request = request;
    self.tasks = tasks;
    self.parent = parent;

    // If the request already has an approval result, use it
    if (self.request.approvalResult) {
      self.approvalResult = self.request.approvalResult;

      // Add in the descriptions
      for (var i = 0, len = self.approvalResult.items.length; i < len; i++) {
        var approvalResultItem = self.approvalResult.items[i];
        approvalResultItem.description = self.request.points[i].properties.pointDescription;
      }
    }

    // Build the initial approval result object if it doesn't exist
    else {
      self.approvalResult = {
        approved: true,
        items: []
      };

      for (var i = 0, len = self.request.points.length; i < len; i++) {
        var point = self.request.points[i];

        var approvalResultItem = {
          pointId: point.id,
          description: point.properties.pointDescription,
          approved: true,
          message: ''
        };

        self.approvalResult.items.push(approvalResultItem);
      }
    }


    // Update the table settings to paint the row backgrounds depending on if they have already been approved
    // or rejected
    self.parent.hot.updateSettings({
      cells: function (row, col, prop) {
        if (self.approvalResult.items[row].approved == false) {
          return {renderer: self.parent.dangerCellRenderer};
        }
      }
    });

    var points = [self.request.points[0], self.request.points[1]];
    self.parent.hot.loadData(points);
  }

  /**
   * Mark the currently selected points as rejected.
   */
  function rejectSelectedPoints() {
    var selectedPointIds = self.parent.getSelectedPointIds();

    // The user must supply a comment for each rejected point
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/table/controls/rejection/rejection-modal.html',
      controller: 'CommentsController as ctrl',
      resolve: {
        selectedPointIds: function() {
          return selectedPointIds;
        },
        approvalResult: function() {
          return self.approvalResult;
        }
      }
    });

    modalInstance.result.then(function(rejectedPoints) {
      console.log(rejectedPoints);
      self.approvalResult.approved = false;

      //for (var i = 0, len = selectedPointIds.length; i < len; i++) {
      //  self.request.points[selectedPointIds[i]].approved = false;
      //}

      for (var i = 0, len = self.approvalResult.items.length; i < len; i++) {
        var approvalResultItem = self.approvalResult.items[i];

        if (selectedPointIds.indexOf(approvalResultItem.pointId) > -1) {
          approvalResultItem.approved = false;
        }
      }

      // Save the request
      RequestService.saveRequest(self.request).then(function () {

        // Update the table settings to paint the approved rows with a red background
        self.parent.hot.updateSettings({
          cells: function (row, col, prop) {
            if (self.approvalResult.items[row].approved == false) {
              return {renderer: self.parent.dangerCellRenderer};
            }
          }
        })
      });
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

    // Send the approval result as a JSON string
    var variables = [{
      "name": "approvalResult",
      "value": JSON.stringify(self.approvalResult),
      "type": "string"
    }];

    TaskService.completeTask(task.id, variables).then(function (task) {
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
  }
}