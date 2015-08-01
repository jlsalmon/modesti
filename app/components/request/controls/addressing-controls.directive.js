'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:addressingControls
 * @description # addressingControls
 */
angular.module('modesti').directive('addressingControls', addressingControls);

function addressingControls() {
  var directive = {
    templateUrl : 'components/request/controls/addressing-controls.html',
    restrict : 'AE',
    controller : 'AddressingControlsController as ctrl',

    link : function(scope, element, attrs, controller) {
      controller.init(scope.$parent.ctrl.request, scope.$parent.ctrl.tasks);
    }
  };

  return directive;
}
