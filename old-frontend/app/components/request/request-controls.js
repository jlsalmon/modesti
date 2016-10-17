'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController
 */
angular.module('modesti').directive('requestControls', function($compile, $http, $ocLazyLoad) {
  return {
    scope: {},
    bindToController: {
      request: '=',
      tasks: '=',
      schema: '=',
      table: '='
    },

    controller: BaseController,
    controllerAs: 'ctrl',

    link: function(scope, element) {
      var schemaId = scope.ctrl.request.domain;
      var status = scope.ctrl.request.status.split('_').join('-').toLowerCase();

      $http.get(BACKEND_BASE_URL + '/plugins/' + schemaId + '/assets').then(function (response) {
        var assets = response.data;
        console.log(assets);

        $ocLazyLoad.load(assets, {serie: true}).then(function() {

          var template = '<div ' + status + '-controls request="ctrl.request" tasks="ctrl.tasks" schema="ctrl.schema" table="ctrl.table"></div>';
          element.append($compile(template)(scope));
        });
      });
    }
  };
});

function BaseController($scope, $state, TaskService, ValidationService, AlertService) {
  var self = this;

  self.parent = $scope.$parent.ctrl;

  // Function definitions
  self.claim = claim;
  self.validate = validate;
  self.submit = submit;
  self.getNumValidationErrors = getNumValidationErrors;
  self.stopEvent = stopEvent;

  /**
   *
   */
  function claim(event) {
    stopEvent(event);
    TaskService.assignTaskToCurrentUser(self.request);
  }

  /**
   *
   */
  function validate(event) {
    stopEvent(event);

    AlertService.clear();
    self.validating = 'started';

    ValidationService.validateRequest(self.request).then(function (request) {
      // Save the reference to the validated request
      self.request = request;

      // Render the table to show the error highlights
      self.table.render();

      if (self.request.valid === false) {
        self.validating = 'error';
        AlertService.add('danger', 'Request failed validation with ' + getNumValidationErrors() + ' errors');
      } else {
        self.validating = 'success';
        AlertService.add('success', 'Request has been validated successfully');
      }
    },

    function (error) {
      console.log('error validating request: ' + error.statusText);
      self.validating = 'error';
    });
  }

  /**
   *
   */
  function submit(event) {
    stopEvent(event);

    var task = TaskService.getCurrentTask();

    AlertService.clear();
    self.submitting = 'started';
    var previousStatus = self.request.status;

    // Complete the task associated with the request
    TaskService.completeTask(task.name, self.request).then(function (request) {
      console.log('completed task ' + task.name);

      self.request = request;
      self.submitting = 'success';

      // If the request is now FOR_CONFIGURATION, no need to go away from the request page
      if (self.request.status === 'FOR_CONFIGURATION') {
        AlertService.add('info', 'Request has been submitted successfully and is ready to be configured.');
      }

      if (self.request.status === 'CLOSED') {
        AlertService.add('info', 'Request has been submitted successfully and is now closed.');
      }

      // If the request is in any other state, show a page with information about what happens next
      else {
        $state.go('submitted', {id: self.request.requestId, previousStatus: previousStatus});
      }
    });
  }

  /**
   *
   */
  function getNumValidationErrors() {
    var n = 0;

    if (self.request.hasOwnProperty('points')) {
      self.request.points.forEach(function (point) {
        if (point.hasOwnProperty('errors')) {
          point.errors.forEach(function (error) {
            n += error.errors.length;
          });
        }
      });
    }

    return n;
  }

  function stopEvent(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
  }
}
