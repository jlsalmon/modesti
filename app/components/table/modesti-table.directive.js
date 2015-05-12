'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:modestiTable
 * @description # modestiTable
 */
angular.module('modesti').directive('modestiTable', modestiTable);

function modestiTable() {
  var directive = {
    templateUrl : 'components/table/modesti-table.html',
    restrict : 'AE',
    transclude: true,
    controller : 'ModestiTableController as ctrl',
    scope: {
      request: '=request',
      schema:  '=schema',
      task: '=task'
    },

    link : function(scope, element, attrs, controller) {
      controller.init(scope.request, scope.schema, scope.task);
    }
  };

  return directive;
}
