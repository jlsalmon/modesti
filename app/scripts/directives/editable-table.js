'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:editableTable
 * @description # editableTable
 */
angular.module('modesti').directive('editableTable', editableTable);

function editableTable() {
  var directive = {
    templateUrl : 'views/templates/editable-table.html',
    restrict : 'A',
    controller : 'EditableTableController as ctrl',
    scope: {
      request: '=request'
    },

    link : function(scope, element, attrs, controller) {
      controller.init();
    }
  };
  
  return directive;
};
