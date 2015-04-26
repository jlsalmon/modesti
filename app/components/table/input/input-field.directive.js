'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:inputField
 * @description
 * # inputField
 */
angular.module('modesti').directive('inputField', inputField);

function inputField() {
  return {
    restrict : 'A',
    controller : 'InputFieldController as ctrl',
    scope : {
      schema : '=schema',
      model  : '=model'
    },

    link : function(scope, element, attrs, controller) {
      controller.init(scope, element);
    }
  };
}
