'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController(request, children, schema, tasks) {
  var self = this;

  self.request = request;
  self.children = children;
  self.schema = schema;
  self.tasks = tasks;
  self.currentActiveTab = 0;

  self.activateTab = activateTab;

  /**
   * Activate a particular tab
   */
  function activateTab(tab) {
    self.currentActiveTab = tab;
  }
}