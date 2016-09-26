import {RequestService} from '../request.service';
import {TaskService} from '../../task/task.service';
import {AlertService} from '../../alert/alert.service';
import {HistoryService} from '../history/history.service';
import {Request} from '../request';
import {Task} from '../../task/task';
import {Schema} from '../../schema/schema';
import {Table} from '../table/table';
import {Category} from '../../schema/category/category';
import {Point} from '../point/point';
import {Field} from '../../schema/field/field';
import IComponentOptions = angular.IComponentOptions;
import IStateService = angular.ui.IStateService;

export class RequestToolbarComponent implements IComponentOptions {
  public templateUrl: string = '/request/toolbar/toolbar.component.html';
  public controller: Function = RequestToolbarController;
  public bindings: any = {
    request: '=',
    tasks: '=',
    schema: '=',
    table: '=',
    activeCategory: '='
  };
}

class RequestToolbarController {
  public static $inject: string[] = ['$uibModal', '$state', 'RequestService', 'TaskService',
                                     'AlertService', 'HistoryService'];

  public request: Request;
  public tasks: Task[];
  public schema: Schema;
  public table: Table;
  public activeCategory: Category;

  public constructor(private $modal: any, private $state: IStateService, private requestService: RequestService,
                     private taskService: TaskService, private alertService: AlertService,
                     private historyService: HistoryService) {}

  public save(): void {
    this.requestService.saveRequest(this.request).then((request: Request) => {
      this.request = request;
    });
  }

  public undo(): void {
    this.table.hot.undo();
  }

  public redo(): void {
    this.table.hot.redo();
  }

  public cut(): void {
    this.table.hot.copyPaste.triggerCut();
  }

  public copy(): void {
    this.table.hot.copyPaste.setCopyableText();
  }

  public paste(): void {
    this.table.hot.copyPaste.triggerPaste();
    this.table.hot.copyPaste.copyPasteInstance.onPaste((value: any) => {
      console.log('onPaste(): ' + value);
    });
  }

  public assignTask(): void {
    this.taskService.assignTask(this.request).then((newTask: Task) => {
      this.tasks[newTask.name] = newTask;
      this.table.reload();
    });
  }

  public assignTaskToCurrentUser(): void {
    this.taskService.assignTaskToCurrentUser(this.request).then((newTask: Task) => {
      this.tasks[newTask.name] = newTask;
      this.table.reload();
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
        this.$state.go('requests');
      }, (error: any) => {
        console.log('delete failed: ' + error.statusText);
      });
    }, () => {
      console.log('delete aborted');
    });
  }

  public cloneRequest(): void {
    let modalInstance: any = this.$modal.open({
      animation: false,
      templateUrl: '/request/clone/clone-request.modal.html',
      controller: 'CloneRequestModalController as ctrl',
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
