angular.module('modesti').directive('showIf', function(TaskService, ngIfDirective) {
  return {
    transclude: 'element',
    priority: 600,
    link: function(scope, element, attrs) {
      attrs.ngIf = function() {
        var expression = attrs.showIf;
        var conditions = expression.split(" && ");
        var task = TaskService.getCurrentTask();
        var results = [];

        conditions.forEach(function (condition) {
          var result = false;

          if (condition.indexOf('user-authorised-for-task') != -1) {
            result = TaskService.isCurrentUserAuthorised(task);
            result = condition.indexOf('!') != -1 ? !result : result;
          }
          else if (condition.indexOf('task-assigned-to-current-user') != -1) {
            result = TaskService.isCurrentUserAssigned(task);
            result = condition.indexOf('!') != -1 ? !result : result;
          }
          else if (condition.indexOf('task-assigned') != -1) {
            result = TaskService.isTaskClaimed(task);
            result = condition.indexOf('!') != -1 ? !result : result;
          }

          results.push(result);
        });

        return results.reduce(function (a, b) {
            return (a === b) ? a : false;
          }) === true;
      };

      ngIfDirective[0].link.apply(ngIfDirective[0], arguments);
    }
  }
});
