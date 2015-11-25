'use strict';

/**
 * @ngdoc filter
 * @name modesti.filter:active
 * @function
 * @description # active
 */
angular.module('modesti').filter('active', active);

function active() {
  /*jshint camelcase: false */

  return function (datasources, request) {
    var result = [];

    request.points.forEach(function (point) {
      datasources.forEach(function (datasource) {

        if (point.properties.pointType &&
          (point.properties.pointType === angular.uppercase(datasource.id) || point.properties.pointType === angular.uppercase(datasource.name_en))) {
          if (result.indexOf(datasource) === -1) {
            result.push(datasource);
          }
        }
      });
    });

    return result;
  };
}
