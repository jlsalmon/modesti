import {TaskService} from '../../task/task.service';
import {ValidationService} from '../validation/validation.service';
import {AlertService} from '../../alert/alert.service';
import {Point} from '../point/point';
import {Task} from '../../task/task';
import {Request} from '../request';
import IPromise = angular.IPromise;
import IDeferred = angular.IDeferred;
import IQService = angular.IQService;
import ICompileService = angular.ICompileService;
import IHttpService = angular.IHttpService;
import IDirective = angular.IDirective;
import IDirectiveFactory = angular.IDirectiveFactory;
import IScope = angular.IScope;
import IStateService = angular.ui.IStateService;

export class RequestFooterDirective implements IDirective {

  public controller: Function = RequestFooterController;
  public controllerAs: string = '$ctrl';

  public scope: any = {};
  public bindToController: any = {
    request: '=',
    tasks: '=',
    schema: '=',
    table: '='
  };

  public constructor(private $compile: ICompileService, private $http: IHttpService, private $ocLazyLoad: any) {}

  public static factory(): IDirectiveFactory {
    const directive: IDirectiveFactory = ($compile: ICompileService, $http:  IHttpService, $ocLazyLoad: any) =>
      new RequestFooterDirective($compile, $http, $ocLazyLoad);
    directive.$inject = ['$compile', '$http', '$ocLazyLoad'];
    return directive;
  }

  public link: Function = (scope, element) => {
    let schemaId: string = scope.$ctrl.request.domain;
    let status: string = scope.$ctrl.request.status.split('_').join('-').toLowerCase();

    this.$http.get('/api/plugins/' + schemaId + '/assets').then((response: any) => {
      let assets: string[] = response.data;
      console.log(assets);

      this.$ocLazyLoad.load(assets, {serie: true}).then(() => {

        let template: string = '<div ' + status + '-controls request="$ctrl.request" ' +
          'tasks="$ctrl.tasks" schema="$ctrl.schema" table="$ctrl.table"></div>';
        element.append(this.$compile(template)(scope));
      });
    });
  }
}

class RequestFooterController {
  public static $inject: string[] = ['$scope', '$state', '$q', 'TaskService', 'ValidationService', 'AlertService'];

  public request: Request;
  public table: any;
  public validating: string;
  public submitting: string;

  public constructor(private $scope: IScope, private $state: IStateService, private $q: IQService,
                     private taskService: TaskService, private validationService: ValidationService,
                     private alertService: AlertService) {}

  public claim(event: JQueryEventObject): void {
    this.stopEvent(event);
    this.taskService.assignTaskToCurrentUser(this.request).then(() => {
      this.table.activateDefaultCategory();
    });
  }

  public validate(event: JQueryEventObject): void {
    this.stopEvent(event);

    this.alertService.clear();
    this.validating = 'started';

    this.validationService.validateRequest(this.request).then((request: Request) => {
      // Save the reference to the validated request
      this.request = request;

      if (this.request.valid === false) {
        this.validating = 'error';
        this.alertService.add('danger', 'Request failed validation with ' + this.getNumValidationErrors() + ' errors');
      } else {
        this.validating = 'success';
        this.alertService.add('success', 'Request has been validated successfully');
      }

      // Render the table to show the error highlights
      this.table.render();
    },

    (error: any) => {
      console.log('error validating request: ' + error.statusText);
      this.validating = 'error';
    });
  }

  public submit(event: JQueryEventObject): IPromise<Request> {
    this.stopEvent(event);
    let q: IDeferred<Request> = this.$q.defer();

    let task: Task = this.taskService.getCurrentTask();
    let previousStatus: string = this.request.status;

    this.alertService.clear();
    this.submitting = 'started';

    // Complete the task associated with the request
    this.taskService.completeTask(task.name, this.request).then((request: Request) => {
      console.log('completed task ' + task.name);

      this.request = request;
      this.submitting = 'success';

      q.resolve(request);
    },
    (error: any) => {
      q.reject(error);
    });

    return q.promise;
  }

  public getNumValidationErrors(): number {
    let n: number = 0;

    if (this.request.hasOwnProperty('points')) {
      this.request.points.forEach((point: Point) => {
        if (point.hasOwnProperty('errors')) {
          point.errors.forEach((error: any) => {
            n += error.errors.length;
          });
        }
      });
    }

    return n;
  }

  public stopEvent(event: JQueryEventObject): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
  }
}
