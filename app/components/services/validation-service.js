'use strict';

/**
 * @ngdoc service
 * @name modesti.ValidationService
 * @description # ValidationService Service in the modesti.
 */
angular.module('modesti').service('ValidationService', ValidationService);

function ValidationService($q) {
  var self = this;

  // Public API
  var service = {
    validateRequest: validateRequest
  };

  function validateRequest(rows, schema, hot) {
    var q = $q.defer();
    var valid = true, errors = [], checkedColumns = [];
    var category, field, column;

    schema.categories.forEach(function(category) {

      // First scan column by column
      category.fields.forEach(function(field) {

        var columnName = getPropertyName(field);

        if (checkedColumns.indexOf(columnName) > -1) {
          return;
        }


        column = getColumnByProperty(rows, columnName);

        //column =  hot.getDataAtProp(property);
        console.log('col: ' + column);

        column.forEach(function (value, row) {
          var col = hot.propToCol('properties.' + columnName);
          var cell = hot.getCellMeta(row, col);

          // Required fields
          if (field.required === true) {
            if (value === '' || value === undefined || value === null) {
              valid = false;
              console.log('required field validation failed');
              cell.valid = false;
              errors.push('Line ' + (row + 1) + ': Column "' + field.name_en + '" is mandatory');
            }
          }

          // Unique columns
          if (field.unique) {
            var data = $.extend([], column);
            var index = data.indexOf(value);
            data.splice(index, 1);
            var second_index = data.indexOf(value);
            cell.valid = !(index > -1 && second_index > -1);

            if (!cell.valid) {
              valid = false;
              console.log('cell failed uniqueness validation: [' + row + ', ' + col + ']');
              var error = 'Line ' + (row + 1) + ': Column "' + field.name_en + '" must be unique. Check for duplicate descriptions and attributes.';
              errors.push(error);
            }
          }

          // TODO: Min/max length validation

          // TODO: Unique columns validation

          // TODO: Unique tagnames, fault states, address parameter validations

          // TODO: column groups validation

          // TODO: Mutually exclusive groups validation

        });

        checkedColumns.push(columnName);
      });

      // Scan row by row
      rows.forEach(function(point, row) {

        var groupActive = false;
        category.fields.forEach(function(field) {


          var propertyName = getPropertyName(field);
          var col = hot.propToCol('properties.' + propertyName);
          var cell = hot.getCellMeta(row, col);


          // Column group validation
          if (typeof field.required === 'string' && field.required === 'group') {
            var value = getValueByPropertyName(point, propertyName);

            if (groupActive && (value === undefined || value === '' || value === null)) {
              valid = false;
              cell.valid = false;
              errors.push('Line ' + (row + 1) + ': Field "' + field.name_en + '" is required for points of type "' + category.name + '"');
            }

            if (value !== undefined && value !== '' && value !== null) {
              groupActive = true;
            }
          }
        });
      });

    });

    q.resolve({valid: valid, errors: errors});
    return q.promise;
  }

  /**
   * Get an entire column by its property name
   * @param rows
   * @param property
   * @returns {Array}
   */
  function getColumnByProperty(rows, property) {
    var column = [];

    var point;
    for (var i = 0, len = rows.length; i < len; i++) {
      point = rows[i];
      column.push(getValueByPropertyName(point, property));
    }

    return column;
  }

  /**
   * Get the value of the property of a point
   *
   * @param point
   * @param propertyName
   */
  function getValueByPropertyName(point, propertyName) {
    if (propertyName.indexOf('.') > -1) {
      var props = propertyName.split('.');
      return point.properties[props[0]][props[1]];
    } else {
      return point.properties[propertyName]
    }
  }

  /**
   * Get the name of a property from its field schema
   *
   * @param field
   */
  function getPropertyName(field) {
    if (field.type === 'autocomplete') {
      return field.id + '.' + (field.model ? field.model : 'value')
    } else {
      return field.id;
    }
  }

  return service;
}
