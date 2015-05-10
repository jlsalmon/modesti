'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:pointTestingControls
 * @description # pointTestingControls
 */
angular.module('modesti').directive('pointTestingControls', pointTestingControls);

function pointTestingControls() {
  var directive = {
    templateUrl : 'components/table/controls/point-testing-controls.html',
    restrict : 'AE',
    require : ['^modestiTable', 'pointTestingControls'],
    controller : 'PointTestingControlsController as ctrl',
    scope: {},

    link : function(scope, element, attrs, controllers) {
      var self = controllers[1];
      var parent = controllers[0];
      self.init(parent);
    }
  };

  return directive;
}
