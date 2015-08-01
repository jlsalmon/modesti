'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:testingControls
 * @description # testingControls
 */
angular.module('modesti').directive('testingControls', testingControls);

function testingControls() {
  var directive = {
    templateUrl : 'components/table/controls/testing-controls.html',
    restrict : 'AE',
    controller : 'TestingControlsController as ctrl',

    link : function(scope, element, attrs, controller) {
      controller.init(scope.$parent.ctrl.request, scope.$parent.ctrl.tasks);
    }
  };

  return directive;
}
