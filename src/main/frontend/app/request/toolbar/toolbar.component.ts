import {RequestService} from '../request.service';
import {TaskService} from '../../task/task.service';
import {AlertService} from '../../alert/alert.service';
import {HistoryService} from '../history/history.service';

export class RequestToolbarComponent implements ng.IComponentOptions {
  public templateUrl:string = '/request/toolbar/toolbar.component.html';
  public controller:Function = RequestToolbarController;
  public bindings:any = {
    request: '=',
    tasks: '=',
    schema: '=',
    table: '=',
    activeCategory: '='
  };
}

class RequestToolbarController {
  public static $inject:string[] = ['$uibModal', '$state', 'RequestService', 'TaskService', 'AlertService', 'HistoryService'];

  public request:any;
  public tasks:any;
  public schema:any;
  public table:any;
  public activeCategory:any;

  public constructor(private $modal:any, private $state:any, private requestService:RequestService, 
                     private taskService:TaskService, private alertService:AlertService, private historyService:HistoryService) {}

  public save() {
    var request = this.request;

    this.requestService.saveRequest(request).then(() => {
      console.log('saved request');
    }, () => {
      console.log('error saving request');
    });
  }

  public undo() {
    this.table.undo();
  }

  public redo() {
    this.table.redo();
  }

  public cut() {
    this.table.copyPaste.triggerCut();
  }

  public copy() {
    this.table.copyPaste.setCopyableText();
  }

  public paste() {
    this.table.copyPaste.triggerPaste();
    this.table.copyPaste.copyPasteInstance.onPaste((value) => {
      console.log('onPaste(): ' + value);
    });
  }

  public assignTask() {
    this.taskService.assignTask(this.request).then((newTask) => {
      this.tasks[newTask.name] = newTask;
    });
  }

  public assignTaskToCurrentUser() {
    this.taskService.assignTaskToCurrentUser(this.request).then((newTask) => {
      this.tasks[newTask.name] = newTask;
    });
  }

  public showHelp() {
    this.$modal.open({
      animation: false,
      templateUrl: '/request/help/request-help.modal.html',
      controller: 'RequestHelpModalController as ctrl'
    });
  }

  public showComments() {
    this.$modal.open({
      animation: false,
      templateUrl: '/request/comments/request-comments.modal.html',
      controller: 'RequestCommentsModalController as ctrl',
      resolve: {
        request: () => this.request
      }
    });
  }

  public showHistory() {
    this.$modal.open({
      animation: false,
      size: 'lg',
      templateUrl: '/request/history/request-history.modal.html',
      controller: 'RequestHistoryModalController as ctrl',
      resolve: {
        request: () => this.request,
        history: () => this.historyService.getHistory(this.request.requestId)
      }
    });
  }

  public deleteRequest() {
    var modalInstance = this.$modal.open({
      animation: false,
      templateUrl: '/request/delete/delete-request.modal.html',
      controller: 'DeleteRequestModalController as ctrl',
      resolve: {
        request: () => this.request
      }
    });

    modalInstance.result.then(() => {
      this.requestService.deleteRequest(this.request.requestId).then(() => {
        console.log('deleted request');
        this.alertService.add('success', 'Request was deleted successfully.');
        this.$state.go('requests');
      },

      (error) => {
        console.log('delete failed: ' + error.statusText);
      });
    },

    () => {
      console.log('delete aborted');
    });
  }

  public cloneRequest() {
    var modalInstance = this.$modal.open({
      animation: false,
      templateUrl: '/request/clone/clone-request.modal.html',
      controller: 'CloneRequestModalController as ctrl',
      resolve: {
        request: () => this.request,
        schema: () => this.schema
      }
    });

    modalInstance.result.then(() => {},
    () => {
      console.log('clone aborted');
    });
  }

  public getAssignee() {
    var task = this.tasks[Object.keys(this.tasks)[0]];

    if (!task) {
      return null;
    }

    return task.assignee;
  }

  public isCurrentTaskRestricted() {
    var task = this.tasks[Object.keys(this.tasks)[0]];
    return task && task.candidateGroups.length === 1 && task.candidateGroups[0] === 'modesti-administrators';
  }

  public getActiveDatasources() {
    var result = [];

    this.request.points.forEach((point) => {
      this.schema.datasources.forEach((datasource) => {

        if (point.properties.pointType &&
          (point.properties.pointType === angular.uppercase(datasource.id) || point.properties.pointType === angular.uppercase(datasource.name))) {
          if (result.indexOf(datasource) === -1) {
            result.push(datasource);
          }
        }
      });
    });

    return result;
  }

  /**
   * Return true if the given category is "invalid", i.e. there are points in
   * the current request that have errors that relate to the category.
   *
   * @param category
   */
  public isInvalidCategory(category) {
    var fieldIds = category.fields.map((field) => field.id);
    var invalid = false;

    this.request.points.forEach((point) => {
      if (point.errors && point.errors.length > 0) {
        point.errors.forEach((error) => {
          if (!error.category) {
            var property = error.property.split('.')[0];

            if (fieldIds.indexOf(property) !== -1) {
              invalid = true;
            }
          }

          else if (error.category === category.name || error.category === category.id) {
            invalid = true;
          }
        });
      }
    });

    return invalid;
  }
}
