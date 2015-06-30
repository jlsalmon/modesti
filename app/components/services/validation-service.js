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

  /**
   *
   * @param points
   * @param schema
   * @returns {*}
   */
  function validateRequest(points, schema) {
    var q = $q.defer();

    // Reset all categories to valid
    for (var i in schema.categories) {
      schema.categories[i].valid = true;
    }

    var valid = true;

    // Validate row by row
    if (!validateRows(points, schema)) valid = false;
    // Validate column by column
    if (!validateColumns(points, schema)) valid = false;

    q.resolve(valid);
    return q.promise;
  }

  /**
   *
   * @param points
   * @param schema
   * @returns {boolean}
   */
  function validateRows(points, schema) {
    var point, valid = true;

    for (var i in points) {
      point = points[i];
      point.valid = true;
      point.errors = {};

      // Empty rows are valid
      if (Object.keys(point.properties).length <= 1) {
        continue;
      }

      var category;
      for (var j in schema.categories) {
        category = schema.categories[j];

        var field;
        for (var k in category.fields) {
          field = category.fields[k];

          var propertyName = getPropertyName(field);
          point.errors[propertyName] = [];

          var value = getValueByPropertyName(point, propertyName);

          // Required fields
          if (field.required === true) {
            if (value === '' || value === undefined || value === null) {
              point.valid = category.valid = valid = false;
              point.errors[propertyName].push('Line ' + (point.id) + ': Field "' + field.name_en + '" is mandatory');
            }
          }

          // Min length
          if (field.minLength) {
            if (value && value.length < field.minLength) {
              point.valid = category.valid = valid = false;
              point.errors[propertyName].push('Line ' + (point.id) + ': Field "' + field.name_en + '" must be at least ' + field.minLength + ' characters in length');
            }
          }

          // Max length
          if (field.maxLength) {
            if (value && value.length > field.maxLength) {
              //cell.valid = false;
              point.valid = category.valid = valid = false;
              point.errors[propertyName].push('Line ' + (point.id) + ': Field "' + field.name_en + '" must not exceed ' + field.maxLength + ' characters in length');
            }
          }
        }

        // Validate additional constraints
        for (var l in category.constraints) {
          var constraint = category.constraints[l];

          // Get all the fields specified as members of the constraint
          var fields = [];
          for (var n in category.fields) {
            var field = category.fields[n];
            if (constraint.members.indexOf(field.id) > -1) {
              fields.push(field);
            }
          }

          // Check the values of all fields for this point
          var emptyFields = [], columnNames = [];
          for (var m in fields) {
            var field = fields[m];
            columnNames.push(field.name_en);
            var value = getValueByPropertyName(point, getPropertyName(field));
            if (value === undefined || value === '' || value === null) {
              emptyFields.push(field);
            }
          }

          switch (constraint.type) {
            case 'or':
            {
              if (emptyFields.length == constraint.members.length) {
                point.valid = category.valid = valid = false;
                for (var o in emptyFields) {
                  var field = emptyFields[o];
                  point.errors[field.id].push('Line ' + point.id + ': At least one of "' + columnNames.join(', ') + '" is required for category "' + category.name + '"');
                }

              }
              break;
            }
            case 'xnor':
            {
              if (emptyFields.length != 0 && emptyFields.length != constraint.members.length) {
                point.valid = category.valid = valid = false;

                for (var o in emptyFields) {
                  var field = emptyFields[o];
                  point.errors[field.id].push('Line ' + point.id + ': Field "' + field.name_en + '" is required for points of category "' + category.name
                  + '" if other fields of that category have been specified');
                }
              }
              break;
            }
          }
        }
      }
    }

    return valid;
  }


  /**
   *
   * @param points
   * @param schema
   * @returns {boolean}
   */
  function validateColumns(points, schema) {
    var valid = true, checkedColumns = [];

    var category;
    for (var i in schema.categories) {
      category = schema.categories[i];

      var field;
      for (var j in category.fields) {
        field = category.fields[j];

        var columnName = getPropertyName(field);
        if (checkedColumns.indexOf(columnName) > -1) {
          continue;
        }

        var column = getColumnByProperty(points, columnName);

        var value, point;
        for (var row in column) {
          value = column[row];
          point = points[row];

          // Unique columns
          if (field.unique) {
            var data = $.extend([], column);
            var index = data.indexOf(value);
            data.splice(index, 1);
            var second_index = data.indexOf(value);

            if (index > -1 && second_index > -1) {
              point.valid = category.valid = valid = false;
              point.errors[columnName].push('Line ' + (point.id) + ': Column "' + field.name_en + '" must be unique. Check for duplicate descriptions and attributes.');
            }
          }


          // TODO: Unique columns validation

          // TODO: Unique tagnames, fault states, address parameter validations

          // TODO: column groups validation

          // TODO: Mutually exclusive groups validation
        }

        checkedColumns.push(columnName);
      }
    }

    return valid;
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
    var value;

    if (propertyName.indexOf('.') > -1) {
      var props = propertyName.split('.');
      if (point.properties.hasOwnProperty(props[0]) && point.properties[props[0]].hasOwnProperty(props[1])) {
        value = point.properties[props[0]][props[1]];
      }
    } else {
      if (point.properties.hasOwnProperty(propertyName)) {
        value = point.properties[propertyName]
      }
    }

    return value;
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
