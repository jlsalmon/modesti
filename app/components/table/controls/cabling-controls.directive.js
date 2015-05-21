'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:cablingControls
 * @description # cablingControls
 */
angular.module('modesti').directive('cablingControls', cablingControls);

function cablingControls() {
  var directive = {
    templateUrl : 'components/table/controls/cabling-controls.html',
    restrict : 'AE',
    require : ['^modestiTable', 'cablingControls'],
    controller : 'CablingControlsController as ctrl',
    scope: {},

    link : function(scope, element, attrs, controllers) {
      var self = controllers[1];
      var parent = controllers[0];
      self.init(parent);
    }
  };

  return directive;
}
