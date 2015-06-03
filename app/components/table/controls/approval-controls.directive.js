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
    controller : 'ApprovalControlsController as ctrl',

    link : function(scope, element, attrs, controller) {
      controller.init(scope.$parent.ctrl);
    }
  };

  return directive;
}
