'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TasksController
 * @description # TasksController Controller of the modesti
 */
angular.module('modesti').controller('TasksController', TasksController);

function TasksController($location, $localStorage, Restangular) {
  var self = this;

  self.claimTask = claimTask;
  
  // TODO refactor this into a service
  Restangular.all('runtime/tasks').getList().then(function(result) {
    console.log('got ' + result.data.length + ' tasks');
    self.tasks = result.data;
  });
  
  function claimTask(task) {
    var id = task.id;
    
    var params = {
      action : 'claim',
      assignee : $localStorage.username
    }
    
    Restangular.one('runtime/tasks', id).post('', params).then(function(result) {
      console.log('claimed task ' + id + ' as user ' + params.assignee);
      $location.path('/tasks/' + id);
    });
  }
}
