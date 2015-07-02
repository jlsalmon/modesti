'use strict';

/**
 * @ngdoc service
 * @name modesti.RequestService
 * @description # RequestService Service in the modesti.
 */
angular.module('modesti').service('RequestService', RequestService);

function RequestService($filter, $rootScope, $q, Restangular) {
  var self = this;

  self.cache = {};

  // Public API
  var service = {
    getRequests: getRequests,
    getRequest: getRequest,
    getChildRequests: getChildRequests,
    saveRequest: saveRequest,
    createRequest: createRequest,
    deleteRequest: deleteRequest,
    clearCache: clearCache
  };

  /**
   *
   * @returns {*}
   */
  function getRequests() {
    var q = $q.defer();

    Restangular.all('requests').getList({'sort': 'requestId', 'requestId.dir': 'desc'}).then(function (requests) {
        q.resolve(requests.data);
      },

      function (error) {
        console.log('error: ' + error.statusText);
        q.reject(error);
      });

    return q.promise;
  }

  /**
   *
   * @param id
   * @param params
   * @param unsavedRequest
   * @returns {*}
   */
  function getRequest(id, params, unsavedRequest) {
    var q = $q.defer();

    if (self.cache[id]) {
      var request = self.cache[id];
      console.log('using cached request');

      // Merge the given potentially unsaved request with the cached
      // request. This is because we don't want to lose any unsaved
      // changes.
      if (unsavedRequest) {
        for (var i in unsavedRequest.points) {
          // If the point has been modified, update it
          update(request.points, unsavedRequest.points[i])
        }
      }

      // Make a copy for sorting/filtering
      request = Restangular.copy(request);

      if (params) {
        // Sort/filter the points
        request.points = transformData(request.points, params.filter(), params);
      }

      q.resolve(request);
    }

    else {
      console.log('fetching request ' + id);

      Restangular.one('requests', id).get().then(function (response) {
          var request = response.data;

          // Cache the request
          self.cache[request.requestId] = request;
          console.log('cached request (with ' + request.points.length + ' points)');

          // Make a copy for sorting/filtering
          request = Restangular.copy(request);

          if (params) {
            // Perform initial sorting/filtering/slicing
            request.points = transformData(request.points, params.filter(), params);
          }

          q.resolve(request);
        },

        function (error) {
          console.log(error.status + ' ' + error.statusText);
          q.reject(error);
        });
    }

    return q.promise;
  }

  /**
   *
   * @param request
   * @returns {*|{src}}
   */
  function getChildRequests(request) {
    var childRequestIds = request.childRequestIds;
    var promises = [];

    angular.forEach(childRequestIds, function(childRequestId) {
      promises.push(service.getRequest(childRequestId));
    });

    return $q.all(promises);
  }

  /**
   *
   * @param request
   * @returns {*}
   */
  function saveRequest(request) {
    $rootScope.saving = "started";
    var q = $q.defer();

    // Handle points that have been added, removed, modified or filtered since
    // the request was last cached.

    // Merge the given request with the cached request. This is because
    // the given request may have been filtered, and thus contain less
    // points. We don't want to accidentally delete points!
    //if (request.points.length < self.cache[request.requestId].points.length) {
    //  for (var i in self.cache[request.requestId].points) {
    //    var point = self.cache[request.requestId].points[i];
    //    // If the cached point isn't in the given request, add it
    //    if (!contains(request.points, point)) {
    //      request.points.push(point);
    //    }
    //  }
    //}
    //
    //
    //// Remove points that have been marked as deleted. Note that points
    //// that have been filtered out will not be deleted.
    //var i = request.points.length;
    //while (i--) {
    //  var point = request.points[i];
    //  if (point.deleted) {
    //    request.points.splice(request.points.indexOf(point), 1);
    //  }
    //}

    request.save().then(function (savedRequest) {
      console.log('saved request');

      // Cache the newly saved request
      self.cache[request.requestId] = savedRequest.data;

      q.resolve(self.cache[request.requestId]);
      $rootScope.saving = "success";

    }, function (error) {
      console.log('error saving request: ' + error.data.message);
      q.reject(error);
      $rootScope.saving = "error";
    });

    return q.promise;
  }

  /**
   *
   * @param request
   * @returns {*}
   */
  function createRequest(request) {
    var q = $q.defer();
    var requests = Restangular.all('requests');

    requests.post(request).then(function (response) {
        var location = response.headers('Location');
        console.log('created request: ' + location);
        q.resolve(location);
      },

      function (error) {
        console.log(error.data.message);
        for (var i in error.data.errors) {
          console.log('error: ' + error.data.errors[i].message);
        }
        q.reject(error);
      });

    return q.promise;
  }

  /**
   *
   * @param id
   * @returns {*}
   */
  function deleteRequest(id) {
    var q = $q.defer();

    Restangular.one('requests', id).remove().then(function (response) {
        console.log('deleted request: ' + response);
        q.resolve(response);
      },

      function (error) {
        console.log(error.status + ' ' + error.statusText);
        q.reject(error);
      });

    return q.promise;
  }

  /**
   *
   */
  function clearCache() {
    self.cache = {};
  }

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

  return service;
}
