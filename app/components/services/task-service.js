'use strict';

/**
 * @ngdoc service
 * @name modesti.TaskService
 * @description # TaskService Service in the modesti.
 */
var app = angular.module('modesti');

app.service('TaskService', function($q, Restangular) {

  var service = {

    /**
     *
     */
    getTask : function(id) {
      var q = $q.defer();
      
      Restangular.one('runtime/tasks', id).get().then(function(response) {
        var task = response.data;
        q.resolve(task);
      },

      function(error) {
        console.log(error.status + ' ' + error.statusText);
        q.reject(error);
      });
      
      return q.promise;
    }
  };

  return service;
});