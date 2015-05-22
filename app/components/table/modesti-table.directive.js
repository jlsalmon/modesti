'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:modestiTable
 * @description # modestiTable
 */
angular.module('modesti').directive('modestiTable', modestiTable);

function modestiTable() {
  var directive = {
    templateUrl : 'components/table/modesti-table.html',
    restrict : 'AE',
    transclude: true,
    controller : 'ModestiTableController as ctrl',
    scope: {
      request: '=',
      schema:  '=',
      tasks:   '=',
    },

    link : function(scope, element, attrs, controller) {
      controller.init(scope.request, scope.schema, scope.tasks);
    }
  };

  return directive;
}


angular.module('modesti').directive('loadingContainer', function () {
  return {
    restrict: 'A',
    scope: false,
    link: function (scope, element, attrs) {
      var loadingLayer = angular.element('<div class="loading"></div>');
      element.append(loadingLayer);
      element.addClass('loading-container');
      scope.$watch(attrs.loadingContainer, function (value) {
        loadingLayer.toggleClass('ng-hide', !value);
      });
    }
  };
})