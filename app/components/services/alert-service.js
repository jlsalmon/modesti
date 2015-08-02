'use strict';

/**
 * @ngdoc service
 * @name modesti.AlertService
 * @description # AlertService Service in the modesti.
 */
angular.module('modesti').service('AlertService', AlertService);

function AlertService($rootScope, $timeout) {
  var self = this;

  // Create an array of globally available alerts
  $rootScope.alerts = [];

  /**
   * Public API for the alert service.
   */
  var service = {
    add: add,
    close: close,
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
        return service.close(this);
      }
    }
    
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
  
  /**
   * 
   */
  function clear() {
    angular.forEach($rootScope.alerts, function(alert) {
      alert.close();
    })
  }

  return service;
}