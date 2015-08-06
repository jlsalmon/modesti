'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:cablingControls
 * @description # cablingControls
 */
angular.module('modesti').directive('cablingControls', cablingControls);

function cablingControls() {
  var directive = {
    templateUrl : 'components/request/controls/cabling-controls.html',
    restrict : 'AE',
    controller : 'CablingControlsController as ctrl',

    link : function(scope, element, attrs, controller) {
      controller.init(scope.$parent.ctrl);
    }
  };

  return directive;
}
