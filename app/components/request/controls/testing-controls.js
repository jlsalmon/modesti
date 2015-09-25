'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TestingController
 * @description # TestingController Controller of the modesti
 */
angular.module('modesti').controller('TestingController', TestingController);

function TestingController($scope, $state, RequestService, TaskService, AlertService, ValidationService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submitting = undefined;
  self.passed = true;

  self.passSelectedPoints = passSelectedPoints;
  self.passAll = passAll;
  self.failSelectedPoints = failSelectedPoints;
  self.validate = validate;
  self.submit = submit;

  init();

  /**
   *
   */
  function init() {
    // Initialise the testing state of the request itself
    if (!self.parent.request.testing) {
      self.parent.request.testing = {passed: undefined, message: ''};
    }

    // Initialise the testing state of each point
    self.parent.rows.forEach(function (point) {
      if (!point.testing) {
        point.testing = {passed: undefined, message: ''};
      }
    });
  }

  /**
   *
   */
  function passSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.passed = true;

    var selectedPointIds = self.parent.getSelectedPointIds();
    passPoints(selectedPointIds);
  }

  /**
   *
   * @param event
   */
  function passAll(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var pointIds = self.parent.rows.map(function (row) {
      return row.id;
    });
    passPoints(pointIds);

    self.parent.request.testing = {passed: true, message: ''};
  }

  /**
   *
   * @param pointIds
   */
  function passPoints(pointIds) {
    self.parent.rows.forEach(function (point) {
      if (pointIds.indexOf(point.id) > -1) {

        // TODO: there may be other unrelated errors that we don't want to nuke
        point.errors = [];

        point.testing = {
          passed: true,
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
  function failSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.passed = false;

    var selectedPointIds = self.parent.getSelectedPointIds();
    failPoints(selectedPointIds);
  }

  /**
   *
   * @param pointIds
   */
  function failPoints(pointIds) {
    var point;
    for (var i = 0, len = self.parent.rows.length; i < len; i++) {
      point = self.parent.rows[i];

      if (!point.testing) {
        point.testing = {};
      }

      if (pointIds.indexOf(point.id) > -1) {
        point.testing.passed = false;

        if (!point.testing.message) {
          ValidationService.setErrorMessage(point, 'testing.message', 'Reason for rejection must be given in the comment field');
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


    // The request is only valid if all points in the request have been either passed or failed. If they have
    // been rejected, there must be an accompanying comment.

    var valid = true;
    self.parent.rows.forEach(function (point) {
      point.errors = [];

      if (!point.testing || point.testing.passed === null) {
        ValidationService.setErrorMessage(point, 'testing.message', 'Each point in the request must be either passed or failed');
        valid = false;
      }

      else if (point.testing.passed === false &&
      (point.testing.message === undefined || point.testing.message === null || point.testing.message === '')) {
        ValidationService.setErrorMessage(point, 'testing.message', 'Reason for failure must be given in the comment field');
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

    var task = self.parent.tasks.test;
    if (!task) {
      console.log('error testing request: no task');
      return;
    }

    self.submitting = 'started';

    self.parent.request.testing = {passed: self.tested, message: ''};

    // Save the request
    RequestService.saveRequest(self.parent.request).then(function () {

      TaskService.completeTask(task.name, self.parent.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          self.submitting = 'success';
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
