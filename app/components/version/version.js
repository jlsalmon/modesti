'use strict';

/**
 * @ngdoc directive
 * @name modesti:appVersion
 *
 * @description
 * Prints the current version of the app.
 */
angular.module('modesti').directive('appVersion', version);

function version(properties) {
  return function (scope, element) {
    element.text(properties.version);
  };
}