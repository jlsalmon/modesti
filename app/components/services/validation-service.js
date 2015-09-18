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

    // Reset all categories and datasources to valid
    schema.categories.concat(schema.datasources).forEach(function (category) {
      category.valid = true;
    });

    var valid = true;

    // Validate each point separately. This checks things like required values, min/max length, valid values etc.
    points.forEach(function (point) {
      if (!validatePoint(point, schema)) {
        valid = false;
      }
    });

    // Validate the request as a whole. This checks things like unique column groups, mutually exclusive columns, etc.
    if (!validateConstraints(request, schema)) {
      valid = false;
    }

    q.resolve(valid);
    return q.promise;
  }

  /**
   *
   * @param point
   * @param schema
   * @returns {boolean}
   */
  function validatePoint(point, schema) {
    var valid = true;
    point.valid = undefined;
    point.errors = [];

    // Ignore empty points
    if (isEmptyPoint(point)) {
      return true;
    }

    schema.categories.concat(schema.datasources).forEach(function (category) {
      category.fields.forEach(function (field) {

        var propertyName = getPropertyName(field);
        var value = getValueByPropertyName(point, propertyName);

        // Check for invalid fields
        if (!isValidValue(value, point, field)) {
          point.valid = category.valid = valid = false;
          setErrorMessage(point, propertyName, 'Value "' + value + '" is not a legal option for field "' + field.name_en + '". Please select a value from the list.');
        }


        // Required fields (can be simple boolean or condition list)
        var required;
        if (field.required === true) {
          required = true;
        } else if (field.required !== null && typeof field.required === 'object') {
          required = evaluateCondition(point, field.required);
        }

        if (required === true) {
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
      });
    });

    return valid;
  }

  /**
   *
   * @param point
   * @param condition
   * @returns {boolean}
   */
  function evaluateCondition(point, condition) {
    var result = false;

    // Chained OR condition
    if (condition.or) {
      condition.or.forEach(function (subCondition) {
        var value = point.properties[subCondition.field];
        if (value === subCondition.value) {
          result = true;
        }
      });
    }

    // Simple condition
    else {
      var value = point.properties[condition.field];
      if (value === condition.value) {
        result = true;
      }
    }

    return result;
  }

  /**
   * TODO: Mutually exclusive groups validation
   *
   * @param request
   * @param schema
   * @returns {boolean}
   */
  function validateConstraints(request, schema) {
    var valid = true;
    var points = request.points;

    schema.categories.concat(schema.datasources).forEach(function (category) {
      if (!category.constraints) {
        return;
      }

      category.constraints.forEach(function (constraint) {

        // Some constraints are only active during certain request statuses
        if (constraint.activeStates && constraint.activeStates.indexOf(request.status) == -1) {
          return;
        }

        switch (constraint.type) {
          case 'or':
          {
            if (!validateOrConstraint(points, category, constraint)) {
              valid = false;
            }
            break;
          }
          case 'and':
          {
            if (!validateAndConstraint(points, category, constraint)) {
              valid = false;
            }
            break;
          }
          case 'xnor':
          {
            if (!validateXnorConstraint(points, category, constraint)) {
              valid = false;
            }
            break;
          }
          case 'unique':
          {
            if (!validateUniqueConstraint(points, category, constraint)) {
              valid = false;
            }
            break;
          }
        }
      });
    });

    return valid;
  }

  /**
   *
   * @param points
   * @param category
   * @param constraint
   * @returns {boolean}
   */
  function validateOrConstraint(points, category, constraint) {
    var valid = true;

    points.forEach(function (point) {
      // Ignore empty points
      if (isEmptyPoint(point)) {
        return true;
      }

      // Get all the fields specified as members of the constraint
      var fields = getFields(category, constraint.members);

      // Get a list of fields for the constraint that are empty
      var emptyFields = getEmptyFields(point, fields);

      if (emptyFields.length == constraint.members.length) {
        point.valid = category.valid = valid = false;
        var fieldNames = getFieldNames(category, constraint.members);

        emptyFields.forEach(function (emptyField) {
          setErrorMessage(point, getPropertyName(emptyField), 'At least one of "' + fieldNames.join(', ') + '" is required for group "' + category.name_en + '"');
        });
      }
    });

    return valid;
  }

  /**
   *
   * @param points
   * @param category
   * @param constraint
   * @returns {boolean}
   */
  function validateAndConstraint(points, category, constraint) {
    var valid = true;

    points.forEach(function (point) {
      // Ignore empty points
      if (isEmptyPoint(point)) {
        return true;
      }

      // Some constraints are only evaluated when their condition is true
      if (constraint.condition) {
        var value = point.properties[constraint.condition.field];
        if (value !== constraint.condition.value) {
          return;
        }
      }

      // Get all the fields specified as members of the constraint
      var fields = getFields(category, constraint.members);

      // Get a list of fields for the constraint that are empty
      var emptyFields = getEmptyFields(point, fields);

      if (emptyFields.length === constraint.members.length) {
        // If all fields are empty, say "Address of type X is required"
        point.valid = category.valid = valid = false;

        emptyFields.forEach(function (emptyField) {
          setErrorMessage(point, getPropertyName(emptyField), 'All fields in group "' + category.name_en + '" are required for this point');
        });
      }
      if (emptyFields.length > 0) {
        // If some are filled, say "Field X is required"
        point.valid = category.valid = valid = false;

        emptyFields.forEach(function (emptyField) {
          setErrorMessage(point, getPropertyName(emptyField), 'Field "' + emptyField.name_en + '" is required for points of type "' + category.name_en + '"');
        });
      }
    });

    return valid;
  }

  /**
   *
   * @param points
   * @param category
   * @param constraint
   * @returns {boolean}
   */
  function validateXnorConstraint(points, category, constraint) {
    var valid = true;

    points.forEach(function (point) {
      // Ignore empty points
      if (isEmptyPoint(point)) {
        return true;
      }

      // Get all the fields specified as members of the constraint
      var fields = getFields(category, constraint.members);

      // Get a list of fields for the constraint that are empty
      var emptyFields = getEmptyFields(point, fields);

      if (emptyFields.length != 0 && emptyFields.length != constraint.members.length) {
        point.valid = category.valid = valid = false;

        emptyFields.forEach(function (emptyField) {
          setErrorMessage(point, getPropertyName(emptyField), 'Field "' + emptyField.name_en + '" is required for points of type "' + category.name_en + '"');
        });
      }
    });

    return valid;


  }

  /**
   * Unique constraints apply to the entire request. For example, a constraint with two members means that the result of
   * the concatenation of the values of those members must be unique for all points.
   *
   *
   * @param points
   * @param category
   * @param constraint
   * @returns {boolean}
   */
  function validateUniqueConstraint(points, category, constraint) {
    var valid = true;
    var concatenatedValues = [];

    // Build a new array containing the concatenation of the values of all constraint members
    points.forEach(function (point) {
      var concatenatedValue = '';

      constraint.members.forEach(function (member) {
        var value = getValueByPropertyName(point, member);
        if (value !== undefined && value !== null && value !== '') {
          concatenatedValue += value;
        }
      });

      concatenatedValues.push(concatenatedValue);
    });

    points.forEach(function (point, i) {
      // Ignore empty points
      if (isEmptyPoint(point)) {
        return true;
      }

      var value = concatenatedValues[i];

      var data = $.extend([], concatenatedValues);
      var index = data.indexOf(value);
      data.splice(index, 1);
      var second_index = data.indexOf(value);

      if (value && index > -1 && second_index > -1) {
        point.valid = category.valid = valid = false;
        setErrorMessage(point, '', 'Columns "' + getFieldNames(category, constraint.members).join(', ') + '" must be unique for all points. Check for duplications.');
      }
    });

    return valid;
  }

  /**
   *
   * @param category
   * @param fieldIds
   */
  function getFields(category, fieldIds) {
    var fields = [];

    category.fields.forEach(function (field) {
      if (fieldIds.indexOf(field.id) > -1) {
        fields.push(field);
      }
    });

    return fields;
  }

  /**
   *
   * @param point
   * @param fields
   * @returns {Array}
   */
  function getEmptyFields(point, fields) {
    var emptyFields = [];

    fields.forEach(function (field) {
      var value = getValueByPropertyName(point, getPropertyName(field));
      if (value === undefined || value === '' || value === null) {
        emptyFields.push(field);
      }
    });

    return emptyFields;
  }

  /**
   *
   * @param category
   * @param fieldIds
   * @returns {Array}
   */
  function getFieldNames(category, fieldIds) {
    var fieldNames = [];

    getFields(category, fieldIds).forEach(function (field) {
      fieldNames.push(field.name_en);
    });

    return fieldNames;
  }

  /**
   *
   * @param point
   * @param propertyName
   * @param message
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

  /**
   *
   * @param value
   * @param point
   * @param field
   * @returns {boolean} true if the value is valid, false otherwise
   */
  function isValidValue(value, point, field) {
    // If the value is empty, it's technically not invalid.
    if (value === undefined || value === null || value === '') {
      return true;
    }

    // If we have an options field, check that the value is in the static list of options
    if (field.type === 'options' && field.options && field.options instanceof Array) {
      for (var key in field.options) {
        var option = field.options[key];

        if (value !== undefined && value !== null && value !== '' && value == option) {
          return true;
        }

        // Fiddle with the meaning and check again
        if (value !== undefined && value !== null && value !== ''  && (value == option.split(':')[0] || value == option.replace(':', ''))) {
          return true;
        }
      }

      return false;
    }

    // Otherwise, if we have an autocomplete field, make a call to the backend to see if this value returns any results
    else if (field.type === 'autocomplete') {

      // If no results are found in the source function, then this field will have been marked as invalid.
      return !(point.invalidFields && point.invalidFields.indexOf(field.id) > -1);

    }

    else {
      return true;
    }
  }

  return service;
}
