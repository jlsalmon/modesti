import {AuthService} from '../auth/auth.service';
import {RequestService} from '../request/request.service';

export class TaskService {
  public static $inject:string[] = ['$q', '$http', '$state', '$uibModal', 'AuthService', 'RequestService'];

  public tasks:any = {};
  
  public constructor(private $q:any, private $http:any, private $state:any, private $modal:any, 
                     private authService:AuthService, private requestService:RequestService) {}

  public getCurrentTask() {
    return this.tasks[Object.keys(this.tasks)[0]];
  }

  public getTasksForRequest(request) {
    console.log('fetching tasks for request ' + request.requestId);

    var q = this.$q.defer();

    this.$http.get('/api/requests/' + request.requestId + '/tasks').then((response) => {
      var tasks = {};

      if (response.data.hasOwnProperty('_embedded')) {
        angular.forEach(response.data._embedded.tasks, function (task) {
          tasks[task.name] = task;
        });
      }

      console.log('fetched ' + Object.keys(tasks).length + ' task(s)');
      this.tasks = tasks;
      q.resolve(tasks);
    },

    (error) => {
      console.log('error fetching tasks: ' + error);
      q.reject(error);
    });

    return q.promise;
  }

  public getSignalsForRequest(request) {
    console.log('fetching signals for request ' + request.requestId);

    var q = this.$q.defer();

    this.$http.get('/api/requests/' + request.requestId + '/signals').then((response) => {
      var signals = {};

      if (response.data.hasOwnProperty('_embedded')) {
        angular.forEach(response.data._embedded.signals, function (signal) {
          signals[signal.name] = signal;
        });
      }

      console.log('fetched ' + Object.keys(signals).length + ' signal(s)');
      q.resolve(signals);
    },

    (error) => {
      console.log('error fetching signals: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public assignTask(request) {
    var q = this.$q.defer();
    var task = this.getCurrentTask();

    var modalInstance = this.$modal.open({
      animation: false,
      templateUrl: '/request/assign/assign-request.modal.html',
      controller: 'AssignRequestModalController as ctrl',
      resolve: {
        task: function () {
          return task;
        }
      }
    });

    modalInstance.result.then((assignee) => {
      console.log('assigning request to user ' + assignee.username);

      this.doAssignTask(task.name, request.requestId, assignee.username).then((newTask) => {
        console.log('assigned request');
        task = newTask;
        request.assignee = assignee.username;
        q.resolve(newTask);
      });
    });

    return q.promise;
  }

  public assignTaskToCurrentUser(request) {
    var q = this.$q.defer();
    var task = this.getCurrentTask();
    var username = this.authService.getCurrentUser().username;

    this.doAssignTask(task.name, request.requestId, username).then((newTask) => {
      console.log('assigned request');
      this.tasks[task.name] = newTask;
      request.assignee = username;
      q.resolve(newTask);
    });

    return q.promise;
  }

  public doAssignTask(taskName, requestId, assignee) {
    var q = this.$q.defer();

    var params = {
      action: 'ASSIGN',
      assignee: assignee
    };

    this.$http.post('/api/requests/' + requestId + '/tasks/' + taskName, params).then((response) => {
      console.log('assigned task ' + taskName + ' to user ' + params.assignee);
      q.resolve(response.data);
    },

    (error) => {
      console.log('error assigning task ' + taskName + ': ' + error.data.message);
      q.reject(error);
    });

    return q.promise;
  }

  public completeTask(taskName, request) {
    var q = this.$q.defer();
    var params = {action: 'COMPLETE'};

    this.requestService.saveRequest(request).then(() => {
      console.log('saved request before completing task');

      this.$http.post('/api/requests/' + request.requestId + '/tasks/' + taskName, params).then(() => {
        console.log('completed task ' + taskName);

        // Clear the cache so that the state reload also pulls a fresh request
        this.requestService.clearCache();

        this.$state.reload().then(() => {
          // Get the request once again from the cache
          this.requestService.getRequest(request.requestId).then((request) => {
            q.resolve(request);
          });
        });
      },

      (error) => {
        console.log('error completing task ' + taskName);
        q.reject(error);
      });
    },

    (error) => {
      console.log('error saving before completing task: ' + error.statusText);
    });

    return q.promise;
  }

  public isTaskClaimed(task) {
    return task.assignee !== undefined && task.assignee !== null;
  }

  public isAnyTaskClaimed(tasks) {
    for (var key in tasks) {
      if (this.isTaskClaimed(tasks[key])) {
        return true;
      }
    }
    return false;
  }

  public isCurrentUserAssigned(task) {
    var user = this.authService.getCurrentUser();
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
  public isCurrentUserAuthorised(task) {
    if (!task) {
      return false;
    }

    var user = this.authService.getCurrentUser();
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

  public sendSignal(signal) {
    var q = this.$q.defer();

    this.$http.post(signal._links.this.href, {}).then(function () {
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
