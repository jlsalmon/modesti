'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CablingController
 * @description # CablingController Controller of the modesti
 */
angular.module('modesti').controller('CablingController', CablingController);

function CablingController($scope, $state, RequestService, TaskService, AlertService, ValidationService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submitting = undefined;
  self.cabled = true;

  self.cableSelectedPoints = cableSelectedPoints;
  self.cableAll = cableAll;
  self.rejectSelectedPoints = rejectSelectedPoints;
  self.validate = validate;
  self.submit = submit;

  init();

  /**
   *
   */
  function init() {
    // Initialise the cabling state of the request itself
    if (!self.parent.request.cabling) {
      self.parent.request.cabling = {cabled: undefined, message: ''};
    }

    // Initialise the cabling state of each point
    self.parent.rows.forEach(function (point) {
      if (!point.cabling) {
        point.cabling = {cabled: undefined, message: ''};
      }
    });
  }

  /**
   *
   */
  function cableSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.cabled = true;

    var selectedPointIds = self.parent.getSelectedPointIds();
    cablePoints(selectedPointIds);
  }

  /**
   *
   * @param event
   */
  function cableAll(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var pointIds = self.parent.rows.map(function (row) {
      return row.id;
    });
    cablePoints(pointIds);

    self.parent.request.cabling = {cabled: true, message: ''};
  }

  /**
   *
   * @param pointIds
   */
  function cablePoints(pointIds) {
    self.parent.rows.forEach(function (point) {
      if (pointIds.indexOf(point.id) > -1) {

        // TODO: there may be other unrelated errors that we don't want to nuke
        point.errors = [];

        point.cabling = {
          cabled: true,
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
  function rejectSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.cabled = false;

    var selectedPointIds = self.parent.getSelectedPointIds();
    rejectPoints(selectedPointIds);
  }

  /**
   *
   * @param pointIds
   */
  function rejectPoints(pointIds) {
    var point;
    for (var i = 0, len = self.parent.rows.length; i < len; i++) {
      point = self.parent.rows[i];

      if (!point.cabling) {
        point.cabling = {};
      }

      if (pointIds.indexOf(point.id) > -1) {
        point.cabling.cabled = false;

        if (!point.cabling.message) {
          ValidationService.setErrorMessage(point, 'cabling.message', 'Reason for rejection must be given in the comment field');
        }
      }
    }

    // Save the request
    RequestService.saveRequest(self.parent.request).then(function () {
      self.parent.hot.render();
    });
  }

  /**
   *
   * @param event
   */
  function validate(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.validating = 'started';
    AlertService.clear();


    // The request is only valid if all points in the request have been either calbed or rejected. If they have
    // been rejected, there must be an accompanying comment.

    var valid = true;
    self.parent.rows.forEach(function (point) {
      point.errors = [];

      if (!point.cabling || point.cabling.cabled === null) {
        ValidationService.setErrorMessage(point, 'cabling.message', 'Each point in the request must be either cabled or rejected');
        valid = false;
      }

      else if (point.cabling.cabled === false &&
      (point.cabling.message === undefined || point.cabling.message === null || point.cabling.message === '')) {
        ValidationService.setErrorMessage(point, 'cabling.message', 'Reason for rejection must be given in the comment field');
        valid = false;
      }
    });

    if (!valid) {
      self.validating = 'error';
      AlertService.add('danger', 'Request failed validation with ' + self.parent.getNumValidationErrors() + ' errors');
      self.parent.hot.render();
      return;
    }

    self.validating = 'success';
    AlertService.add('success', 'Request has been validated successfully');
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.parent.tasks.cable;
    if (!task) {
      console.log('error cabling request: no task');
      return;
    }

    self.submitting = 'started';

    self.parent.request.cabling = {cabled: self.cabled, message: ''};

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
}
