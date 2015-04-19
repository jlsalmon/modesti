'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController($scope, request, schema) {
  var self = this;

  self.request = request;
  self.schema = schema;
  
  //self.request = {};
  //self.schema = {};
  self.currentActiveTab = 0;

//  self.resolve = {
//      request: getRequest(),
//      schema:  getSchema()
//  };
  
//  self.getRequest = getRequest;
//  self.getSchema = getSchema;
  self.activateTab = activateTab;

//  getRequest();
//  getSchema();

  /**
   * Called by the route provider to resolve data dependencies before the
   * controller is activated.
   */
//  function resolve($q, $route) {
//    console.log('resolving');
//    var q = $q.defer();
//    
//    
//  }

//  /**
//   * 
//   */
//  function getRequest($q, $route, RequestService) {
//    console.log('fetching request');
//    var q = $q.defer();
//    var id = $route.current.params.key;
//
//    RequestService.getRequest(id).then(function(request) {
//      //self.request = request;
//      console.log('fetched request (with ' + request.points.length + ' points)');
//      q.resolve(request);
//    },
//
//    function(error) {
//      console.log('error fetching request: ' + error);
//      q.reject();
//    });
//    
//    return q.promise;
//  }
//
//  /**
//   * 
//   */
//  function getSchema($q, $http) {
//    console.log('fetching schema');
//    var q = $q.defer();
//    var id = '55327aaba826c50bae333ae4'; // $routeParams.id;
//
//    // TODO refactor this into a service
//    $http.get('http://localhost:8080/schemas/' + id).then(function(response) {
//      console.log('fetched schema: ' + response.data.name);
//      q.resolve(response.data);
//      //self.schema = response.data;
//    },
//    
//    function(error) {
//      console.log('error fetching schema: ' + error);
//      q.reject();
//    });
//    
//    return q.promise;
//  }

  /**
   * Activate a particular tab
   */
  function activateTab(tab) {
    self.currentActiveTab = tab;
  }
}