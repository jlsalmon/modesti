'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:configControls
 * @description # configControls
 */
angular.module('modesti').directive('configControls', configControls);

function configControls() {
  var directive = {
    templateUrl : 'components/request/controls/config-controls.html',
    restrict : 'AE',
    controller : 'ConfigControlsController as ctrl',

    link : function(scope, element, attrs, controller) {
      controller.init(scope.$parent.ctrl);
    }
  };

  return directive;
}
