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
   * @returns {*}
   */
  function getPoints() {
    var q = $q.defer();

    $http.get(BACKEND_BASE_URL + '/points/', {params: {lineNo: 1}}).then(function (points) {
      q.resolve(points.data);
    },

    function (error) {
      console.log('error: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  return service;
}
