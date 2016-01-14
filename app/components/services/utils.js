'use strict';

/**
 * @ngdoc factory
 * @name modesti.Utils
 * @description # Utils
 */
angular.module('modesti').factory('Utils', Utils);

function Utils() {

  return {
    isEmptyPoint: isEmptyPoint,
    getField: getField
  };

  /**
   * Check if a point is empty. A point is considered to be empty if it contains no properties, or if the values of all
   * its properties are either null, undefined or empty strings.
   *
   * @param point the point to check
   * @returns {boolean} true if the point is empty, false otherwise
   */
  function isEmptyPoint(point) {
    if (Object.keys(point.properties).length === 0) {
      return true;
    }

    var property;
    for (var key in point.properties) {
      if (point.properties.hasOwnProperty(key)) {
        property = point.properties[key];

        if (typeof property === 'object') {
          for (var subproperty in property) {
            if (property.hasOwnProperty(subproperty)) {
              if (property[subproperty] !== null && property[subproperty] !== undefined && property[subproperty] !== '') {
                return false;
              }
            }
          }
        } else if (property !== null && property !== undefined && property !== '') {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Retrieve a field object matching the given id from the given schema.
   *
   * @param schema
   * @param id
   * @returns {*}
   */
  function getField(schema, id) {
    var result;

    schema.categories.forEach(function (category) {
      category.fields.forEach(function (field) {
        if (field.id === id) {
          result = field;
        }
      });
    });

    schema.datasources.forEach(function (datasource) {
      datasource.fields.forEach(function (field) {
        if (field.id === id) {
          result = field;
        }
      });
    });

    return result;
  }
}
