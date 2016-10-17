'use strict';

angular.module('modesti').directive('enableIf', function(TaskService, ngDisabledDirective) {
  return {
    link: function(scope, element, attrs) {
      attrs.ngDisabled = function() {
        var expression = attrs.enableIf;
        var conditions = expression.split(' && ');
        var task = TaskService.getCurrentTask();
        var results = [];

        conditions.forEach(function (condition) {
          var result = false;

          if (condition.indexOf('user-authorised-for-task') !== -1) {
            result = TaskService.isCurrentUserAuthorised(task);
            result = condition.indexOf('!') !== -1 ? !result : result;
          }
          else if (condition.indexOf('task-assigned-to-current-user') !== -1) {
            result = TaskService.isCurrentUserAssigned(task);
            result = condition.indexOf('!') !== -1 ? !result : result;
          }
          else if (condition.indexOf('task-assigned') !== -1) {
            result = TaskService.isTaskClaimed(task);
            result = condition.indexOf('!') !== -1 ? !result : result;
          }

          results.push(result);
        });

        return results.reduce(function(a, b){ return (a === b) ? a : false; }) !== true;
      };

      ngDisabledDirective[0].link.apply(ngDisabledDirective[0], arguments);
    }
  };
});
