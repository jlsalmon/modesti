'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:UserRequestsController
 * @description # UserRequestsController Controller of the modesti
 */
angular.module('modesti').controller('UserRequestsController', UserRequestsController);

function UserRequestsController($location, $localStorage, Restangular, RequestService, TaskService) {
  var self = this;

  self.filter = {
      status: '',
      domain: '',
      subsystem: {
        system: '',
        subsystem: ''
      },
      categories: '',
      creator: {
        username: ''
      },
      type: ''
  };

  self.deleteRequest = deleteRequest;
  self.editRequest = editRequest;
  self.claimTask = claimTask;

  getRequests();

  /**
   *
   */
  function getRequests() {
    RequestService.getRequests().then(function(requests) {
      self.requests = requests;
    },

    function(error) {
      // what to do here?
    });
  }

  /**
   *
   */
  function deleteRequest(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1);

    RequestService.deleteRequest(id).then(function() {
      console.log('deleted request ' + id);
      self.requests.splice(self.requests.indexOf(request), 1);
    },

    function(error) {
      // something went wrong deleting the request
    });
  }

  /**
   *
   */
  function editRequest(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1);

    $location.path('/requests/' + id);
  }

  /**
   *
   */
  function claimTask(request) {

    TaskService.getTaskForRequest(request.requestId).then(function(task){
      TaskService.claimTask(task.id).then(function(task){
        $location.path('/requests/' + request.requestId);
      },

      function(error) {
        console.log('error claiming task ' + id);
      });
    },

    function(error) {
      console.log('error querying tasks');
    });
  }
};
