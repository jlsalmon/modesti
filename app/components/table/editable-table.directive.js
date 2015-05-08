'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:editableTable
 * @description # editableTable
 */
angular.module('modesti').directive('editableTable', editableTable);

function editableTable() {
  var directive = {
    templateUrl : 'components/table/editable-table.html',
    restrict : 'A',
    controller : 'EditableTableController as ctrl',
    scope: {
      request: '=request',
      schema:  '=schema'
    },

    link : function(scope, element, attrs, controller) {
      controller.init(scope.request, scope.schema);
    }
  };

  return directive;
}
