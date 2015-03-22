'use strict';

/**
 * @ngdoc function
 * @name verity.controller:UserRequestsController
 * @description # UserRequestsController Controller of the verity
 */
var app = angular.module('verity');

app.controller('UserRequestsController', function($scope, $location, RequestService) {
  
  RequestService.getRequests().then(function(requests) {
    $scope.requests = requests;
  },
  
  function(error) {
    // what to do here?
    $scope.requests = [];
  })
  
  $scope.deleteRequest = function(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1);
    
    RequestService.deleteRequest(id).then(function() {
      console.log('deleted request ' + id);
      $scope.requests.splice($scope.requests.indexOf(request), 1);
    },
    
    function(error) {
      // something went wrong deleting the request
    });
  };
  
  $scope.editRequest = function(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1);
    
    $location.path('/requests/' + id);
  }
});
