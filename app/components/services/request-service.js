'use strict';

/**
 * @ngdoc service
 * @name modesti.RequestService
 * @description # RequestService
 */
angular.module('modesti').service('RequestService', RequestService);

function RequestService($http, $rootScope, $q, Restangular, AuthService) {
  var self = this;

  self.cache = {};

  // Public API
  var service = {
    getRequests: getRequests,
    getRequest: getRequest,
    getRequestHistory: getRequestHistory,
    getChildRequests: getChildRequests,
    saveRequest: saveRequest,
    createRequest: createRequest,
    cloneRequest: cloneRequest,
    deleteRequest: deleteRequest,
    isCurrentUserOwner : isCurrentUserOwner,
    getRequestMetrics: getRequestMetrics,
    clearCache: clearCache
  };

  /**
   *
   * @returns {*}
   */
  function getRequests(page, size, sort, filter) {
    var q = $q.defer();
    page = page || 0;
    size = size || 15;
    sort = sort || 'createdAt,desc';

    $http.get(BACKEND_BASE_URL + '/requests/search',
    {
      params: {
        query: parseQuery(filter),
        page: page - 1,
        size: size,
        sort: sort
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

  /**
   *
   * @param filter
   * @returns {string}
   */
  function parseQuery(filter) {
    var expressions = [];

    for (var property in filter) {
      if (typeof filter[property] === 'string' && filter[property] !== '') {
        expressions.push(property.toString() + '=="' + filter[property] + '"');
      }

      else if (filter[property] instanceof Array && filter[property].length > 0) {
        expressions.push(property.toString() + '=in=' + '("' + filter[property].join('","') + '")');
      }

      else if (typeof filter[property] === 'object') {
        for (var subProperty in filter[property]) {

          if (typeof filter[property][subProperty] === 'string' && filter[property][subProperty] !== '') {
            expressions.push(property.toString() + '.' + subProperty.toString() + '=="' + filter[property][subProperty] + '"');
          }

          else if (filter[property][subProperty] instanceof Array && filter[property][subProperty].length > 0) {
            expressions.push(property.toString() + '.' + subProperty.toString() + '=in=' + '("' + filter[property][subProperty].join('","') + '")');
          }
        }
      }
    }

    var query = expressions.join('; ');

    console.log('parsed query: ' + query);
    return query;
  }

  /**
   *
   * @param id
   * @returns {*}
   */
  function getRequest(id) {
    var q = $q.defer();
    console.log('fetching request ' + id);

    Restangular.one('requests', id).get().then(function (response) {
      var request = response.data;

      // Make a copy for sorting/filtering
      request = Restangular.copy(request);

      q.resolve(request);
    },

    function (error) {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });


    return q.promise;
  }

  function getRequestHistory(id) {
    var q = $q.defer();

    console.log('fetching history for request ' + id);

    Restangular.one('requestHistories', id).get().then(function (response) {
      var history = response.data;
      q.resolve(history);
    },

    function (error) {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });

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

    angular.forEach(childRequestIds, function (childRequestId) {
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
    $rootScope.saving = 'started';
    var q = $q.defer();

    $http.put(BACKEND_BASE_URL + '/requests/' + request.requestId, request).then(function (response) {
      console.log('saved request');

      // Cache the newly saved request
      self.cache[request.requestId] = response.data;

      q.resolve(self.cache[request.requestId]);
      $rootScope.saving = 'success';

    }, function (error) {
      console.log('error saving request: ' + error.data.message);
      q.reject(error);
      $rootScope.saving = 'error';
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
      console.log(error.statusText);
      for (var i in error.data.errors) {
        console.log('error: ' + error.data.errors[i].message);
      }
      q.reject(error);
    });

    return q.promise;
  }

  /**
   *
   * @param request
   * @param schema
   * @returns {*}
   */
  function cloneRequest(request, schema) {
    var clone = {
      domain: request.domain,
      type : request.type,
      description : request.description,
      creator : AuthService.getCurrentUser().username,
      //subsystem: request.subsystem,
      points: request.points.slice(),
      properties: {}
    };

    // Clone request-level properties that are defined in the schema
    if (schema.fields) {
      schema.fields.forEach(function (field) {
        if (request.properties.hasOwnProperty(field.id)) {
          clone.properties[field.id] = request.properties[field.id];
        }
      });
    }

    clone.points.forEach(function (point) {
      point.dirty = true;
      point.selected = false;
      point.errors = [];

      // TODO: delete properties that are not in the schema
      delete point.properties.valid;
      delete point.properties.approvalResult;
      delete point.properties.addressingResult;
      delete point.properties.cablingResult;
      delete point.properties.testResult;
      delete point.properties.timTagId;
    });

    return createRequest(clone);
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
   * @param request
   * @returns {boolean}
   */
  function isCurrentUserOwner(request) {
    var user = AuthService.getCurrentUser();
    if (!user) {
      return false;
    }

    return user && user.username === request.creator;
  }

  /**
   *
   * @returns {*}
   */
  function getRequestMetrics() {
    var q = $q.defer();

    $http.get(BACKEND_BASE_URL + '/metrics').then(function (response) {
      q.resolve(response.data);
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

  ///**
  // *
  // * @param data
  // * @param filter
  // * @returns {*}
  // */
  //function filterData(data, filter) {
  //  return $filter('filter')(data, filter);
  //}
  //
  ///**
  // *
  // * @param data
  // * @param params
  // * @returns {*}
  // */
  //function orderData(data, params) {
  //  return params.sorting() ? $filter('orderBy')(data, params.orderBy()) : data;
  //}
  //
  ///**
  // *
  // * @param data
  // * @param filter
  // * @param params
  // * @returns {*}
  // */
  //function transformData(data, filter, params) {
  //  return orderData(filterData(data, filter), params);
  //}
  //
  ///**
  // * If the given array contains a modified version of the given point, this
  // * function will update it in the array.
  // */
  //function update(array, point) {
  //  var i = array.length;
  //  while (i--) {
  //    if (array[i].lineNo === point.lineNo && angular.toJson(array[i]) !== angular.toJson(point)) {
  //      array[i] = point;
  //    }
  //  }
  //}

  return service;
}
