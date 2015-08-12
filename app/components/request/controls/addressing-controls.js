'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AddressingControlsController
 * @description # AddressingControlsController Controller of the modesti
 */
angular.module('modesti').controller('AddressingControlsController', AddressingControlsController);

function AddressingControlsController($state, RequestService, TaskService) {
  var self = this;

  self.parent = {};
  self.request = {};
  self.tasks = {};

  self.submitting = undefined;
  self.addressed = true;

  self.init = init;
  self.isCurrentUserAuthorised = isCurrentUserAuthorised;
  self.isCurrentTaskClaimed = isCurrentTaskClaimed;
  self.isCurrentUserAssigned = isCurrentUserAssigned;
  self.claim = claim;
  self.rejectRequest = rejectRequest;
  self.canSubmit = canSubmit;
  self.submit = submit;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
    self.request = parent.request;
    self.tasks = parent.tasks;

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
    })
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAuthorised() {
    return TaskService.isCurrentUserAuthorised(self.tasks['address']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentTaskClaimed() {
    return TaskService.isTaskClaimed(self.tasks['address']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAssigned() {
    return TaskService.isCurrentUserAssigned(self.tasks['address']);
  }

  /**
   *
   */
  function claim() {
    TaskService.claimTask(self.tasks['address'].name, self.request.requestId).then(function (task) {
      console.log('claimed task successfully');
      self.tasks['address'] = task;
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
   * The addressing can be submitted if all points that require addresses have them.
   **
   * TODO: only show those points which require addresses to the addresser
   *
   * TODO: how to deduce if a point requires an address? What if a request has a mixture of datasources, some which
   * TODO: require an address and some which don't? Can it be derived somehow from the moneq or the plc name? Also,
   * TODO: what if someone adds a datasource but doesn't actually have any points of that type?
   *
   * @returns {boolean}
   */
  function canSubmit() {
    return false;
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks['address'];
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
}