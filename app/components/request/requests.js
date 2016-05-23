'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestsController
 * @description # RequestsController
 */
angular.module('modesti').controller('RequestsController', RequestsController);

function RequestsController($http, $location, $scope, RequestService, AuthService, SchemaService) {
  var self = this;

  self.statuses = [];
  self.schemas = [];
  self.users = [AuthService.getCurrentUser()];
  self.types = ['CREATE', 'UPDATE', 'DELETE'];

  self.filter = {};

  self.sort = 'createdAt,desc';

  self.loading = undefined;

  self.queryUsers = queryUsers;
  self.isUserAuthenticated = isUserAuthenticated;
  self.getCurrentUsername = getCurrentUsername;
  self.deleteRequest = deleteRequest;
  self.editRequest = editRequest;
  self.onPageChanged = onPageChanged;
  self.resetFilter = resetFilter;
  self.getRequestCount = getRequestCount;
  self.hasCustomProperties = hasCustomProperties;
  self.formatCustomProperty = formatCustomProperty;

  resetFilter();
  getRequests(1, 15, self.sort, self.filter);
  getRequestMetrics();
  getSchemas();
  //getSubsystems();

  /**
   *
   * @returns {boolean}
   */
  function isUserAuthenticated() {
    return AuthService.isCurrentUserAuthenticated();
  }

  /**
   *
   * @returns {string}
   */
  function getCurrentUsername() {
    return AuthService.getCurrentUser().username;
  }

  /**
   *
   */
  function resetFilter() {
    console.log('filter reset');
    self.filter = {
      description: '',
      status: '',
      domain: '',
      creator: '',
      assignee: '',
      type: ''
    };
  }

  /**
   *
   */
  function getRequests(page, size, sort, filter) {
    self.loading = 'started';

    RequestService.getRequests(page, size, sort, filter).then(function (response) {
      if (response.hasOwnProperty('_embedded')) {
        self.requests = response._embedded.requests;
      } else {
        self.requests = [];
      }

      self.page = response.page;
      // Backend pages 0-based, Bootstrap pagination 1-based
      self.page.number += 1;

      angular.forEach(response._links, function (item) {
        if (item.rel === 'next') {
          self.page.next = item.href;
        }

        if (item.rel === 'prev') {
          self.page.prev = item.href;
        }
      });

      self.loading = 'success';
    },

    function () {
      self.loading = 'error';
    });
  }

  /**
   *
   */
  function deleteRequest(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1);

    RequestService.deleteRequest(id).then(function () {
      console.log('deleted request ' + id);
      self.requests.splice(self.requests.indexOf(request), 1);
    },

    function () {
      // something went wrong deleting the request
    });
  }

  /**
   *
   */
  function editRequest(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1).replace('{?projection}', '');

    $location.path('/requests/' + id);
  }

  /**
   * Retrieve some metrics about requests. Currently contains only the number of requests of each status.
   */
  function getRequestMetrics() {
    RequestService.getRequestMetrics().then(function (statuses) {
      self.statuses = statuses;
    });
  }

  /**
   * Get the number of requests of a given status
   *
   * @param status
   */
  function getRequestCount(status) {
    for (var key in self.statuses) {
      if (self.statuses.hasOwnProperty(key)) {
        var s = self.statuses[key];

        if (s.hasOwnProperty('status') && s.status === status) {
          return s.count;
        }
      }
    }

    return 0;
  }

  /**
   *
   */
  function getSchemas() {
    SchemaService.getSchemas().then(function (schemas) {
      self.schemas = schemas;
    });
  }

  // TODO: consolidate this functionality with assignment-modal.js
  function queryUsers(query) {
    return $http.get(BACKEND_BASE_URL + '/users/search', {
      params : {
        query : parseQuery(query)
      }
    }).then(function(response) {
      if (!response.data.hasOwnProperty('_embedded')) {
        return [];
      }

      self.users = response.data._embedded.users;
    });
  }

  // TODO: consolidate this functionality with assignment-modal.js
  function parseQuery(query) {
    var q = '';

    if (query.length !== 0) {
      q += '(username == ' + query;
      q += ' or firstName == ' + query;
      q += ' or lastName == ' + query + ')';
    }

    console.log('parsed query: ' + query);
    return q;
  }

  function hasCustomProperties(request) {
    var has = false;
    self.schemas.forEach(function (schema) {
      if (schema.id === request.domain && schema.fields) {
        has = true;
      }
    });

    return has;
  }

  function formatCustomProperty(request, key) {
    var value = '';
    var field = {};

    self.schemas.forEach(function (schema) {
      if (schema.id === request.domain && schema.fields) {

        schema.fields.forEach(function (f) {
          if (request.properties.hasOwnProperty(f.id) && key === f.id) {
            field = f;

            if (field.type === 'autocomplete') {
              value = field.model ? request.properties[field.id][field.model] : request.properties[field.id].value;
            } else {
              value = request.properties[field.id]
            }
          }
        });
      }
    });

    return {value: value, field: field};
  }

  /**
   *
   */
  function onPageChanged() {
    getRequests(self.page.number, self.page.size, self.sort, self.filter);
  }

  /**
   *
   */
  function onCriteriaChanged() {
    if (!self.page) {
      return;
    }

    console.log('filter: ' + JSON.stringify(self.filter));
    getRequests(0, self.page.size, self.sort, self.filter);
  }

  /**
   *
   */
  $scope.$watch(function() { return self.filter; }, onCriteriaChanged, true);
  $scope.$watch(function() { return self.sort; }, onCriteriaChanged, true);
}
