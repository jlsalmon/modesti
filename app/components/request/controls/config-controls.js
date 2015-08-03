'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:ConfigControlsController
 * @description # ConfigControlsController Controller of the modesti
 */
angular.module('modesti').controller('ConfigControlsController', ConfigControlsController);

function ConfigControlsController($state, $http, $timeout, RequestService, TaskService, AlertService) {
  var self = this;

  self.request = {};
  self.tasks = {};
  self.progress = undefined;
  self.configuring = undefined;

  self.init = init;
  self.configure = configure;

  /**
   *
   */
  function init(parent) {
    self.request = parent.request;
    self.tasks = parent.tasks;
  }

  /**
   *
   */
  function configure(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks['configure'];
    if (!task) {
      console.log('error configuring request: no task');
      return;
    }

    self.configuring = 'started';

    // Start a timer loop to periodically poll for progress updates
    getProgress();

    // Complete the task
    TaskService.completeTask(task.name, self.request.requestId).then(function () {
      console.log('completed task ' + task.name);

      // Clear the cache so that the state reload also pulls a fresh request
      RequestService.clearCache();

      $state.reload().then(function() {
        self.configuring = 'success';

        AlertService.add('info', 'Your request has been configured successfully.')
      });
    },

    function (error) {
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

    $http.get(BACKEND_BASE_URL + '/requests/' + self.request.requestId + '/progress').then(function (response) {
      if (response.data) {
        self.progress = response.data;
      }

      if (self.configuring != 'success' && self.configuring != 'error') {
        $timeout(getProgress, 100);
      }
    });
  }
}