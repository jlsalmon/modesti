'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TestingController
 * @description # TestingController
 */
angular.module('modesti').controller('TestingController', TestingController);

function TestingController($scope, RequestService, AlertService, ValidationService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.passed = false;

  self.passSelectedPoints = passSelectedPoints;
  self.passAll = passAll;
  self.failSelectedPoints = failSelectedPoints;
  self.submit = submit;
  self.validate = validate;

  init();

  /**
   *
   */
  function init() {
    // Initialise the testing state of the request itself
    if (!self.parent.request.properties.testResult) {
      self.parent.request.properties.testResult = {passed: null, message: ''};
    }

    // Initialise the test result of each point
    self.parent.rows.forEach(function (point) {
      if (!point.properties.testResult) {
        point.properties.testResult = {passed: null, message: ''};
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
      return row.lineNo;
    });
    passPoints(pointIds);

    self.parent.request.properties.testResult = {passed: true, message: ''};
  }

  /**
   *
   * @param pointIds
   */
  function passPoints(pointIds) {
    self.parent.rows.forEach(function (point) {
      if (pointIds.indexOf(point.lineNo) > -1) {

        // TODO: there may be other unrelated errors that we don't want to nuke
        point.errors = [];

        point.properties.testResult = {
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

      if (!point.properties.testResult) {
        point.properties.testResult = {};
      }

      if (pointIds.indexOf(point.lineNo) > -1) {
        point.properties.testResult.passed = false;

        if (!point.properties.testResult.message) {
          ValidationService.setErrorMessage(point, 'testResult.message', 'Reason for rejection must be given in the comment field');
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

    self.parent.validating = 'started';
    AlertService.clear();

    // The request is only valid if all points in the request have been either passed or failed. If they have
    // been rejected, there must be an accompanying comment.

    var valid = true;
    self.parent.rows.forEach(function (point) {
      point.errors = [];

      if (!point.properties.testResult || point.properties.testResult.passed === null) {
        ValidationService.setErrorMessage(point, 'testResult.message', 'Each point in the request must be either passed or failed');
        valid = false;
      }

      else if (point.properties.testResult.passed === false &&
      (point.properties.testResult.message === undefined || point.properties.testResult.message === null || point.properties.testResult.message === '')) {
        ValidationService.setErrorMessage(point, 'testResult.message', 'Reason for failure must be given in the comment field');
        valid = false;
      }
    });

    if (!valid) {
      self.parent.validating = 'error';
      AlertService.add('danger', 'Request failed validation with ' + self.parent.getNumValidationErrors() + ' errors');
      self.parent.hot.render();
      return;
    }

    self.parent.validating = 'success';
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

    var passed = true;
    var numFailures = 0;

    self.parent.rows.forEach(function (point) {
      if (!point.properties.testResult || point.properties.testResult.passed === false) {
        passed = false;
        numFailures++;
      }
    });

    var message;
    if (passed) {
      message = 'All points passed'
    } else {
      message = (self.parent.rows.length - numFailures) + ' out of ' + self.parent.rows.length + ' points failed';
    }

    self.parent.request.properties.testResult = {passed: passed, message: message};
    self.parent.submit();
  }
}
