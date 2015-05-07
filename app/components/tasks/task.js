'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:TaskController
 * @description # TaskController Controller of the modesti
 */
angular.module('modesti').controller('TaskController', TaskController);

function TaskController(task) {
  var self = this;

  self.task = task;
}
