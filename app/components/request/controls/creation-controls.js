'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CreationController
 * @description # CreationController
 */
angular.module('modesti').controller('CreationController', CreationController);

function CreationController($scope, $http, $q, $state, $modal, RequestService, AlertService, SchemaService, TaskService, Utils) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.submit = submit;
  self.split = split;
  self.canValidate = canValidate;
  self.canSubmit = canSubmit;

  init();

  /**
   *
   */
  function init() {
    generateValues();
    // Register the afterChange() hook so that we can use it to send a signal to the backend if we are in 'submit'
    // state and the user makes a modification
    self.parent.hot.addHook('afterChange', generateValues);
  }

  function canValidate() {
    return true;
  }

  function canSubmit() {
    var numEmptyPoints = 0;
    self.parent.request.points.forEach(function (point) {
      if (Utils.isEmptyPoint(point)) {
        numEmptyPoints++;
      }
    });

    // Don't allow submit if all points are empty
    if (numEmptyPoints === self.parent.request.points.length) {
      return false;
    }

    return self.parent.request.valid === true;
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
   */
  function generateValues() {
    SchemaService.generateTagnames(self.parent.request);
    SchemaService.generateFaultStates(self.parent.request);
    //SchemaService.generateAlarmCategories(self.parent.request);
  }
}
