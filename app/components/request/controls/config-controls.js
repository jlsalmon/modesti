'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:ConfigController
 * @description # ConfigController
 */
angular.module('modesti').controller('ConfigController', ConfigController);

function ConfigController($scope, $state, $http, $timeout, RequestService, TaskService, AlertService) {
  var self = this;
  self.parent = $scope.$parent.ctrl;

  self.progress = undefined;
  self.configuring = undefined;

  self.configure = configure;

  /**
   *
   */
  function configure(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.parent.tasks.configure;
    if (!task) {
      console.log('error configuring request: no task');
      return;
    }

    self.configuring = 'started';

    // Start a timer loop to periodically poll for progress updates
    getProgress();

    // Complete the task
    TaskService.completeTask(task.name, self.parent.request.id).then(function () {
      console.log('completed task ' + task.name);

      // Clear the cache so that the state reload also pulls a fresh request
      RequestService.clearCache();

      $state.reload().then(function() {
        // Get the request once again from the cache
        RequestService.getRequest(self.parent.request.id).then(function (request) {
          self.parent.request = request;

          var configurationResult = self.parent.request.properties.configurationResult;
          var url = '<a href="' + configurationResult.reportUrl + '" target="_blank">' + configurationResult.reportUrl + '</a>';

          // Show an alert if the configuration failed.
          if (!configurationResult || configurationResult.success === false) {
            self.configuring = 'error';
            AlertService.add('danger', 'Configuration failed, please see error log for details. Full configuration report available at: ' + url);
          }
          else {
            self.configuring = 'success';
            AlertService.add('success', 'Request has been configured successfully. Full configuration report available at:' + url);
          }
        });
      });
    },

    function () {
      console.log('error completing task ' + task.name);
      self.configuring = 'error';
    });
  }

  /**
   *
   * @returns
   */
  function getProgress() {
    console.log('checking progress');
    var url = BACKEND_BASE_URL + '/requests/' + self.parent.request.id + '/progress';

    $http.get(url).then(function (response) {
      if (response.data) {
        self.progress = response.data;
      }

      if (self.configuring !== 'success' && self.configuring !== 'error') {
        $timeout(getProgress, 100);
      }
    });
  }
}
