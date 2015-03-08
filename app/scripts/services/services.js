'use strict';

/**
 * @ngdoc service
 * @name verity.myService
 * @description
 * # myService
 * Service in the verity.
 */
var app = angular.module('verity');

app.service('RequestService', function($filter, $q, Restangular) {

  function filterData(data, filter) {
    return $filter('filter')(data, filter)
  }

  function orderData(data, params) {
    return params.sorting() ? $filter('orderBy')(data, params.orderBy()) : filteredData;
  }

  function sliceData(data, params) {
    return data.slice((params.page() - 1) * params.count(), params.page() * params.count())
  }

  function transformData(data, filter, params) {
    return sliceData(orderData(filterData(data, filter), params), params);
  }

  var service = {
    cachedRequest : null,

    getData : function($defer, params, filter) {

      var q = $q.defer();
      
      if (service.cachedRequest) {
        console.log('using cached request');
        var points = service.cachedRequest.points;
        var filteredData = filterData(points, filter);
        var transformedData = sliceData(orderData(filteredData, params), params);
        params.total(filteredData.length)
        service.cachedRequest.points = transformedData;

        //$defer.resolve(transformedData);
        q.resolve(service.cachedRequest);
      }

      else {
        console.log('fetching request');
        var baseRequests = Restangular.all('requests');

        // This will query /requests and return a promise.
        baseRequests.getList().then(function(requests) {
          var request = requests[0];
          var points = request.points;

          service.cachedRequest = request;
          console.log('cached request');
          params.total(points.length)
          var filteredData = $filter('filter')(points, filter);
          var transformedData = transformData(points, filter, params)

          service.cachedRequest.points = transformedData;
          
          //$defer.resolve(transformedData);
          q.resolve(service.cachedRequest);
        });
      }
      
      return q.promise;
    }
  };

  return service;
});