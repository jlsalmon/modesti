'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TasksController
 * @description # TasksController Controller of the modesti
 */
angular.module('modesti').controller('TasksController', TasksController);

function TasksController(Restangular) {
  var self = this;

  // TODO refactor this into a service
  Restangular.all('runtime/tasks').getList().then(function(result) {
    console.log('got ' + result.data.length + ' tasks');
    self.tasks = result.data;
  });
}
