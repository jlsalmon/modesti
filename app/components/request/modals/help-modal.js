'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:HelpModalController
 * @description # HelpModalController Controller of the modesti
 */
angular.module('modesti').controller('HelpModalController', HelpModalController);

function HelpModalController($modalInstance) {
  var self = this;

  self.close = close;

  function close() {
    $modalInstance.close();
  }
}
