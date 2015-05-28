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
      request: '=',
      schema:  '=',
      tasks:   '='
    },

    link : function(scope, element, attrs, controller) {
      controller.init(scope.request, scope.schema, scope.tasks);
    }
  };

  return directive;
}