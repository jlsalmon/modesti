'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:UserRequestsController
 * @description # UserRequestsController Controller of the modesti
 */
angular.module('modesti').controller('UserRequestsController', UserRequestsController);

function UserRequestsController($location, RequestService) {
  var self = this;
  
  self.filter = {
      status: '',
      domain: '',
      subsystem: '',
      assignee: '',
      type: ''
  };

  self.deleteRequest = deleteRequest;
  self.editRequest = editRequest;
  
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
};
