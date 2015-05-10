'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestCreationControlsController
 * @description # RequestCreationControlsController Controller of the modesti
 */
angular.module('modesti').controller('RequestCreationControlsController', RequestCreationControlsController);

function RequestCreationControlsController($window, Restangular, RequestService, ValidationService) {
  var self = this;
  
  self.init = init;
  self.addRow = addRow;
  self.duplicateSelectedRows = duplicateSelectedRows;
  self.deleteSelectedRows = deleteSelectedRows;
  self.validate = validate;
  self.submit = submit;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
  }

  /**
   *
   */
  function addRow() {
    console.log('adding new row');
    var request = self.parent.request;

    var newRow = {
      'name' : '',
      'description' : '',
      'domain' : request.domain
    };

    request.points.push(newRow);

    RequestService.saveRequest(request).then(function(request) {
      console.log('added new row');
      self.parent.request = request;

      // Reload the table data
      self.parent.tableParams.reload();

      // Move to the last page
      var pages = self.parent.tableParams.settings().$scope.pages;
      for ( var i in pages) {
        if (pages[i].type == "last") {
          self.parent.tableParams.page(pages[i].number);
        }
      }

    }, function(error) {
      console.log('error adding new row: ' + error);
    });
  }

  /**
   *
   */
  function duplicateSelectedRows() {
    var points = self.parent.request.points;
    console.log('duplicating rows (before: ' + points.length + ' points)');

    // Find the selected points and duplicate them
    for (var i in points) {
      var point = points[i];

      if (self.parent.checkboxes.items[point.id]) {
        var duplicate = angular.copy(point);
        // Remove the ID of the duplicated point, as the backend will generate
        // us a new one when we save
        delete duplicate.id;
        // Add the new duplicate to the original points
        points.push(duplicate);
      }
    }

    // Save the changes
    RequestService.saveRequest(self.parent.request).then(function(savedRequest) {
      console.log('saved request after row duplication');
      console.log('duplicated rows (after: ' + savedRequest.points.length + ' points)');

      // Reload the table data
      self.parent.tableParams.reload();

    }, function(error) {
      console.log('error saving request after row duplication: ' + error);
    });
  }

  /**
   *
   */
  function deleteSelectedRows() {
    var points = self.parent.request.points;
    console.log('deleting rows (before: ' + points.length + ' points)');

    // Find the selected points and mark them as deleted
    for (var i in points) {
      var point = points[i];

      if (self.parent.checkboxes.items[point.id]) {
        point.deleted = true;
      }
    }

    // Save the changes
    RequestService.saveRequest(self.request).then(function(savedRequest) {
      console.log('saved request after row deletion');
      console.log('deleted rows (after: ' + savedRequest.points.length + ' points)');

      // Reload the table data
      self.parent.tableParams.reload();

    }, function(error) {
      console.log('error saving request after row deletion: ' + error);
    });
  }
  

  /**
   * 
   */
  function validate() {
    var request = self.parent.request;

    ValidationService.validateRequest(request).then(function(result) {
      console.log('validated request');
      self.validationResult = result;
    }, function(error) {
      console.log('error validating request: ' + error);
    });
  }
  
  /**
   * 
   */
  function submit() {
    Restangular.one('requests/' + self.parent.request.requestId + '/submit').post().then(function(response) {
      console.log('submitted request');
      // Reload the current state
      $window.location.reload(true);
    },
    
    function(error) {
      console.log('error submitting request: ' + error);
    });
  }
}