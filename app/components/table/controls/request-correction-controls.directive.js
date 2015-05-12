'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:requestCorrectionControls
 * @description # requestCorrectionControls
 */
angular.module('modesti').directive('requestCorrectionControls', requestCorrectionControls);

function requestCorrectionControls() {
  var directive = {
    templateUrl : 'components/table/controls/request-correction-controls.html',
    restrict : 'AE',
    require : ['^modestiTable', 'requestCorrectionControls'],
    controller : 'RequestCorrectionControlsController as ctrl',
    scope: {},

    link : function(scope, element, attrs, controllers) {
      var self = controllers[1];
      var parent = controllers[0];
      self.init(parent);
    }
  };

  return directive;
}
