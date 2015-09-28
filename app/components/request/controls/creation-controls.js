'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CreationController
 * @description # CreationController
 */
angular.module('modesti').controller('CreationController', CreationController);

function CreationController($scope, $http, $state, $modal, RequestService, AlertService, ColumnService, ValidationService) {
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
      return !ValidationService.isEmptyPoint(point);
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
        var point = self.parent.hot.getSourceDataAtRow(row);
        var propName = property.split('.')[1];

        var prop = point.properties[propName];

        for (var attribute in prop) {
          if (prop.hasOwnProperty(attribute)) {
            prop[attribute] = null;
          }
        }
      }

      // This is a workaround. See function documentation for info.
      saveNewValue(row, property, newValue);
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

  /**
   * It would be really great to get rid of this code, but currently Handsontable does not support columns backed
   * by complex objects, so it's necessary until then. See https://github.com/handsontable/handsontable/issues/2578.
   *
   * TODO: consolidate common functionality with column.source() function in ColumnService
   *
   * @param row
   * @param property
   * @param newValue
   */
  function saveNewValue(row, property, newValue) {
    if (property.indexOf('.') === -1) {
      return;
    }

    var point = self.parent.hot.getSourceDataAtRow(row);
    // get the outer object i.e. properties.location.value -> location
    var prop = property.split('.')[1];
    var field = getField(prop);

    // Don't make a call if the query is less than the minimum length
    if (field.minLength && newValue < field.minLength) {
      return;
    }

    var params = {};
    if (field.params === undefined) {
      // By default, searches are done via parameter called 'query'
      params.query = newValue;
    } else {
      for (var i in field.params) {
        var param = field.params[i];

        // The parameter might be a sub-property of another property (i.e. contains a dot). In
        // that case, find the property of the point and add it as a search parameter. This
        // acts like a filter for a search, based on another property.
        // TODO: add "filter" parameter to schema instead of this?
        if (param.indexOf('.') > -1) {
          var parts = param.split('.');
          var dependentProperty = parts[0];
          var dependentSubProperty = parts[1];

          if (point.properties[dependentProperty] && point.properties[dependentProperty].hasOwnProperty(dependentSubProperty) &&
              point.properties[dependentProperty][dependentSubProperty]) {
            params[dependentSubProperty] = point.properties[dependentProperty][dependentSubProperty];
          } else {
            params[dependentSubProperty] = '';
          }
        }
        else {
          params[param] = newValue;
        }
      }
    }

    if (prop === field.id && field.type === 'autocomplete') {

      $http.get(BACKEND_BASE_URL + '/' + field.url, {
        params: params,
        cache: true
      }).then(function (response) {
        if (!response.data.hasOwnProperty('_embedded')) {
          return [];
        }

        var returnPropertyName = field.url.split('/')[0];
        response.data._embedded[returnPropertyName].map(function (item) {
          var value = (field.model === undefined && typeof item === 'object') ? item.value : item[field.model];

          if (value === newValue) {
            console.log('saving new value');
            delete item._links;
            point.properties[prop] = item;

            // Automatically update dependent dropdowns.
            updateDependentValues(row, property);
          }
        });
      });
    }
  }

  /**
   *
   */
  function updateDependentValues(row, property) {
    self.parent.activeCategory.fields.forEach(function (field) {

      if (field.params) {
        field.params.forEach(function (param) {
          if (param.indexOf('.') > -1) {
            var thisProp = property.split('.')[1];
            var dependentProp = param.split('.')[0];

            if (thisProp === dependentProp) {
              ColumnService.getOptions(field, self.parent.hot, row, '').then(function (results) {
                console.log('got ' + results.length + ' results for dependent field ' + field.id + ' for ' + thisProp);

                if (results.length === 1) {
                  self.parent.hot.setDataAtRowProp(row, 'properties.' + field.id + '.value', results[0].text);
                }
              });
            }
          }
        });
      }
    });
  }

  /**
   *
   * @param property
   * @returns {*}
   */
  function getField(property) {
    var result;

    self.parent.schema.categories.forEach(function (category) {
      category.fields.forEach(function (field) {
        if (field.id === property) {
          result = field;
        }
      });
    });

    self.parent.schema.datasources.forEach(function (datasource) {
      datasource.fields.forEach(function (field) {
        if (field.id === property) {
          result = field;
        }
      });
    });

    return result;
  }
}
