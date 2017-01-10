import {TaskService} from '../../task/task.service';
import {Task} from '../../task/task';
import IDirectiveFactory = angular.IDirectiveFactory;
import IDirective = angular.IDirective;
import IScope = angular.IScope;

export class ShowIfDirective implements IDirective {

  public transclude: string = 'element';
  public priority: number = 600;

  public constructor(private taskService: TaskService, private ngIfDirective: any) {}

  public static factory(): IDirectiveFactory {
    const directive: IDirectiveFactory = (taskService: TaskService, ngIfDirective: any) =>
      new ShowIfDirective(taskService, ngIfDirective);
    directive.$inject = ['TaskService', 'ngIfDirective'];
    return directive;
  }

  public link(scope: IScope, element: any, attrs: any): void {
    attrs.ngIf = () => {
      let expression: string = attrs.showIf;
      let conditions: string[] = expression.split(' && ');
      let task: Task = this.taskService.getCurrentTask();
      let results: boolean[] = [];

      conditions.forEach((condition: string) => {
        let result: boolean = false;

        if (condition.indexOf('user-authorised-for-task') !== -1) {
          result = this.taskService.isCurrentUserAuthorised(task);
          result = condition.indexOf('!') !== -1 ? !result : result;
        } else if (condition.indexOf('task-assigned-to-current-user') !== -1) {
          result = this.taskService.isCurrentUserAssigned(task);
          result = condition.indexOf('!') !== -1 ? !result : result;
        } else if (condition.indexOf('task-assigned') !== -1) {
          result = this.taskService.isTaskClaimed(task);
          result = condition.indexOf('!') !== -1 ? !result : result;
        }

        results.push(result);
      });

      return results.reduce((a: boolean, b: boolean) =>  (a === b) ? a : false) === true;
    };

    this.ngIfDirective[0].link.apply(this.ngIfDirective[0], arguments);
  }
}
