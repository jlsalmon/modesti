'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:HistoryModalController
 * @description # HistoryModalController Controller of the modesti
 */
angular.module('modesti').controller('HistoryModalController', HistoryModalController);

function HistoryModalController($modalInstance, request, history) {
  var self = this;

  self.request = request;
  self.history = history;

  self.ok = ok;

  function ok() {
    $modalInstance.close();
  }
}
