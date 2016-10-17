'use strict';

/**
 * @ngdoc service
 * @name modesti.HistoryService
 * @description # HistoryService
 */
angular.module('modesti').service('HistoryService', HistoryService);

function HistoryService($q, Restangular) {

  /**
   * Public API for the history service.
   */
  var service = {
    getHistory: getHistory
  };

  /**
   *
   * @param requestId
   */
  function getHistory(requestId) {
    var q = $q.defer();

    Restangular.one('requests/' + requestId + '/history').get().then(function (response) {
      console.log('fetched history for request ' + requestId);

      var history = response.data._embedded.historicEvents;
      q.resolve(history);
    },

    function (error) {
      console.log('error querying history for request ' + requestId + ': ' + error.data.message);
      q.reject(error);
    });

    return q.promise;
  }

  return service;
}
