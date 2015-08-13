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
    validateRequest: validateRequest,
    setErrorMessage: setErrorMessage
  };

  /**
   *
   * @param request
   * @param schema
   * @returns {*}
   */
  function validateRequest(request, schema) {
    var q = $q.defer();
    var points = request.points;

    // Reset all categories to valid
    for (var i in schema.categories) {
      schema.categories[i].valid = true;
    }

    var valid = true;

    if (request.status === 'IN_PROGRESS' || request.status === 'FOR_CORRECTION') {
      // Validate row by row
      if (!validateRows(points, schema)) valid = false;
      // Validate column by column
      if (!validateColumns(points, schema)) valid = false;
    }

    else if (request.status === 'FOR_APPROVAL') {
      // TODO move approval validation here
    }

    else if (request.status === 'FOR_ADDRESSING') {
      if (!validateAddresses(points, schema)) valid = false;
    }

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
      point.valid = undefined;
      point.errors = [];

      // Ignore empty points
      if (isEmptyPoint(point)) {
        continue;
      }

      var category;
      for (var j in schema.categories) {
        category = schema.categories[j];

        var field;
        for (var k in category.fields) {
          field = category.fields[k];

          var propertyName = getPropertyName(field);
          //point.errors[propertyName] = [];

          var value = getValueByPropertyName(point, propertyName);

          // Required fields
          if (field.required === true) {
            if (value === '' || value === undefined || value === null) {
              point.valid = category.valid = valid = false;
              setErrorMessage(point, propertyName, 'Field "' + field.name_en + '" is mandatory');
            }
          }

          // Min length
          if (field.minLength) {
            if (value && value.length < field.minLength) {
              point.valid = category.valid = valid = false;
              setErrorMessage(point, propertyName, 'Field "' + field.name_en + '" must be at least ' + field.minLength + ' characters in length');
            }
          }

          // Max length
          if (field.maxLength) {
            if (value && value.length > field.maxLength) {
              //cell.valid = false;
              point.valid = category.valid = valid = false;
              setErrorMessage(point, propertyName, 'Field "' + field.name_en + '" must not exceed ' + field.maxLength + ' characters in length');
            }
          }
        }

        // Validate additional constraints
        valid = checkConstraints(point, category);
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

          // Ignore empty points
          if (isEmptyPoint(point)) {
            continue;
          }

          // Unique columns
          if (field.unique) {
            var data = $.extend([], column);
            var index = data.indexOf(value);
            data.splice(index, 1);
            var second_index = data.indexOf(value);

            if (value && index > -1 && second_index > -1) {
              point.valid = category.valid = valid = false;
              setErrorMessage(point, columnName, 'Column "' + field.name_en + '" must be unique. Check for duplicate descriptions and attributes.');
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
   * Checks that all points have a correct address based on their point type.
   *
   * @param points
   * @param schema
   */
  function validateAddresses(points, schema) {
    var point, valid = true;

    for (var i = 0, len = points.length; i < len; i++) {
      point = points[i];
      point.errors = [];

      var pointType = point.properties.pointType;

      // Find the category that matches the point type
      var category;
      for (var key in schema.categories) {
        category = schema.categories[key];

        if (category.name === pointType) {
          // TODO: do this properly. This is hacked by making any "xnor" constraints become "and" constraints
          for (var l in category.constraints) {
            var constraint = category.constraints[l];

            if (constraint.type === 'xnor') {
              constraint.type = 'and';
            }
          }

          valid = checkConstraints(point, category);
        }
      }





      //
      //if (pointType === 'APIMMD' && !point.properties.plcBlockType) {
      //  ValidationService.setErrorMessage(point, 'plcBlockType', 'A complete APIMMD address is required for this point');
      //  ValidationService.setErrorMessage(point, 'plcWordId', 'A complete APIMMD address is required for this point');
      //  ValidationService.setErrorMessage(point, 'plcBitId', 'A complete APIMMD address is required for this point');
      //  valid = false;
      //}
      //
      //else if (pointType === 'LSAC' && !point.properties.lsacType) {
      //  ValidationService.setErrorMessage(point, 'lsacType', 'A complete LSAC address is required for this point');
      //  ValidationService.setErrorMessage(point, 'lsacCard', 'A complete LSAC address is required for this point');
      //  ValidationService.setErrorMessage(point, 'lsacRack', 'A complete LSAC address is required for this point');
      //  ValidationService.setErrorMessage(point, 'lsacPort', 'A complete LSAC address is required for this point');
      //  valid = false;
      //}

    }
  }

  function checkConstraints(point, category) {
    var valid = true;

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
              setErrorMessage(point, getPropertyName(field), 'At least one of "' + columnNames.join(', ') + '" is required for group "' + category.name + '"');
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
              setErrorMessage(point, getPropertyName(field), 'Field "' + field.name_en + '" is required for points of type "' + category.name + '"');
            }
          }
          break;
        }
        case 'and':
        {
          if (emptyFields.length === constraint.members.length) {
            // If all fields are empty, say "Address of type X is required"
            point.valid = category.valid = valid = false;

            for (var o in emptyFields) {
              var field = emptyFields[o];
              setErrorMessage(point, getPropertyName(field), 'All fields in group "' + category.name + '" are required for this point');
            }
          }
          if (emptyFields.length > 0) {
            // If some are filled, say "Field X is required"
            point.valid = category.valid = valid = false;

            for (var o in emptyFields) {
              var field = emptyFields[o];
              setErrorMessage(point, getPropertyName(field), 'Field "' + field.name_en + '" is required for points of type "' + category.name + '"');
            }
          }
          break;
        }
      }
    }

    return valid;
  }

  /**
   *
   * @param point
   * @param propertyName
   * @param error
   */
  function setErrorMessage(point, propertyName, message) {
    var error;

    for (var i in point.errors) {
      if (point.errors[i].property === propertyName) {
        error = point.errors[i];
      }
    }

    if (!error) {
      point.errors.push({property: propertyName, errors: [message]});
    }
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

  /**
   * Check if a point is empty. A point is considered to be empty if it contains no properties, or if the values of all
   * its properties are either null, undefined or empty strings.
   *
   * @param point the point to check
   * @returns {boolean} true if the point is empty, false otherwise
   */
  function isEmptyPoint(point) {
    if (Object.keys(point.properties).length == 0) {
      return true;
    }

    var property;
    for (var key in point.properties) {
      if (point.properties.hasOwnProperty(key)) {
        property = point.properties[key];

        if (typeof property === 'object') {
          for (var subproperty in property) {
            if (property.hasOwnProperty(subproperty)) {
              if (property[subproperty] !== null && property[subproperty] !== undefined && property[subproperty] != '') {
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

  return service;
}
