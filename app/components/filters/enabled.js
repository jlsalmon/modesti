'use strict';

/**
 * @ngdoc filter
 * @name modesti.filter:enabled
 * @function
 * @description # enabled Filters a list of categories and returns those that
 *              are enabled for the given workflow state.
 */
angular.module('modesti').filter('enabled', enabled);

function enabled() {
  return function (input, status) {
    var result = [];

    input.map(function(category) {
      if (!category.disabledStates || category.disabledStates.indexOf(status) == -1){
        result.push(category)
      }
    });

    return result;
  };
}