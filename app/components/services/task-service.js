'use strict';

/**
 * @ngdoc service
 * @name modesti.TaskService
 * @description # TaskService
 */
angular.module('modesti').service('TaskService', TaskService);

function TaskService($q, $http, $state, $modal, AuthService, RequestService) {
  var self = this;

  self.tasks = {};

  /**
   * Public API for the task service.
   */
  return {
    getTasksForRequest: getTasksForRequest,
    getSignalsForRequest: getSignalsForRequest,
    getCurrentTask: getCurrentTask,
    assignTask: assignTask,
    assignTaskToCurrentUser: assignTaskToCurrentUser,
    completeTask: completeTask,
    isTaskClaimed: isTaskClaimed,
    isAnyTaskClaimed: isAnyTaskClaimed,
    isCurrentUserAssigned: isCurrentUserAssigned,
    isCurrentUserAuthorised: isCurrentUserAuthorised,
    sendSignal: sendSignal
  };

  function getCurrentTask() {
    return self.tasks[Object.keys(self.tasks)[0]];
  }

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

      console.log('fetched ' + Object.keys(tasks).length + ' task(s)');
      self.tasks = tasks;
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

      console.log('fetched ' + Object.keys(signals).length + ' signal(s)');
      q.resolve(signals);
    },

    function (error) {
      console.log('error fetching signals: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  /**
   *
   */
  function assignTask(request) {
    var q = $q.defer();
    var task = getCurrentTask();

    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/assignment-modal.html',
      controller: 'AssignmentModalController as ctrl',
      resolve: {
        task: function () {
          return task;
        }
      }
    });

    modalInstance.result.then(function (assignee) {
      console.log('assigning request to user ' + assignee.username);

      doAssignTask(task.name, request.requestId, assignee.username).then(function (newTask) {
        console.log('assigned request');
        task = newTask;
        request.assignee = assignee.username;
        q.resolve(newTask);
      });
    });

    return q.promise;
  }

  /**
   *
   */
  function assignTaskToCurrentUser(request) {
    var q = $q.defer();
    var task = getCurrentTask();
    var username = AuthService.getCurrentUser().username;

    doAssignTask(task.name, request.requestId, username).then(function (newTask) {
      console.log('assigned request');
      task = newTask;
      request.assignee = username;
      q.resolve(newTask);
    });

    return q.promise;
  }

  /**
   *
   * @param taskName
   * @param requestId
   * @param assignee
   * @returns {*}
   */
  function doAssignTask(taskName, requestId, assignee) {
    var q = $q.defer();

    var params = {
      action: 'ASSIGN',
      assignee: assignee
    };

    $http.post(BACKEND_BASE_URL + '/requests/' + requestId + '/tasks/' + taskName, params).then(function (response) {
      console.log('assigned task ' + taskName + ' to user ' + params.assignee);
      q.resolve(response.data);
    },

    function (error) {
      console.log('error assigning task ' + taskName + ': ' + error.data.message);
      q.reject(error);
    });

    return q.promise;
  }

  /**
   *
   * @param taskName
   * @param request
   * @returns {*}
   */
  function completeTask(taskName, request) {
    var q = $q.defer();
    var params = {action: 'COMPLETE'};

    RequestService.saveRequest(request).then(function () {
      console.log('saved request before completing task');

      $http.post(BACKEND_BASE_URL + '/requests/' + request.requestId + '/tasks/' + taskName, params).then(function () {
        console.log('completed task ' + taskName);

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          // Get the request once again from the cache
          RequestService.getRequest(request.requestId).then(function (request) {
            q.resolve(request);
          });
        });
      },

      function (error) {
        console.log('error completing task ' + taskName);
        q.reject(error);
      });
    },

    function (error) {
      console.log('error saving before completing task: ' + error.statusText);
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
   * @param tasks
   * @returns {boolean}
   */
  function isAnyTaskClaimed(tasks) {
    for (var key in tasks) {
      if (isTaskClaimed(tasks[key])) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param task
   * @returns {boolean}
   */
  function isCurrentUserAssigned(task) {
    var user = AuthService.getCurrentUser();
    if (!user || !task) {
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
    if (!task) {
      return false;
    }

    var user = AuthService.getCurrentUser();
    if (!user) {
      return false;
    }

    if (!task.candidateGroups || task.candidateGroups.length === 0) {
      return true;
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

  /**
   *
   */
  function sendSignal(signal) {
    var q = $q.defer();

    $http.post(signal._links.self.href, {}).then(function () {
      console.log('sent signal ' + signal.name);
      q.resolve();
    },

    function (error) {
      console.log('error sending signal ' + signal.name + ': ' + error);
      q.reject(error);
    });

    return q.promise;
  }
}
