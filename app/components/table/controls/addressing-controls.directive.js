'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:addressingControls
 * @description # addressingControls
 */
angular.module('modesti').directive('addressingControls', addressingControls);

function addressingControls() {
  var directive = {
    templateUrl : 'components/table/controls/addressing-controls.html',
    restrict : 'AE',
    require : ['addressingControls'],
    controller : 'AddressingControlsController as ctrl',

    link : function(scope, element, attrs, controller) {
      controller.init();
    }
  };

  return directive;
}
