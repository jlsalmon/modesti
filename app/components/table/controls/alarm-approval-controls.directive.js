'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:alarmApprovalControls
 * @description # alarmApprovalControls
 */
angular.module('modesti').directive('alarmApprovalControls', alarmApprovalControls);

function alarmApprovalControls() {
  var directive = {
    templateUrl : 'components/table/controls/alarm-approval-controls.html',
    restrict : 'AE',
    require : ['^modestiTable', 'alarmApprovalControls'],
    controller : 'AlarmApprovalControlsController as ctrl',
    scope: {},

    link : function(scope, element, attrs, controllers) {
      var self = controllers[1];
      var parent = controllers[0];
      self.init(parent);
    }
  };

  return directive;
}
