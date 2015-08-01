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
  function configure() {
    var task = self.tasks['configure'];
    if (!task) {
      console.log('error configuring request: no task');
      return;
    }

    self.configuring = 'started';

    // Start a timer loop to periodically poll for progress updates
    getProgress();

    // Complete the task
    TaskService.completeTask(task.id, []).then(function (task) {
      console.log('completed task ' + task.id);

      // Clear the cache so that the state reload also pulls a fresh request
      RequestService.clearCache();

      $state.reload().then(function() {
        self.configuring = 'success';
        
        AlertService.add('info', 'Your request has been configured successfully.')
      });
    },

    function (error) {
      console.log('error completing task ' + task.id);
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
      self.progress = response.data;

      if (self.configuring != 'success' && self.configuring != 'error') {
        $timeout(getProgress, 100);
      }
    });
  }
}