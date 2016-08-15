import {TaskService} from '../../task/task.service';

export class ShowIfDirective implements ng.IDirective {

  public transclude:string = 'element';
  public priority:number = 600;

  public constructor(private taskService:TaskService, private ngIfDirective:any) {}

  static factory(): ng.IDirectiveFactory {
    const directive = (taskService:TaskService, private ngIfDirective:any) => new ShowIfDirective(taskService, ngIfDirective);
    directive.$inject = ['TaskService', 'ngIfDirective'];
    return directive;
  }

  public link:Function = (scope, element, attrs) => {
    attrs.ngIf = () => {
      var expression = attrs.showIf;
      var conditions = expression.split(' && ');
      var task = this.taskService.getCurrentTask();
      var results = [];

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

      return results.reduce((a, b) => {
          return (a === b) ? a : false;
        }) === true;
    };

    this.ngIfDirective[0].link.apply(this.ngIfDirective[0], arguments);
  }
}
