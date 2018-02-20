import {AuthService} from '../auth/auth.service';
import {RequestService} from '../request/request.service';
import {Task} from './task';
import {Request} from '../request/request';
import {Signal} from './signal';
import {User} from '../user/user';
import {Authority} from '../user/authority';
import IPromise = angular.IPromise;
import IDeferred = angular.IDeferred;
import IQService = angular.IQService;
import IHttpService = angular.IHttpService;
import IStateService = angular.ui.IStateService;

export class TaskService {
  public static $inject: string[] = ['$q', '$http', '$state', '$uibModal', 'AuthService', 'RequestService'];

  public tasks: any = {};

  public constructor(private $q: IQService, private $http: IHttpService, private $state: IStateService,
                     private $modal: any, private authService: AuthService, private requestService: RequestService) {}

  public getCurrentTask(): Task {
    return this.tasks[Object.keys(this.tasks)[0]];
  }

  public getTasksForRequest(request: Request): IPromise<Task[]> {
    console.log('fetching tasks for request ' + request.requestId);

    let q: IDeferred<Task[]> = this.$q.defer();

    this.$http.get('/api/requests/' + request.requestId + '/tasks').then((response: any) => {
      let tasks: any = {};

      if (response.data.hasOwnProperty('_embedded')) {
        angular.forEach(response.data._embedded.tasks, (task: Task) => {
          tasks[task.name] = task;
        });
      }

      console.log('fetched ' + Object.keys(tasks).length + ' task(s)');
      this.tasks = tasks;
      q.resolve(tasks);
    },

    (error: any) => {
      console.log('error fetching tasks: ' + error);
      q.reject(error);
    });

    return q.promise;
  }

  public getSignalsForRequest(request: Request): IPromise<Signal[]> {
    console.log('fetching signals for request ' + request.requestId);

    let q: IDeferred<Signal[]> = this.$q.defer();

    this.$http.get('/api/requests/' + request.requestId + '/signals').then((response: any) => {
      let signals: any = {};

      if (response.data.hasOwnProperty('_embedded')) {
        angular.forEach(response.data._embedded.signals, (signal: Signal) => {
          signals[signal.name] = signal;
        });
      }

      console.log('fetched ' + Object.keys(signals).length + ' signal(s)');
      q.resolve(signals);
    },

    (error: any) => {
      console.log('error fetching signals: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public assignTask(request: Request): IPromise<Task> {
    let q: IDeferred<Task> = this.$q.defer();
    let task: Task = this.getCurrentTask();

    let modalInstance: any = this.$modal.open({
      animation: false,
      templateUrl: '/request/assign/assign-request.modal.html',
      controller: 'AssignRequestModalController as ctrl',
      resolve: {
        task: () => task
      }
    });

    modalInstance.result.then((assignee: User) => {
      console.log('assigning request to user ' + assignee.username);

      this.doAssignTask(task.name, request.requestId, assignee.username).then((newTask: Task) => {
        console.log('assigned request');
        task = newTask;
        request.assignee = assignee.username;
        q.resolve(newTask);
      });
    });

    return q.promise;
  }

  public unassignTask(request: Request): IPromise<Task> {
    let q: IDeferred<Task> = this.$q.defer();
    let task: Task = this.getCurrentTask();

    let params: any = {
      action: 'UNASSIGN'
    };

    this.$http.post('/api/requests/' + request.requestId + '/tasks/' + task.name, params).then((response: any) => {
      console.log('unassigned task ' + task.name);
      q.resolve(response.data);
    },

    (error: any) => {
      console.log('error unassigning task ' + task.name + ': ' + error.data.message);
      q.reject(error);
    });

    return q.promise;
  }

  public assignTaskToCurrentUser(request: Request): IPromise<Task> {
    let q: IDeferred<Task> = this.$q.defer();
    let task: Task = this.getCurrentTask();
    let username: string = this.authService.getCurrentUser().username;

    this.doAssignTask(task.name, request.requestId, username).then((newTask: Task) => {
      console.log('assigned request');
      this.tasks[task.name] = newTask;
      request.assignee = username;
      q.resolve(newTask);
    });

    return q.promise;
  }

  public doAssignTask(taskName: string, requestId: string, assignee: string): IPromise<Task> {
    let q: IDeferred<Task> = this.$q.defer();

    let params: any = {
      action: 'ASSIGN',
      assignee: assignee
    };

    this.$http.post('/api/requests/' + requestId + '/tasks/' + taskName, params).then((response: any) => {
      console.log('assigned task ' + taskName + ' to user ' + params.assignee);
      q.resolve(response.data);
    },

    (error: any) => {
      console.log('error assigning task ' + taskName + ': ' + error.data.message);
      q.reject(error);
    });

    return q.promise;
  }

  public completeTask(taskName: string, request: Request): IPromise<Request> {
    let q: IDeferred<Request> = this.$q.defer();
    let params: any = {action: 'COMPLETE'};

    this.requestService.saveRequest(request).then(() => {
      console.log('saved request before completing task');

      this.$http.post('/api/requests/' + request.requestId + '/tasks/' + taskName, params).then(() => {
        console.log('completed task ' + taskName);

        // Clear the cache so that the state reload also pulls a fresh request
        this.requestService.clearCache();

        this.$state.reload().then(() => {
          // Get the request once again from the cache
          this.requestService.getRequest(request.requestId).then((r: Request) => {
            q.resolve(r);
          });
        });
      },

      (error: any) => {
        console.log('error completing task ' + taskName);
        q.reject(error);
      });
    },

    (error: any) => {
      console.log('error saving before completing task: ' + error.statusText);
    });

    return q.promise;
  }

  public isTaskClaimed(task: Task): boolean {
    return task.assignee != null;
  }

  public isAnyTaskClaimed(tasks: Task[]): boolean {
    for (let key in tasks) {
      if (this.isTaskClaimed(tasks[key])) {
        return true;
      }
    }
    return false;
  }

  public isCurrentUserAssigned(task?: Task): boolean {
    if (!task) {
      task = this.getCurrentTask();
    }

    let user: User = this.authService.getCurrentUser();
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
  public isCurrentUserAuthorised(task?: Task): boolean {
    if (!task) {
      task = this.getCurrentTask();
    }

    let user: User = this.authService.getCurrentUser();
    if (!user || !task) {
      return false;
    }

    if (!task.candidateGroups || task.candidateGroups.length === 0) {
      return true;
    }

    let authorised: boolean = false;
    user.authorities.forEach((authority: Authority) => {
      let role: string = authority.authority;

      if (task.candidateGroups.indexOf(role) > -1) {
        authorised = true;
      }
    });

    return authorised;
  }

  public sendSignal(signal: Signal): IPromise<Signal> {
    let q: IDeferred<Signal> = this.$q.defer();

    this.$http.post(signal._links.this.href, {}).then(() => {
      console.log('sent signal ' + signal.name);
      q.resolve();
    },

    (error: any) => {
      console.log('error sending signal ' + signal.name + ': ' + error);
      q.reject(error);
    });

    return q.promise;
  }
}
