'use strict';

/**
 * @ngdoc service
 * @name modesti.TaskService
 * @description # TaskService Service in the modesti.
 */
angular.module('modesti').service('TaskService', TaskService);

function TaskService($q, $http, AuthService) {
  var self = this;

  /**
   * Public API for the task service.
   */
  var service = {
    getTasksForRequest: getTasksForRequest,
    getSignalsForRequest: getSignalsForRequest,
    claimTask: claimTask,
    completeTask: completeTask,
    isTaskClaimed: isTaskClaimed,
    isCurrentUserAssigned: isCurrentUserAssigned,
    isCurrentUserAuthorised: isCurrentUserAuthorised
  };

  /**
   *
   * @param request
   * @returns {*}
   */
  function getTasksForRequest(request) {
    console.log('fetching tasks for request ' + request.requestId);

    var q = $q.defer();

    $http.get(BACKEND_BASE_URL + '/requests/' + request.requestId + '/tasks').then(function (response) {
      var tasks = {};

      if (response.data.hasOwnProperty('_embedded')) {
        angular.forEach(response.data._embedded.tasks, function (task) {
          tasks[task.name] = task;
        });
      }

      console.log('fetched ' + tasks.length + ' task(s)');
      q.resolve(tasks);
    },

    function (error) {
      console.log('error fetching tasks: ' + error);
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

    $http.get(BACKEND_BASE_URL + '/requests/' + request.requestId + '/signals').then(function (response) {
      var signals = {};

      if (response.data.hasOwnProperty('_embedded')) {
        angular.forEach(response.data._embedded.signals, function (signal) {
          signals[signal.name] = signal;
        });
      }

      console.log('fetched ' + signals.length + ' signal(s)');
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
   * @param taskName
   * @param requestId
   * @returns {*}
   */
  function claimTask(taskName, requestId) {
    var q = $q.defer();

    var params = {
      action: 'CLAIM',
      assignee: AuthService.getCurrentUser().username
    };

    $http.post(BACKEND_BASE_URL + '/requests/' + requestId + '/tasks/' + taskName, params).then(function (response) {
      console.log('claimed task ' + taskName + ' as user ' + params.assignee);
      q.resolve(response.data);
    },

    function (error) {
      console.log('error claiming task ' + taskName + ': ' + error.data.message);
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

  /**
   *
   * @param task
   * @returns {boolean}
   */
  function isTaskClaimed(task) {
    return task.assignee !== undefined && task.assignee !== null;
  }

  /**
   *
   * @param task
   * @returns {boolean}
   */
  function isCurrentUserAssigned(task) {
    var user = AuthService.getCurrentUser();
    if (!user) {
      return false;
    }

    return task.assignee === user.username;
  }

  /**
   * Check if the current user is authorised to act upon the given task.
   *
   * @param task
   * @returns {boolean} true if the current user is authorised to act the given task
   */
  function isCurrentUserAuthorised(task) {
    var user = AuthService.getCurrentUser();
    if (!user) {
      return false;
    }

    var role;
    for (var i = 0, len = user.authorities.length; i < len; i++) {
      role = user.authorities[i].authority;

      if (task.candidateGroups.indexOf(role) > -1) {
        return true;
      }
    }


    return false;
  }

  return service;
}