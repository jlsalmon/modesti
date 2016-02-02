'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CreationController
 * @description # CreationController
 */
angular.module('modesti').controller('CreationController', CreationController);

function CreationController($scope, $http, $q, $state, $modal, RequestService, AlertService, SchemaService, Utils) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submit = submit;
  self.split = split;

  init();

  /**
   *
   */
  function init() {
    // Register the afterChange() hook so that we can use it to send a signal to the backend if we are in 'submit'
    // state and the user makes a modification
    self.parent.hot.addHook('afterChange', afterChange);
  }

  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    // Remove empty points
    self.parent.request.points = self.parent.request.points.filter(function (point) {
      return !Utils.isEmptyPoint(point);
    });

    self.parent.submit();
  }

  /**
   *
   */
  function split(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var signal = self.parent.signals.splitRequest;
    if (!signal) {
      console.log('error splitting request: no signal');
      return;
    }

    var selectedPointIds = self.parent.getSelectedPointIds();
    if (!selectedPointIds.length) {
      return;
    }

    console.log('splitting points: ' + selectedPointIds);

    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/controls/modals/splitting-modal.html',
      controller: 'SplittingModalController as ctrl',
      resolve: {
        selectedPointIds: function () {
          return selectedPointIds;
        },
        rows: function () {
          return self.parent.rows;
        }
      }
    });

    // Callback fired when the user clicks 'ok'. Not fired if 'cancel' clicked.
    modalInstance.result.then(function () {
      self.splitting = 'started';
      AlertService.clear();

      // TODO refactor this into a service
      $http.post(signal._links.self.href, JSON.stringify(selectedPointIds)).then(function () {
        console.log('sent split signal');

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          self.splitting = 'success';
          AlertService.add('info', 'Request <b>' + self.parent.request.requestId + '</b> was successfully split.');
        });
      },

      function (error) {
        console.log('error sending signal: ' + error.data.message);
        self.splitting = 'error';
      });
    });
  }

  /**
   * Called after a change is made to the table (edit, paste, etc.)
   *
   * @param changes a 2D array containing information about each of the edited cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: "alter", "empty", "edit", "populateFromArray", "loadData", "autofill", "paste"
   */
  function afterChange(changes, source) {
    console.log('afterChange()');

    // When the table is initially loaded, this callback is invoked with source === 'loadData'. In that case, we don't
    // want to save the request or send the modification signal.
    if (source === 'loadData') {
      return;
    }

    // Make sure the point IDs are consecutive
    self.parent.rows.forEach(function (row, i) {
      row.lineNo = i + 1;
    });

    var promises = [];

    // Loop over the changes and check if anything actually changed. Mark any changed points as dirty.
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

      // If the value was cleared, make sure any other properties of the object are also cleared.
      if (newValue === undefined || newValue === null || newValue === '') {
        //var point = self.parent.hot.getSourceDataAtRow(row);
        var point = self.parent.rows[row];
        var propName = property.split('.')[1];

        var prop = point.properties[propName];

        if (typeof prop === 'object') {
          for (var attribute in prop) {
            if (prop.hasOwnProperty(attribute)) {
              prop[attribute] = null;
            }
          }
        } else {
          prop = null;
        }

      }

      // This is a workaround. See function documentation for info.
      var promise = saveNewValue(row, property, newValue);
      promises.push(promise);
    }

    // Wait for all new values to be updated
    $q.all(promises).then(function () {

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
    });

  }

  /**
   * It would be really great to get rid of this code, but currently Handsontable does not support columns backed
   * by complex objects, so it's necessary until then. See https://github.com/handsontable/handsontable/issues/2578.
   *
   * @param row
   * @param property
   * @param newValue
   */
  function saveNewValue(row, property, newValue) {
    var q = $q.defer();
    var point = self.parent.rows[row];

    // get the outer object i.e. properties.location.value -> location
    var outerProp = property.split('.')[1];
    var field = Utils.getField(self.parent.schema, outerProp);

    // Don't need to process non-object properties
    if (field.type !== 'autocomplete') {
      q.resolve();
      return q.promise;
    }

    SchemaService.queryFieldValues(field, newValue, point).then(function (values) {

      values.forEach(function (item) {
        var value = (field.model === undefined && typeof item === 'object') ? item.value : item[field.model];

        if (value === newValue) {
          console.log('saving new value');
          delete item._links;
          point.properties[outerProp] = item;
        }
      });

      q.resolve();
    });

    return q.promise;
  }
}
