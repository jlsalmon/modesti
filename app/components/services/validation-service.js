'use strict';

/**
 * @ngdoc service
 * @name modesti.ValidationService
 * @description # ValidationService
 */
angular.module('modesti').service('ValidationService', ValidationService);

function ValidationService($q, SchemaService, RequestService, TaskService, Utils) {
  /*jshint camelcase: false */

  // Public API
  var service = {
    validateRequest: validateRequest,
    setErrorMessage: setErrorMessage,
  };

  /**
   *
   * @param request
   * @param task
   * @param schema
   * @returns {*}
   */
  function validateRequest(request, task, schema) {
    var q = $q.defer();

    // Reset all categories and datasources to valid
    schema.categories.concat(schema.datasources).forEach(function (category) {
      category.valid = true;
    });

    // Reset all points and clear any error messages.
    request.points.forEach(function (point) {
      point.properties.valid = undefined;
      point.errors = [];
    });

    var valid = true;

    // Validate the mutually exclusive column group specifications.
    if (!validateMutualExclusions(request, schema)) {
      valid = false;
    }

    // Validate the constraints of the schema. This checks things like unique column groups and mutually inclusive fields.
    if (!validateConstraints(request, schema)) {
      valid = false;
    }

    // Validate each point separately. This checks things like required values, min/max length, valid values etc.
    if (!validatePoints(request.points, schema, request.status)) {
      valid = false;
    }

    // If we found errors already, don't bother to call the backend validations.
    if (valid === false) {
      request.properties.valid = false;
      q.resolve(request);
      return q.promise;
    }

    // All good so far! Call the backend validations

    // First save the request
    RequestService.saveRequest(request).then(function () {
      console.log('saved request before validation');

      // Complete the task associated with the request
      TaskService.completeTask(task.name, request.requestId).then(function () {
        console.log('completed task ' + task.name);

        // Clear the cache
        RequestService.clearCache();

        // Get the request once again
        RequestService.getRequest(request.requestId).then(function (request) {
          q.resolve(request);
        });
      },

      function (error) {
        console.log('error completing task: ' + error.statusText);
        q.reject(error);
      });
    },

    function (error) {
      console.log('error saving before validation: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  /**
   *
   * @param points
   * @param schema
   * @param status
   * @returns {boolean}
   */
  function validatePoints(points, schema, status) {
    var valid = true;

    points.forEach(function (point) {

      // Ignore empty points
      if (Utils.isEmptyPoint(point)) {
        return true;
      }

      schema.categories.concat(schema.datasources).forEach(function (category) {
        category.fields.forEach(function (field) {

          var propertyName = getPropertyName(field);
          var value = getValueByPropertyName(point, propertyName);

          // Check for invalid fields
          if (!isValidValue(value, point, field)) {
            point.properties.valid = category.valid = valid = false;
            setErrorMessage(point, propertyName, 'Value "' + value + '" is not a legal option for field "' + field.name_en + '". Please select a value from the list.');
          }

          // Validate unique fields
          if (field.unique === true) {
            if (!validateUniqueConstraint(points, category, {type: 'unique', members: [field.id]})) {
              valid = false;
            }
          }

          // Required fields (can be simple boolean or condition list)
          var required;
          if (field.required === true) {
            required = true;
          } else if (field.required !== null && typeof field.required === 'object') {
            required = SchemaService.evaluateConditional(point, field.required, status);
          }

          if (required === true) {
            if (value === '' || value === undefined || value === null) {
              point.properties.valid = category.valid = valid = false;
              setErrorMessage(point, propertyName, 'Field "' + field.name_en + '" is mandatory');
            }
          }

          // Min length
          if (field.minLength) {
            if (value && value.length < field.minLength) {
              point.properties.valid = category.valid = valid = false;
              setErrorMessage(point, propertyName, 'Field "' + field.name_en + '" must be at least ' + field.minLength + ' characters in length');
            }
          }

          // Max length
          if (field.maxLength) {
            if (value && value.length > field.maxLength) {
              //cell.valid = false;
              point.properties.valid = category.valid = valid = false;
              setErrorMessage(point, propertyName, 'Field "' + field.name_en + '" must not exceed ' + field.maxLength + ' characters in length');
            }
          }
        });
      });
    });

    return valid;
  }

  /**
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

        switch (constraint.type) {
          case 'or':
          {
            if (!validateOrConstraint(points, category, constraint, request.status)) {
              valid = false;
            }
            break;
          }
          case 'and':
          {
            if (!validateAndConstraint(points, category, constraint, request.status)) {
              valid = false;
            }
            break;
          }
          case 'xnor':
          {
            if (!validateXnorConstraint(points, category, constraint, request.status)) {
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
  function validateOrConstraint(points, category, constraint, status) {
    var valid = true;

    points.forEach(function (point) {
      // Ignore empty points
      if (Utils.isEmptyPoint(point)) {
        return true;
      }

      // Constraints are only applied if the category is editable.
      var editable = SchemaService.evaluateConditional(point, category.editable, status);
      if (!editable) {
        return true;
      }

      // Get all the fields specified as members of the constraint
      var fields = getFields(category, constraint.members);

      // Get a list of fields for the constraint that are empty
      var emptyFields = getEmptyFields(point, fields);

      if (emptyFields.length === constraint.members.length) {
        point.properties.valid = category.valid = valid = false;
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
  function validateAndConstraint(points, category, constraint, status) {
    var valid = true;

    points.forEach(function (point) {
      // Ignore empty points
      if (Utils.isEmptyPoint(point)) {
        return true;
      }

      // Constraints are only applied if the category is editable.
      var editable = SchemaService.evaluateConditional(point, category.editable, status);
      if (!editable) {
        return true;
      }

      // Get all the fields specified as members of the constraint
      var fields = getFields(category, constraint.members);

      // Get a list of fields for the constraint that are empty
      var emptyFields = getEmptyFields(point, fields);

      if (emptyFields.length > 0) {
        point.properties.valid = category.valid = valid = false;

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
  function validateXnorConstraint(points, category, constraint, status) {
    var valid = true;

    points.forEach(function (point) {
      // Ignore empty points
      if (Utils.isEmptyPoint(point)) {
        return true;
      }

      // Constraints are only applied if the category is editable.
      var editable = SchemaService.evaluateConditional(point, category.editable, status);
      if (!editable) {
        return true;
      }

      // Get all the fields specified as members of the constraint
      var fields = getFields(category, constraint.members);

      // Get a list of fields for the constraint that are empty
      var emptyFields = getEmptyFields(point, fields);

      if (emptyFields.length !== 0 && emptyFields.length !== constraint.members.length) {
        point.properties.valid = category.valid = valid = false;

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
      if (Utils.isEmptyPoint(point)) {
        return true;
      }

      var value = concatenatedValues[i];

      var data = $.extend([], concatenatedValues);
      var index = data.indexOf(value);
      data.splice(index, 1);
      var secondIndex = data.indexOf(value);

      if (value && index > -1 && secondIndex > -1) {
        point.properties.valid = category.valid = valid = false;

        if (constraint.members.length === 1) {
          var fieldName = getFieldNames(category, constraint.members)[0];
          setErrorMessage(point, constraint.members[0], 'Field "' + fieldName + '" must be unique for all points. Check for duplications.');
        } else {
          var fieldNames = getFieldNames(category, constraint.members).join(', ');
          setErrorMessage(point, '', 'Field(s) "' + fieldNames + '" must be unique for all points. Check for duplications.');
        }
      }
    });

    return valid;
  }

  /**
   * Some categories are mutually exclusive with some others, i.e. the two cannot be both filled in at the same time.
   *
   * @param request
   * @param schema
   * @returns {boolean}
   */
  function validateMutualExclusions(request, schema) {
    var valid = true;

    schema.categories.concat(schema.datasources).forEach(function (category) {
      if (!category.excludes) {
        return;
      }

      category.excludes.forEach(function (exclude) {

        // Get the excluded category
        schema.categories.concat(schema.datasources).forEach(function (cat) {
          if (cat.id === exclude) {
            var excludedCategory = cat;

            // For each point, check that if one or more of the fields of this category and one or more of the fields of the excluded category are filled. If
            // so, say something like "Fields in the "Alarms" group cannot be used if fields in the "Commands" group have been specified.".
            request.points.forEach(function (point) {
              var emptyFields = getEmptyFields(point, category.fields);

              // If at least one of the fields of this category are filled, then we must check the excluded category.
              if (emptyFields.length !== category.fields.length) {
                emptyFields = getEmptyFields(point, excludedCategory.fields);

                if (emptyFields.length !== excludedCategory.fields.length) {
                  point.properties.valid = category.valid = excludedCategory.valid = valid = false;
                  setErrorMessage(point, '', 'Fields in the "' + category.name_en + '" group cannot be used if fields in the "' + excludedCategory.name_en +
                    '" group have been specified.');
                }
              }
            });
          }
        });
      });
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

      // HACK ALERT: treat auto-generated fields as "empty"
      if (field.id === 'tagname' || field.id === 'faultFamily' || field.id === 'faultMember' || field.id === 'pointDescription') {
        if (emptyFields.indexOf(field) === -1) {
          emptyFields.push(field);
        }
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
   * Set an error message on a single field of a point.
   *
   * @param point
   * @param propertyName
   * @param message
   */
  function setErrorMessage(point, propertyName, message) {
    var exists = false;

    point.errors.forEach(function (error) {
      if (error.property === propertyName) {
        exists = true;
        error.errors.push(message);
      }
    });

    if (!exists) {
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
        value = point.properties[propertyName];
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
      return field.id + '.' + (field.model ? field.model : 'value');
    } else {
      return field.id;
    }
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

    // If we have an options field, check that the value is in the list of options
    if (field.type === 'options' && field.options && field.options instanceof Array) {

      for (var key in field.options) {
        var option = field.options[key];
        option = typeof option === 'object' ? option.value : option;

        if (!isNaN(option) && !isNaN(value) && parseInt(option, 10) === value) {
          return true;
        } else if (option === value) {
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
