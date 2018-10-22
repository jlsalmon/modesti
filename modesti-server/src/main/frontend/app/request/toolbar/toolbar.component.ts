import {RequestService} from "../request.service";
import {TaskService} from "../../task/task.service";
import {AlertService} from "../../alert/alert.service";
import {HistoryService} from "../history/history.service";
import {Request} from "../request";
import {Task} from "../../task/task";
import {Schema} from "../../schema/schema";
import {HandsonTable} from "../../table/handsontable/handsontable";
import {IComponentOptions} from 'angular';
import {IStateService} from 'angular-ui-router';

export class RequestToolbarComponent implements IComponentOptions {
  public templateUrl: string = '/request/toolbar/toolbar.component.html';
  public controller: Function = RequestToolbarController;
  public bindings: any = {
    request: '=',
    tasks: '=',
    schema: '=',
    table: '='
  };
}

class RequestToolbarController {
  public static $inject: string[] = ['$uibModal', '$state', 'RequestService', 'TaskService',
                                     'AlertService', 'HistoryService'];

  public request: Request;
  public tasks: Task[];
  public schema: Schema;
  public table: HandsonTable;

  public constructor(private $modal: any, private $state: IStateService, private requestService: RequestService,
                     private taskService: TaskService, private alertService: AlertService,
                     private historyService: HistoryService) {}

  public save(): void {
    this.requestService.saveRequest(this.request).then((request: Request) => {
      this.request = request;
    });
  }

  public undo(): void {
    this.table.undo();
  }

  public redo(): void {
    this.table.redo();
  }

  public cut(): void {
    this.table.cut();
  }

  public copy(): void {
    this.table.copy();
  }

  public paste(): void {
    this.table.paste();
  }

  public assignTask(): void {
    this.taskService.assignTask(this.request).then((newTask: Task) => {
      this.tasks[newTask.name] = newTask;
      this.table.refreshColumnDefs();
    });
  }

  public unassignTask(): void {
    this.taskService.unassignTask(this.request).then((newTask: Task) => {
      this.tasks[newTask.name] = newTask;
      this.table.refreshColumnDefs();
    });
  }

  public assignTaskToCurrentUser(): void {
    this.taskService.assignTaskToCurrentUser(this.request).then((newTask: Task) => {
      this.tasks[newTask.name] = newTask;
      this.table.refreshColumnDefs();
    });
  }

  public showHelp(): void {
    this.$modal.open({
      animation: false,
      templateUrl: '/request/help/request-help.modal.html',
      controller: 'RequestHelpModalController as ctrl'
    });
  }

  public showComments(): void {
    this.$modal.open({
      animation: false,
      templateUrl: '/request/comments/request-comments.modal.html',
      controller: 'RequestCommentsModalController as ctrl',
      resolve: {
        request: () => this.request
      }
    });
  }

  public showHistory(): void {
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

  public canDeleteRequest() : boolean {
    let task : Task = this.taskService.getCurrentTask();
    let authorized : boolean = this.taskService.isCurrentUserAuthorised(task);
    return authorized && this.request.status !== 'FOR_CONFIGURATION';
  }

  public deleteRequest(): void {
    let modalInstance: any = this.$modal.open({
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
        this.$state.go('requestList');
      }, (error: any) => {
        if (error.status == 403) {
          this.alertService.add('warning', 'Deleting not allowed in this stage.');
        }
        console.log('delete failed: ' + error.statusText);
      });
    }, () => {
      console.log('delete aborted');
    });
  }

  public canCloneRequest() : boolean {
    return this.request.type == 'CREATE' && (this.schema.configuration === null || this.schema.configuration.cloneFromUi);
  }

  public cloneRequest(): void {
    let modalInstance: any = this.$modal.open({
      animation: false,
      templateUrl: '/request/clone/clone-request.modal.html',
      controller: 'CloneRequestModalController as ctrl',
      size: 'lg',
      resolve: {
        request: () => this.request,
        schema: () => this.schema
      }
    });

    modalInstance.result.then(() => {
      console.log('request cloned');
    }, () => {
      console.log('clone aborted');
    });
  }

  public getAssignee(): string {
    let task: Task = this.taskService.getCurrentTask();
    return task ? task.assignee : undefined;
  }

  public isCurrentTaskRestricted(): boolean {
    let task: Task = this.taskService.getCurrentTask();
    return task && task.candidateGroups.length === 1 && task.candidateGroups[0] === 'modesti-administrators';
  }
}
