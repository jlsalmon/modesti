'use strict';

/**
 * @ngdoc service
 * @name modesti.TaskService
 * @description # TaskService Service in the modesti.
 */
angular.module('modesti').service('TaskService', TaskService);

function TaskService($q, $http, $localStorage, Restangular) {
  var self = this;

  /**
   * Public API for the task service.
   */
  var service = {
    getTasksForRequest: getTasksForRequest,
    queryTasksForRequest: queryTasksForRequest,
    getSignalsForRequest: getSignalsForRequest,
    claimTask: claimTask,
    completeTask: completeTask
  };

  /**
   *
   * @param request
   * @returns {*}
   */
  function getTasksForRequest(request) {
    console.log('fetching tasks for request ' + request.requestId);

    var q = $q.defer();

    $http.get(BACKEND_BASE_URL + '/requests/' + request.requestId + '/tasks').then(function(response) {
      var tasks = {};

      angular.forEach(response.data._embedded.tasks, function (task) {
        tasks[task.name] = task;
      });

      console.log('fetched ' + tasks.length + ' task(s)');
      q.resolve(tasks);
    },

    function(error) {
      console.log('error fetching tasks: ' + error);
      q.reject(error);
    });

    //var promises = [];
    //
    //angular.forEach(request._links.tasks, function (link) {
    //  var href = link.href ? link.href : link;
    //  var promise = $http.get(href);
    //  promises.push(promise);
    //});
    //
    //$q.all(promises).then(function (responses) {
    //    console.log('fetched ' + responses.length + ' task(s)');
    //    var tasks = {};
    //
    //    angular.forEach(responses, function (response) {
    //      tasks[response.data.name] = response.data;
    //    });
    //
    //    q.resolve(tasks);
    //  },
    //
    //  function (error) {
    //    console.log('error fetching tasks: ' + error);
    //    q.reject(error);
    //  });

    return q.promise;
  }

  /**
   *
   * @param request
   * @returns {*}
   */
  function queryTasksForRequest(request) {
    console.log('querying tasks for request ' + request.requestId);
    var q = $q.defer();

    var query = {
      processInstanceVariables: [{
        name: "requestId",
        value: request.requestId,
        operation: "equals",
        type: "string"
      }]
    };

    Restangular.one('query/tasks').post('', query).then(function (result) {
        var taskList = result.data.data;
        console.log('found ' + taskList.length + ' task(s)');
        var tasks = {};

        angular.forEach(result.data.data, function (task) {
          tasks[task.name] = task;
        });

        q.resolve(tasks);
      },

      function (error) {
        console.log('error querying tasks: ' + error);
        q.reject(error);
      });

    return q.promise;
  }

  /**
   *
   * @param request
   * @returns {*}
   */
  function getSignalsForRequest(request) {
    console.log('fetching signals for request ' + request.requestId);

    var q = $q.defer();
    var promises = [];

    angular.forEach(request._links.signals, function (link) {
      var href = link.href ? link.href : link;
      var promise = $http.get(href);
      promises.push(promise);
    });

    $q.all(promises).then(function (responses) {
      console.log('fetched ' + responses.length + ' signal(s)');
      var signals = {};

      angular.forEach(responses, function (response) {
        signals[response.data.name] = response.data;
      });

      q.resolve(signals);
    },

    function (error) {
      console.log('error fetching signals: ' + error);
      q.reject(error);
    });

    return q.promise;
  }

  /**
   *
   * @param taskId
   * @returns {*}
   */
  function claimTask(taskId) {
    var q = $q.defer();

    var params = {
      action: 'claim',
      assignee: $localStorage.user.name
    };

    Restangular.one('runtime/tasks', taskId).post('', params).then(function (result) {
        console.log('claimed task ' + taskId + ' as user ' + params.assignee);
        q.resolve(result);
      },

      function (error) {
        console.log('error claiming task ' + taskId);
        q.reject(error);
      });

    return q.promise;
  }

  /**
   *
   * @param taskName
   * @param requestId
   * @returns {*}
   */
  function completeTask(taskName, requestId) {
    var q = $q.defer();
    var params = {action: 'COMPLETE'};

    $http.post(BACKEND_BASE_URL + '/requests/' + requestId + '/tasks/' + taskName, params).then(function () {
        console.log('completed task ' + taskName);
        q.resolve();
      },

      function (error) {
        console.log('error completing task ' + taskName);
        q.reject(error);
      });

    return q.promise;
  }


  return service;
}