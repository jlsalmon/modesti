import {TaskService} from '../../task/task.service';

export class EnableIfDirective implements ng.IDirective {

  public constructor(private taskService:TaskService, private ngDisabledDirective:any) {}

  static factory(): ng.IDirectiveFactory {
    const directive = (taskService:TaskService, ngDisabledDirective:any) => new EnableIfDirective(taskService, ngDisabledDirective);
    directive.$inject = ['TaskService', 'ngDisabledDirective'];
    return directive;
  }

  public link:Function = (scope:any, element:any, attrs:any) => {
    attrs.ngDisabled = () => {
      var expression:any = attrs.enableIf;
      var conditions:any = expression.split(' && ');
      var task:any = this.taskService.getCurrentTask();
      var results:any[] = [];

      conditions.forEach((condition) => {
        var result = false;

        if (condition.indexOf('user-authorised-for-task') !== -1) {
          result = this.taskService.isCurrentUserAuthorised(task);
          result = condition.indexOf('!') !== -1 ? !result : result;
        }
        else if (condition.indexOf('task-assigned-to-current-user') !== -1) {
          result = this.taskService.isCurrentUserAssigned(task);
          result = condition.indexOf('!') !== -1 ? !result : result;
        }
        else if (condition.indexOf('task-assigned') !== -1) {
          result = this.taskService.isTaskClaimed(task);
          result = condition.indexOf('!') !== -1 ? !result : result;
        }

        results.push(result);
      });

      return results.reduce(function(a, b){ return (a === b) ? a : false; }) !== true;
    };

    this.ngDisabledDirective[0].link.apply(this.ngDisabledDirective[0], arguments);
  }
}
