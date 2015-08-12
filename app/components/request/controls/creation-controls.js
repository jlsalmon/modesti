'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CreationControlsController
 * @description # CreationControlsController Controller of the modesti
 */
angular.module('modesti').controller('CreationControlsController', CreationControlsController);

function CreationControlsController($http, $state, $location, $timeout, $modal, RequestService, TaskService, ValidationService, AlertService) {
  var self = this;

  self.parent = {};
  self.request = {};
  self.rows = {};
  self.tasks = {};
  self.signals = {};
  self.hot = {};

  self.validating = undefined;
  self.submitting = undefined;
  self.splitting = undefined;

  self.init = init;
  self.isCurrentUserOwner = isCurrentUserOwner;
  self.validate = validate;
  self.submit = submit;
  self.split = split;
  self.canValidate = canValidate;
  self.canSubmit = canSubmit;
  self.canSplit = canSplit;
  self.hasErrors = hasErrors;
  self.getTotalErrors = getTotalErrors;
  self.getNumValidationErrors = getNumValidationErrors;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
    self.request = parent.request;
    self.rows = parent.rows;
    self.tasks = parent.tasks;
    self.signals = parent.signals;
    self.hot = parent.hot;


    // Register the afterChange() hook so that we can use it to send a signal to the backend if we are in 'submit'
    // state and the user makes a modification
    self.hot.addHook('afterChange', afterChange);

    //// Update the table settings to paint the row backgrounds depending on if they have already been approved
    //// or rejected
    //if (self.request.approvalResult) {
    //  self.hot.updateSettings({
    //    cells: function (row, col, prop) {
    //      if (self.request.approvalResult.items[row].approved == false) {
    //        return {renderer: self.parent.dangerCellRenderer};
    //      }
    //    }
    //  });
    //}

    // Possibly show an alert, depending on how we got here
    //if (self.request.status === 'FOR_CORRECTION') {
    //  AlertService.add('danger', 'Request <b>' + self.request.requestId
    //      + '</b> requires correction. Please see the request log for details.');
    //} else if (self.request.valid === true) {
    //  AlertService.add('success', 'Request <b>' + self.request.requestId
    //      + '</b> was validated successfully.');
    //}
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserOwner() {
    return RequestService.isCurrentUserOwner(self.request);
  }

  /**
   *
   */
  function canValidate() {
    var task = self.tasks['edit'];
    return task;
  }

  /**
   *
   */
  function canSubmit() {
    return self.tasks['submit'];
  }

  /**
   *
   */
  function canSplit() {
    return self.getNumValidationErrors() > 0;
    //return self.parent.getSelectedPointIds().length > 0;
  }

  /**
   *
   * @returns {boolean}
   */
  function hasErrors() {
    return getNumValidationErrors() > 0 || getNumApprovalRejections() > 0
  }

  function getTotalErrors() {
    return getNumValidationErrors() + getNumApprovalRejections();
  }

  /**
   *
   * @returns {number}
   */
  function getNumValidationErrors() {
    var n = 0;

    for (var i in self.rows) {
      var point = self.rows[i];

      for (var j in point.errors) {
        n += point.errors[j].errors.length;
      }
    }

    return n;
  }

  function getNumApprovalRejections() {
    var n = 0;

    for (var i in self.rows) {
      var point = self.rows[i];

      if (point.approval.approved === false) {
        n++;
      }
    }

    return n;
  }

  /**
   *
   */
  function validate(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.validating = 'started';
    AlertService.clear();

    $timeout(function () {
      ValidationService.validateRequest(self.rows, self.parent.schema).then(function (valid) {
        // Render the table to show the error highlights
        self.hot.render();

        if (!valid) {
          self.validating = 'error';
          return;
        }

        // Validate server-side
        var task = self.tasks['edit'];

        if (!task) {
          console.log('warning: no validate task found');
          return;
        }

        // First save the request
        RequestService.saveRequest(self.request).then(function () {
          console.log('saved request before validation');

          // Complete the task associated with the request
          TaskService.completeTask(task.name, self.request.requestId).then(function () {
            console.log('completed task ' + task.name);

            // Clear the cache so that the state reload also pulls a fresh request
            RequestService.clearCache();

            $state.reload().then(function () {
              self.validating = 'success';
              AlertService.add('success', 'Request has been validated successfully');
            });
          },

          function (error) {
            console.log('error completing task: ' + error.statusText);
            self.validating = 'error';
          });
        },

        function (error) {
          console.log('error saving before validation: ' + error.statusText);
          self.validating = 'error';
        });
      });

    })
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks['submit'];

    if (!task) {
      console.log('warning: no submit task found');
      return;
    }

    AlertService.clear();
    self.submitting = 'started';

    // Complete the task associated with the request
    TaskService.completeTask(task.name, self.request.requestId).then(function () {
      console.log('completed task ' + task.name);

      var previousStatus = self.request.status;

      // Clear the cache so that the state reload also pulls a fresh request
      RequestService.clearCache();

      $state.reload().then(function () {
        self.submitting = 'success';

        // If the request is now FOR_CONFIGURATION, no need to go away from the request page
        if (self.request.status === 'FOR_CONFIGURATION') {
          AlertService.add('info', 'Your request has been submitted successfully and is ready to be configured.');
        }

        // If the request is in any other state, show a page with information about what happens next
        else {
          $state.go('submitted', {id: self.request.requestId, previousStatus: previousStatus});
        }
      });
    },

    function (error) {
      console.log('error completing task: ' + error);
      self.submitting = 'error';
    });
  }

  /**
   *
   */
  function split(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks['edit'];
    if (!task) {
      console.log('error splitting request: no task');
      return;
    }

    var signal = self.signals['splitRequest'];
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
          return self.rows;
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
          AlertService.add('info', 'Request <b>' + self.request.requestId + '</b> was successfully split.')
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

    // When the table is initially loaded, this callback is invoked with source == 'loadData'. In that case, we don't
    // want to save the request or send the modification signal.
    if (source == 'loadData') {
      return;
    }

    //if (source == 'paste') {
    //  console.log('paste');
    //  return;
    //}

    // Make sure the point IDs are consecutive
    for (var i = 0, len = self.rows.length; i < len; i++) {
      self.rows[i].id = i + 1;
    }

    // Loop over the changes and check if anything actually changed. Mark any changed points as dirty.
    var change, index, property, oldValue, newValue, dirty = false;
    for (var i = 0, len = changes.length; i < len; i++) {
      change = changes[i];
      index = change[0];
      property = change[1];
      oldValue = change[2];
      newValue = change[3];

      // Mark the point as dirty.
      if (newValue != oldValue) {
        console.log('dirty point: ' + self.rows[index].id);
        dirty = true;
        self.rows[index].dirty = true;
      }

      // This is a workaround. See function documentation for info.
      saveNewValue(index, property, newValue);
    }

    // If nothing changed, there's nothing to do! Otherwise, save the request.
    if (dirty) {
      RequestService.saveRequest(self.request).then(function () {
        // If we are in the "submit" stage of the workflow and the form is modified, then it will need to be
        // revalidated. This is done by sending the "requestModified" signal.
        if (self.tasks['submit']) {
          sendModificationSignal();
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
    if (property.indexOf('.') == -1) {
      return;
    }

    var point = self.hot.getSourceDataAtRow(row);
    // get the outer object i.e. properties.location.value -> location
    var prop = property.split('.')[1];
    var field = getField(prop);

    // Don't make a call if the query is less than the minimum length
    if (field.minLength && newValue < field.minLength) {
      return;
    }

    var params = {};
    if (field.params == undefined) {
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
          var props = param.split('.');
          params[props[1]] = point.properties[props[0]][props[1]];
        }
        else {
          params[param] = newValue;
        }
      }
    }

    if (prop == field.id && field.type == 'autocomplete') {

      $http.get(BACKEND_BASE_URL + '/' + field.url, {
        params: params,
        cache: true
      }).then(function (response) {
        if (!response.data.hasOwnProperty('_embedded')) {
          return [];
        }

        var returnPropertyName = field.url.split('/')[0];
        response.data._embedded[returnPropertyName].map(function (item) {
          var value = (field.model == undefined && typeof item == 'object') ? item.value : item[field.model];

          if (value == newValue) {
            console.log('saving new value');
            delete item._links;
            point.properties[prop] = item;
          }
        });
      });
    }
  }

  /**
   *
   * @param property
   * @returns {*}
   */
  function getField(property) {
    for (var i in self.parent.schema.categories) {
      var category = self.parent.schema.categories[i];

      for (var j in category.fields) {
        var field = category.fields[j];

        if (field.id == property) {
          return field;
        }
      }
    }
  }

  /**
   * Sends the "requestModified" signal when in the "submit" stage of the workflow in order to force the request
   * back to the "validate" stage.
   */
  function sendModificationSignal() {
    var task = self.tasks['submit'];
    if (!task) {
      console.log('error sending modification signal: no task');
      return;
    }

    var signal = self.signals['requestModified'];

    if (signal) {
      console.log('form modified whilst in submit state: sending signal');

      // TODO refactor this into a service
      $http.post(signal._links.self.href, {}).then(function () {
        console.log('sent modification signal');

        // The "submit" task will have changed to "validate".
        TaskService.getTasksForRequest(self.request).then(function (tasks) {
          self.tasks = tasks;
        });
      },

      function (error) {
        console.log('error sending signal: ' + error);
      });
    }
  }
}
