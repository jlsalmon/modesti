import {TaskService} from '../../task/task.service';
import {ValidationService} from '../validation/validation.service';
import {AlertService} from '../../alert/alert.service';

export class RequestFooterDirective implements ng.IDirective {

  public controller:Function = RequestFooterController;
  public controllerAs:string = '$ctrl';

  public scope:any = {};
  public bindToController:any = {
    request: '=',
    tasks: '=',
    schema: '=',
    table: '='
  };

  public constructor(private $compile:ng.ICompileService, private $http:ng.IHttpService, private $ocLazyLoad:any) {}

  static factory(): ng.IDirectiveFactory {
    const directive = ($compile:ng.ICompileService, $http:ng.IHttpService, $ocLazyLoad:any) => new RequestFooterDirective($compile, $http, $ocLazyLoad);
    directive.$inject = ['$compile', '$http', '$ocLazyLoad'];
    return directive;
  }

  public link:Function = (scope, element) => {
    var schemaId = scope.$ctrl.request.domain;
    var status = scope.$ctrl.request.status.split('_').join('-').toLowerCase();

    this.$http.get('/api/plugins/' + schemaId + '/assets').then((response) => {
      var assets = response.data;
      console.log(assets);

      this.$ocLazyLoad.load(assets, {serie: true}).then(() => {

        var template = '<div ' + status + '-controls request="$ctrl.request" tasks="$ctrl.tasks" schema="$ctrl.schema" table="$ctrl.table"></div>';
        element.append(this.$compile(template)(scope));
      });
    });
  }
}

class RequestFooterController {
  public static $inject:string[] = ['$scope', '$state', 'TaskService', 'ValidationService', 'AlertService'];

  public request:any;
  public table:any
  public validating:string;
  public submitting:string;
  
  public constructor(private $scope:any, private $state:any,
                     private taskService:TaskService, private validationService:ValidationService, private alertService:AlertService) {}

  public claim(event) {
    this.stopEvent(event);
    this.taskService.assignTaskToCurrentUser(this.request);
  }

  public validate(event) {
    this.stopEvent(event);

    this.alertService.clear();
    this.validating = 'started';

    this.validationService.validateRequest(this.request).then((request) => {
        // Save the reference to the validated request
        this.request = request;

        // Render the table to show the error highlights
        this.table.render();

        if (this.request.valid === false) {
          this.validating = 'error';
          this.alertService.add('danger', 'Request failed validation with ' + this.getNumValidationErrors() + ' errors');
        } else {
          this.validating = 'success';
          this.alertService.add('success', 'Request has been validated successfully');
        }
      },

      (error) => {
        console.log('error validating request: ' + error.statusText);
        this.validating = 'error';
      });
  }

  public submit(event) {
    this.stopEvent(event);

    var task = this.taskService.getCurrentTask();

    this.alertService.clear();
    this.submitting = 'started';
    var previousStatus = this.request.status;

    // Complete the task associated with the request
    this.taskService.completeTask(task.name, this.request).then((request) => {
      console.log('completed task ' + task.name);

      this.request = request;
      this.submitting = 'success';

      // If the request is now FOR_CONFIGURATION, no need to go away from the request page
      if (this.request.status === 'FOR_CONFIGURATION') {
        this.alertService.add('info', 'Request has been submitted successfully and is ready to be configured.');
      }

      if (this.request.status === 'CLOSED') {
        this.alertService.add('info', 'Request has been submitted successfully and is now closed.');
      }

      // If the request is in any other state, show a page with information about what happens next
      else {
        this.$state.reload().then(() => {
          this.alertService.add('info', 'Request has been submitted successfully.');
        });
      }
    });
  }

  public getNumValidationErrors() {
    var n = 0;

    if (this.request.hasOwnProperty('points')) {
      this.request.points.forEach((point) => {
        if (point.hasOwnProperty('errors')) {
          point.errors.forEach((error) => {
            n += error.errors.length;
          });
        }
      });
    }

    return n;
  }

  public stopEvent(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
  }
}
