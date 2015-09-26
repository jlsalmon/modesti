'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AddressingController
 * @description # AddressingController Controller of the modesti
 */
angular.module('modesti').controller('AddressingController', AddressingController);

function AddressingController($scope, RequestService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.addressed = true;

  self.rejectRequest = rejectRequest;

  init();

  /**
   *
   */
  function init() {
    // Register hooks
    self.parent.hot.addHook('afterChange', afterChange);

    // TODO fill the table with empty, read-only rows
    //self.parent.hot.updateSettings( { minSpareRows: 50 } );
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
   * Called after a change is made to the table (edit, paste, etc.)
   *
   * @param changes a 2D array containing information about each of the edited cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: "alter", "empty", "edit", "populateFromArray", "loadData", "autofill", "paste"
   */
  function afterChange(changes, source) {
    console.log('afterChange()');

    // When the table is initially loaded, this callback is invoked with source == 'loadData'. In that case, we don't
    // want to save the request or send the modification signal.
    if (source === 'loadData') {
      return;
    }

    var change, row, property, oldValue, newValue, dirty = false;
    for (var i = 0, len = changes.length; i < len; i++) {
      change = changes[i];
      row = change[0];
      property = change[1];
      oldValue = change[2];
      newValue = change[3];

      // Mark the point as dirty.
      if (newValue !== oldValue) {
        console.log('dirty point: ' + self.parent.rows[row].lineNo);
        dirty = true;
        self.parent.rows[row].dirty = true;
      }
    }

    // If nothing changed, there's nothing to do! Otherwise, save the request.
    if (dirty) {
      RequestService.saveRequest(self.parent.request).then(function () {
        // If we are in the "submit" stage of the workflow and the form is modified, then it will need to be
        // revalidated. This is done by sending the "requestModified" signal.
        if (self.parent.tasks.submit) {
          self.parent.sendModificationSignal();
        }
      });
    }
  }
}
