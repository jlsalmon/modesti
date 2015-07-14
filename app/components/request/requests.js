'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:UserRequestsController
 * @description # UserRequestsController Controller of the modesti
 */
angular.module('modesti').controller('UserRequestsController', UserRequestsController);

function UserRequestsController($http, $location, $scope, RequestService, TaskService) {
  var self = this;

  self.statuses = [];
  self.domains = ['TIM', 'CSAM', 'PVSS']; // TODO retrieve this dynamically
  self.subsystems = [];
  self.creators = [];
  self.types = ['CREATE', 'UPDATE', 'DELETE'];

  self.filter = {
    status: '',
    domain: '',
    subsystem: {
      system: '',
      subsystem: ''
    },
    //categories: '',
    creator: {
      username: ''
    },
    type: ''
  };

  self.loading = undefined;

  self.deleteRequest = deleteRequest;
  self.editRequest = editRequest;
  self.onPageChanged = onPageChanged;
  self.getRequestCount = getRequestCount;
  self.claimTask = claimTask;

  getRequests(1, 10, "requestId,desc", self.filter);
  getRequestMetrics();
  getSubsystems();

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

    function (error) {
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

    function (error) {
      // something went wrong deleting the request
    });
  }

  /**
   *
   */
  function editRequest(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1);

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

        if (s.hasOwnProperty('status') && s.status == status) {
          return s.count;
        }
      }
    }

    return 0;
  }

  function getSubsystems() {
    // TODO refactor this into a service
    $http.get(BACKEND_BASE_URL + '/subsystems/search/find').then(function(response) {
      self.subsystems = response.data._embedded.subsystems;
    });
  }

  /**
   *
   */
  function onPageChanged() {
    getRequests(self.page.number, self.page.size, "requestId,desc", self.filter);
  }

  /**
   *
   */
  $scope.$watch(function () {
    return self.filter;
  }, function (value) {

    if (!self.page) {
      return;
    }

    getRequests(0, self.page.size, "requestId,desc", self.filter);
  }, true);

  /**
   *
   */
  function claimTask(request) {

    TaskService.getTaskForRequest(request.requestId).then(function (task) {
      TaskService.claimTask(task.id).then(function (task) {
        $location.path('/requests/' + request.requestId);
      },

      function (error) {
        console.log('error claiming task ' + id);
      });
    },

    function (error) {
      console.log('error querying tasks');
    });
  }
};
