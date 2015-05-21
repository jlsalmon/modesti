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
    require : ['^modestiTable', 'addressingControls'],
    controller : 'AddressingControlsController as ctrl',
    scope: {},

    link : function(scope, element, attrs, controllers) {
      var self = controllers[1];
      var parent = controllers[0];
      self.init(parent);
    }
  };

  return directive;
}
