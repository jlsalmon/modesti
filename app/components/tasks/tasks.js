'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TasksController
 * @description # TasksController Controller of the modesti
 */
angular.module('modesti').controller('TasksController', TasksController);

function TasksController($location, $localStorage, Restangular, RequestService) {
  var self = this;

  self.claimTask = claimTask;

  // TODO refactor this into a service
  Restangular.all('runtime/tasks?size=100').getList().then(function(result) {
    console.log('got ' + result.data.length + ' tasks');
    self.tasks = result.data;
  });

  function claimTask(task) {
    var id = task.id;

    var params = {
      action : 'claim',
      assignee : $localStorage.user.name
    };

    Restangular.one('runtime/tasks', id).post('', params).then(function(result) {
      console.log('claimed task ' + id + ' as user ' + params.assignee);

      Restangular.one('runtime/tasks/' + id + '/variables/requestId').get().then(function(response) {
        var requestId = response.data.value;
        console.log('got request id varirable ' + requestId + ' from task ' + id);
        $location.path('/requests/' + requestId);
      },

      function(error) {
        console.log('error getting request variable from task ' + id);
      });
    },

    function(error) {
      console.log('error claiming task ' + id);
    });
  }
}
