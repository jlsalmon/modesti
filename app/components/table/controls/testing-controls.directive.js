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
    require : ['^modestiTable', 'testingControls'],
    controller : 'TestingControlsController as ctrl',
    scope: {},

    link : function(scope, element, attrs, controllers) {
      var self = controllers[1];
      var parent = controllers[0];
      self.init(parent);
    }
  };

  return directive;
}
