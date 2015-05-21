'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:approvalControls
 * @description # approvalControls
 */
angular.module('modesti').directive('approvalControls', approvalControls);

function approvalControls() {
  var directive = {
    templateUrl : 'components/table/controls/approval-controls.html',
    restrict : 'AE',
    require : ['^modestiTable', 'approvalControls'],
    controller : 'ApprovalControlsController as ctrl',
    scope: {},

    link : function(scope, element, attrs, controllers) {
      var self = controllers[1];
      var parent = controllers[0];
      self.init(parent);
    }
  };

  return directive;
}
