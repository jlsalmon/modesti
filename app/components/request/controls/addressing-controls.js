'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AddressingControlsController
 * @description # AddressingControlsController Controller of the modesti
 */
angular.module('modesti').controller('AddressingControlsController', AddressingControlsController);

function AddressingControlsController($state, RequestService, TaskService) {
  var self = this;

  self.parent = {};
  self.request = {};
  self.tasks = {};

  self.submitting = undefined;
  self.addressed = true;

  self.init = init;
  self.isCurrentUserAuthorised = isCurrentUserAuthorised;
  self.isCurrentTaskClaimed = isCurrentTaskClaimed;
  self.isCurrentUserAssigned = isCurrentUserAssigned;
  self.claim = claim;
  self.addressSelectedPoints = addressSelectedPoints;
  self.rejectSelectedPoints = rejectSelectedPoints;
  self.submit = submit;

  /**
   *
   */
  function init(parent) {
    self.parent = parent;
    self.request = parent.request;
    self.tasks = parent.tasks;
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAuthorised() {
    return TaskService.isCurrentUserAuthorised(self.tasks['address']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentTaskClaimed() {
    return TaskService.isTaskClaimed(self.tasks['address']);
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAssigned() {
    return TaskService.isCurrentUserAssigned(self.tasks['address']);
  }

  /**
   *
   */
  function claim() {
    TaskService.claimTask(self.tasks['address'].name, self.request.requestId).then(function (task) {
      console.log('claimed task successfully');
      self.tasks['address'] = task;
      self.parent.activateDefaultCategory();
    });
  }

  /**
   *
   */
  function addressSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.addressed = true;
  }

  /**
   * Mark the currently selected points as rejected.
   */
  function rejectSelectedPoints(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.addressed = false;
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks['address'];
    if (!task) {
      console.log('error addressing request: no task');
      return;
    }

    self.submitting = 'started';

    var addressingResult;

    if (self.addressed) {
      addressingResult = {
        addressed: true,
        errors: []
      };
    } else {
      addressingResult = {
        addressed: false,
        errors: [
          'Point 1 is not addressed because reasons',
          'Point 2 is not addressed because reasons'
        ]
      };
    }

    //// Send the approval result as a JSON string
    //var variables = [{
    //  "name": "addressingResult",
    //  "value": JSON.stringify(addressingResult),
    //  "type": "string"
    //}];

    TaskService.completeTask(task.name, self.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function() {
          self.submitting = 'success';
        });
      },

      function (error) {
        console.log('error completing task ' + task.name);
        self.submitting = 'error';
      });
  }
}