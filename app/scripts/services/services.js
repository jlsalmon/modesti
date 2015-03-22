'use strict';

/**
 * @ngdoc service
 * @name verity.myService
 * @description # myService Service in the verity.
 */
var app = angular.module('verity');

app.service('RequestService', function($filter, $q, Restangular) {

  function filterData(data, filter) {
    var filtered = $filter('filter')(data, filter);
    
    for (var i in data) {
      var point = data[i];
      
      if (filtered.indexOf(point) == -1) {
        point.hidden = true;
      } else {
        point.hidden = false;
      }
    }
    
    return data;
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

    getRequests : function() {
      var q = $q.defer();
      
      Restangular.all('requests').getList().then(function(requests) {
        q.resolve(requests.data);
      },
      
      function(error) {
        console.log('error: ' + error);
        q.reject(error);
      });
      
      return q.promise;
    },
    
    getRequest : function(id, params, filter) {
      var q = $q.defer();

      if (service.cachedRequest && service.cachedRequest.id == id) {
        console.log('using cached request');
        
        // Make a copy for sorting/filtering
        var request = service.cachedRequest;

        // Sort/filter the points
        request.points = transformData(request.points, filter, params);
        
        // Set length for pagination
        params.total(request.points.length);

        q.resolve(request);
      }

      else {
        console.log('fetching request ' + id);

        Restangular.one('requests', id).get().then(function(request) {
          var points = request.data.points;

          if (points.length == 0) {
            // Add a starter row with some pre-filled data
            points.push({
              'name' : '',
              'description' : '',
              'domain' : request.domain
            });
          }

          service.cachedRequest = request.data;
          service.cachedRequest.id = id;
          console.log('cached request');
          
          //var filteredData = $filter('filter')(points, filter);
          var transformedData = transformData(points, filter, params);
          params.total(transformedData.length);

          //service.cachedRequest.points = transformedData;
          q.resolve(service.cachedRequest);
        },

        function(error) {
          console.log(error.status + ' ' + error.statusText);
          q.reject(error);
        });
      }

      return q.promise;
    },

    createRequest : function(request) {
      var q = $q.defer();
      var requests = Restangular.all('requests');

      requests.post(request).then(function(response) {
        var location = response.headers('Location');
        console.log('created request: ' + location);
        q.resolve(location);
      },

      function(error) {
        console.log(error.data.message);
        for ( var i in error.data.errors) {
          console.log('error: ' + error.data.errors[i].message);
        }
        q.reject(error);
      });

      return q.promise;
    },
    
    deleteRequest : function(id) {
      var q = $q.defer();
      
      Restangular.one('requests', id).remove().then(function(response) {
        console.log('deleted request: ' + response);
        q.resolve(response);
      },

      function(error) {
        console.log(error.status + ' ' + error.statusText);
        q.reject(error);
      });
      
      return q.promise;
    }
  };

  return service;
});