'use strict';

/**
 * @ngdoc service
 * @name modesti.RequestService
 * @description # RequestService Service in the modesti.
 */
var app = angular.module('modesti');

app.service('RequestService', function($filter, $rootScope, $q, Restangular) {

  var service = {

    /**
     *
     */
    cachedRequest : null,

    /**
     *
     */
    getRequests : function() {
      var q = $q.defer();

      Restangular.all('requests').getList().then(function(requests) {
        q.resolve(requests.data);
      },

      function(error) {
        console.log('error: ' + error.statusText);
        q.reject(error);
      });

      return q.promise;
    },

    /**
     *
     */
    getRequest : function(id, params, unsavedRequest) {
      var q = $q.defer();

      if (service.cachedRequest && service.cachedRequest.requestId == id) {
        console.log('using cached request');

        // Merge the given potentially unsaved request with the cached
        // request. This is because we don't want to lose any unsaved
        // changes.
        if (unsavedRequest) {
          for ( var i in unsavedRequest.points) {
            // If the point has been modified, update it
            update(service.cachedRequest.points, unsavedRequest.points[i])
          }
        }

        // Make a copy for sorting/filtering
        var request = Restangular.copy(service.cachedRequest);

        if (params) {
          // Sort/filter the points
          request.points = transformData(request.points, params.filter(), params);
        }

        q.resolve(request);
      }

      else {
        console.log('fetching request ' + id);

        Restangular.one('requests', id).get().then(function(request) {
          var points = request.data.points;

          if (points.length == 0) {
            // Add a starter row
            points.push({properties: {}});
          }

          // Cache the request
          service.cachedRequest = request.data;
          //service.cachedRequest.id = id;
          console.log('cached request (with ' + service.cachedRequest.points.length + ' points)');

          // Make a copy for sorting/filtering
          request = Restangular.copy(service.cachedRequest);

          if (params) {
            // Perform initial sorting/filtering/slicing
            request.points = transformData(request.points, params.filter(), params);
          }

          q.resolve(request);
        },

        function(error) {
          console.log(error.status + ' ' + error.statusText);
          q.reject(error);
        });
      }

      return q.promise;
    },

    /**
     *
     */
    saveRequest : function(request) {
      $rootScope.saving = "started";
      var q = $q.defer();

      // Handle points that have been added, removed, modified or filtered since
      // the request was last cached.

      // Merge the given request with the cached request. This is because
      // the given request may have been filtered, and thus contain less
      // points. We don't want to accidentally delete points!
      if (request.points.length < service.cachedRequest.points.length) {
        for (var i in service.cachedRequest.points) {
          var point = service.cachedRequest.points[i];
          // If the cached point isn't in the given request, add it
          if (!contains(request.points, point)) {
            request.points.push(point);
          }
        }
      }


      // Remove points that have been marked as deleted. Note that points
      // that have been filtered out will not be deleted.
      var i = request.points.length;
      while (i--) {
        var point = request.points[i];
        if (point.deleted) {
          request.points.splice(request.points.indexOf(point), 1);
        }
      }

      request.save().then(function(savedRequest) {
        console.log('saved request');

        // Cache the newly saved request
        service.cachedRequest = savedRequest.data;

        q.resolve(service.cachedRequest);
        $rootScope.saving = "success";

      }, function(error) {
        console.log('error saving request: ' + error.data.message);
        q.reject(error);
        $rootScope.saving = "error";
      });

      return q.promise;
    },

    /**
     *
     */
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

    /**
     *
     */
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
    },

    /**
     * 
     */
    clearCache : function() {
      delete service.cachedRequest;
    }
  };

  return service;

  /**
   *
   * @param data
   * @param filter
   * @returns {*}
   */
  function filterData(data, filter) {
    return $filter('filter')(data, filter);
  }

  /**
   *
   * @param data
   * @param params
   * @returns {*}
   */
  function orderData(data, params) {
    return params.sorting() ? $filter('orderBy')(data, params.orderBy()) : data;
  }

  /**
   *
   * @param data
   * @param filter
   * @param params
   * @returns {*}
   */
  function transformData(data, filter, params) {
    return orderData(filterData(data, filter), params);
  }

  /**
   *
   */
  function contains(array, point) {
    var i = array.length;
    while (i--) {
      if (array[i].id == point.id) {
        return true;
      }
    }
    return false;
  }


  /**
   * If the given array contains a modified version of the given point, this
   * function will update it in the array.
   */
  function update(array, point) {
    var i = array.length;
    while (i--) {
      if (array[i].id == point.id && angular.toJson(array[i]) != angular.toJson(point)) {
        array[i] = point;
      }
    }
  }
});