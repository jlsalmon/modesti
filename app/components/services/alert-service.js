'use strict';

/**
 * @ngdoc service
 * @name modesti.AlertService
 * @description # AlertService
 */
angular.module('modesti').service('AlertService', AlertService);

function AlertService($rootScope, $timeout) {

  // Create an array of globally available alerts
  $rootScope.alerts = [];

  /**
   * Public API for the alert service.
   */
  var service = {
    add: add,
    closeAlert: closeAlert,
    closeAlertByIndex: closeAlertByIndex,
    clear: clear
  };

  /**
   *
   * @param type
   * @param message
   * @param timeout
   */
  function add(type, message, timeout) {
    timeout = typeof timeout !== 'undefined' ? timeout : 10000;

    var alert = {
      'type': type,
      'message': message,
      close: function () {
        return service.closeAlert(this);
      }
    };

    $rootScope.alerts.push(alert);

    $timeout(function() {
      alert.close();
    }, timeout);
  }

  /**
   *
   * @param alert
   * @returns {*}
   */
  function closeAlert(alert) {
    return closeAlertByIndex($rootScope.alerts.indexOf(alert));
  }

  /**
   *
   * @param index
   * @returns {Array.<T>}
   */
  function closeAlertByIndex(index) {
    return $rootScope.alerts.splice(index, 1);
  }

  /**
   *
   */
  function clear() {
    angular.forEach($rootScope.alerts, function(alert) {
      alert.close();
    });
  }

  return service;
}
