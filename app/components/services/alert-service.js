'use strict';

/**
 * @ngdoc service
 * @name modesti.AlertService
 * @description # AlertService Service in the modesti.
 */
angular.module('modesti').service('AlertService', AlertService);

function AlertService($rootScope) {
  var self = this;

  // Create an array of globally available alerts
  $rootScope.alerts = [];

  /**
   * Public API for the alert service.
   */
  var service = {
    add: add,
    close: close,
    closeAlertByIndex: closeAlertByIndex
  };

  /**
   *
   * @param type
   * @param message
   */
  function add(type, message) {
    $rootScope.alerts.push({
      'type': type,
      'message': message,
      close: function () {
        return service.close(this);
      }
    })
  }

  /**
   *
   * @param alert
   * @returns {*}
   */
  function close(alert) {
    return this.closeAlertByIndex($rootScope.alerts.indexOf(alert));
  }

  /**
   *
   * @param index
   * @returns {Array.<T>}
   */
  function closeAlertByIndex(index) {
    return $rootScope.alerts.splice(index, 1);
  }

  return service;
}