'use strict';

/**
 * @ngdoc service
 * @name modesti.PointService
 * @description # PointService Service in the modesti.
 */
angular.module('modesti').service('PointService', PointService);

function PointService($http, $q) {

  // Public API
  var service = {
    getPoints: getPoints
  };

  /**
   *
   * @param query
   * @param page
   * @param size
   * @param sort
   * @returns {*}
   */
  function getPoints(query, page, size, sort) {
    var q = $q.defer();
    page = page || 0;
    size = size || 20;
    sort = sort || 'pointId,desc';

    $http.get(BACKEND_BASE_URL + '/points/ref',
    {
      params: {
        search: query,
        page: page - 1,
        size: size,
        sort:sort
      }
    }).then(function (response) {
      q.resolve(response.data);
    },

    function (error) {
      console.log('error: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  return service;
}
