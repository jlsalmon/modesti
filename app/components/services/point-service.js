'use strict';

/**
 * @ngdoc service
 * @name modesti.PointService
 * @description # PointService Service in the modesti.
 */
angular.module('modesti').service('PointService', PointService);

function PointService($http, $q) {
  var self = this;


  // Public API
  var service = {
    getPoints: getPoints
  };

  /**
   *
   * @param query
   * @returns {*}
   */
  function getPoints(query) {
    var q = $q.defer();

    $http.get(BACKEND_BASE_URL + '/points/', {params: {search: query}}).then(function (response) {
      var points = [];

      if (response.data.hasOwnProperty('_embedded')) {
        points = response.data._embedded.points
      }

      q.resolve(points);
    },

    function (error) {
      console.log('error: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  return service;
}
