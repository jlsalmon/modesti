'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController($scope, request, schema, task) {
  var self = this;

  self.request = request;
  self.schema = schema;
  self.task = task;
  self.currentActiveTab = 0;

  self.activateTab = activateTab;

  /**
   * Activate a particular tab
   */
  function activateTab(tab) {
    self.currentActiveTab = tab;
  }
}