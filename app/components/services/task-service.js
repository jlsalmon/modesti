'use strict';

/**
 * @ngdoc service
 * @name modesti.TaskService
 * @description # TaskService Service in the modesti.
 */
var app = angular.module('modesti');

app.service('TaskService', function($q, $localStorage, Restangular) {

  var service = {

    /**
     *
     */
    getTaskForRequest : function(requestId) {
      var q = $q.defer();

      var query = {
        processInstanceVariables : [ {
          name : 'requestId',
          value : requestId,
          operation : 'equals'
        } ]
      };

      // Find the task related to this request. There should only be one
      Restangular.one('query/tasks').post('', query).then(function(result) {
        var task = result.data.data[0];
        console.log('found task ' + task.id + ' for request ' + requestId);
        q.resolve(task);
      },

      function(error) {
        console.log('error querying tasks');
        q.reject(error);
      });

      return q.promise;
    },

    /**
     *
     */
    claimTask : function(taskId) {
      var q = $q.defer();

      var params = {
        action : 'claim',
        assignee : $localStorage.username
      };

      Restangular.one('runtime/tasks', taskId).post('', params).then(function(result) {
        console.log('claimed task ' + taskId + ' as user ' + params.assignee);
        q.resolve(result);
      },

      function(error) {
        console.log('error claiming task ' + taskId);
        q.reject(error);
      });

      return q.promise;
    },

    /**
     *
     */
    completeTask : function(taskId) {
      var q = $q.defer();

      var params = {
        "action" : "complete"
      };

      Restangular.one('runtime/tasks', taskId).post('', params).then(function(result) {
        console.log('completed task ' + taskId);
        q.resolve(result);
      },

      function(error) {
        console.log('error completing task ' + taskId);
        q.reject(error);
      });

      return q.promise;
    }
  };

  return service;
});