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
    require : ['^modestiTable', 'creationControls'],
    controller : 'CreationControlsController as ctrl',

    link : function(scope, element, attrs, controllers) {
      var self = controllers[1];
      var parent = controllers[0];
      self.init(parent);
    }
  };

  return directive;
}
