'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:requestCreationControls
 * @description # requestCreationControls
 */
angular.module('modesti').directive('requestCreationControls', requestCreationControls);

function requestCreationControls() {
  var directive = {
    templateUrl : 'components/table/controls/request-creation-controls.html',
    restrict : 'AE',
    require : ['^modestiTable', 'requestCreationControls'],
    controller : 'RequestCreationControlsController as ctrl',
    scope: {},

    link : function(scope, element, attrs, controllers) {
      var self = controllers[1];
      var parent = controllers[0];
      self.init(parent);
    }
  };

  return directive;
}
