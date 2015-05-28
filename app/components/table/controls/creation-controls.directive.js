'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:creationControls
 * @description # creationControls
 */
angular.module('modesti').directive('creationControls', creationControls);

function creationControls() {
  var directive = {
    templateUrl : 'components/table/controls/creation-controls.html',
    restrict : 'AE',
    controller : 'CreationControlsController as ctrl',
    //scope: {
    //  parent: '='
    //},

    link : function(scope, element, attrs, controller) {
      controller.init(scope.$parent.ctrl.request, scope.$parent.ctrl.tasks);
    }
  };

  return directive;
}
