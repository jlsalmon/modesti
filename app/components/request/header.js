'use strict';

angular.module('modesti').directive('requestHeader', function RequestHeaderDirective() {
  return {
    controller: RequestHeaderController,
    controllerAs: 'ctrl',
    templateUrl: 'components/request/header.html',
    scope: {},
    bindToController: {
      request: '='
    }
  };
});

function RequestHeaderController() {}
