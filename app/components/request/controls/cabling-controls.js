'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CablingController
 * @description # CablingController
 */
angular.module('modesti').controller('CablingController', CablingController);

function CablingController($scope, RequestService, AlertService, ValidationService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

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
      self.parent.request.properties.cablingResult = {cabled: null, message: ''};
    }

    // Initialise the cabling state of each point
    self.parent.rows.forEach(function (point) {
      if (!point.properties.cablingResult) {
        point.properties.cablingResult = {cabled: null, message: ''};
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
      return row.lineNo;
    });
    cablePoints(pointIds);

    self.parent.request.properties.cablingResult = {cabled: true, message: ''};
  }

  /**
   *
   * @param pointIds
   */
  function cablePoints(pointIds) {
    self.parent.rows.forEach(function (point) {
      if (pointIds.indexOf(point.lineNo) > -1) {

        // TODO: there may be other unrelated errors that we don't want to nuke
        point.errors = [];

        point.properties.cablingResult = {
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

      if (!point.properties.cablingResult) {
        point.properties.cablingResult = {};
      }

      if (pointIds.indexOf(point.lineNo) > -1) {
        point.properties.cablingResult.cabled = false;

        if (!point.properties.cablingResult.message) {
          ValidationService.setErrorMessage(point, 'cablingResult.message', 'Reason for rejection must be given in the comment field');
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
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.parent.request.properties.cablingResult = {cabled: self.cabled, message: ''};
    self.parent.submit();
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

    // The request is only valid if all points in the request have been either calbed or rejected. If they have
    // been rejected, there must be an accompanying comment.

    var valid = true;
    self.parent.rows.forEach(function (point) {
      point.errors = [];

      if (!point.properties.cablingResult || point.properties.cablingResult.cabled === null) {
        ValidationService.setErrorMessage(point, 'cablingResult.message', 'Each point in the request must be either cabled or rejected');
        valid = false;
      }

      else if (point.properties.cablingResult.cabled === false &&
      (point.properties.cablingResult.message === undefined || point.properties.cablingResult.message === null || point.properties.cablingResult.message === '')) {
        ValidationService.setErrorMessage(point, 'cablingResult.message', 'Reason for rejection must be given in the comment field');
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
}
