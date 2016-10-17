'use strict';

/**
 * @ngdoc service
 * @name modesti.SchemaService
 * @description # SchemaService
 */
angular.module('modesti').service('SchemaService', SchemaService);

function SchemaService($q, $http) {

  // Public API
  var service = {
    getSchema: getSchema,
    getSchemas: getSchemas,
    queryFieldValues: queryFieldValues,
    evaluateConditional: evaluateConditional,
    //generateTagnames: generateTagnames,
    //generateFaultStates: generateFaultStates,
    //generateAlarmCategories: generateAlarmCategories
  };

  /**
   *
   * @param request
   * @returns {*}
   */
  function getSchema(request) {
    console.log('fetching schema');
    var q = $q.defer();

    var url = request._links.schema.href;

    $http.get(url).then(function (response) {
      var schema = response.data;
      console.log('fetched schema: ' + schema.id);

      // TODO: remove this domain-specific code into a SchemaPostProcessor inside a plugin
      var domains = ['TIM', 'CSAM', 'WinCC OA (CV)'];
      if (domains.indexOf(schema.id) !== -1) {

        // Prepend tagname and fault state fields
        schema.categories.concat(schema.datasources).forEach(function (category) {

          // Tagname is shown on each category except General and Alarms
          if (category.id !== 'alarms' && category.id !== 'alarmHelp') {
            category.fields.push(getTagnameField());
          }

          var constraint;

          // Tagnames must be unique.
          if (category.id === 'location') {
            constraint = getUniqueTagnameConstraint();

            if (category.constraints) {
              category.constraints.push(constraint);
            } else {
              category.constraints = [constraint];
            }
          }

          // Fault member, fault code and problem description are shown on 'alarms' and 'alarmHelp' categories
          if (category.id === 'alarms' || category.id === 'alarmHelp') {
            category.fields.push(getFaultFamilyField());
            category.fields.push(getFaultMemberField());
            category.fields.push(getProblemDescriptionField());
          }

          // Fault member, fault code and problem description must make a unique triplet.
          if (category.id === 'alarms') {
            constraint = getAlarmTripletConstraint();

            if (category.constraints) {
              category.constraints.push(constraint);
            } else {
              category.constraints = [constraint];
            }
          }
        });
      }

      q.resolve(schema);
    },

    function (error) {
      console.log('error fetching schema: ' + error.statusText);
      q.reject();
    });

    return q.promise;
  }

  /**
   *
   * @returns {*}
   */
  function getSchemas() {
    console.log('fetching schemas');
    var q = $q.defer();

    $http.get(BACKEND_BASE_URL + '/schemas').then(function (response) {
      var schemas = response.data._embedded.schemas;

      // Don't include the core schema
      schemas = schemas.filter(function (schema) {
        return schema.id !== 'core';
      });

      console.log('fetched ' + schemas.length + ' schemas');
      q.resolve(schemas);
    },

    function (error) {
      console.log('error fetching schemas: ' + error.statusText);
      q.reject();
    });

    return q.promise;
  }

  /**
   *
   * @param field
   * @param query
   * @param point
   * @returns {*}
   */
  function queryFieldValues(field, query, point) {
    console.log('querying values for field ' + field.id + ' with query string "' + query + '"');
    var q = $q.defer();

    // Don't make a call if the query is less than the minimum length
    if (field.minLength && query.length < field.minLength) {
      q.resolve([]);
      return q.promise;
    }

    // Figure out the query parameters we need to put in the URI.
    var params = {};

    // If no params are specified, then by default the query string will be mapped to a parameter
    // called 'query'.
    if (field.params === undefined) {
      params.query = query;
    }

    // Otherwise, an array of params will have been given.
    else if (field.params instanceof Array) {

      if (field.params.length === 1) {
        var param = field.params[0];

        if (point && point.properties[param]) {
          params[param] = point.properties[param];
        } else {
          params[param] = query;
        }
      } else if (field.params.length > 1) {

        // If we've been given a point object, then we can query stuff based on other properties
        // of that point. So we take all parameters into account.
        if (point) {
          field.params.forEach(function (param) {

            // Conventionally the first parameter will be called 'query'.
            if (param === 'query') {
              params.query = query;
            }

            // If the specified parameter name matches a property of the point, then we will use the
            // value of that property as the corresponding parameter value. Otherwise, it will be
            // mapped to the query string.
            if (point.properties[param]) {
              params[param] = point.properties[param];
            } else {
              params[param] = query;
            }

            // The parameter might be a sub-property of another property (i.e. contains a dot). In
            // that case, find the property of the point and add it as a search parameter. This
            // acts like a filter for a search, based on another property.
            // TODO: add "filter" parameter to schema instead of this?
            if (param.indexOf('.') > -1) {
              var parts = param.split('.');
              var prop = parts[0];
              var subProp = parts[1];

              if (point.properties[prop] && point.properties[prop].hasOwnProperty(subProp) && point.properties[prop][subProp]) {
                params[subProp] = point.properties[prop][subProp];
              } else {
                params[subProp] = '';
              }
            }
          });
        }

        // If we haven't been given a point, then we can't do point-contextual queries. In that
        // case, we ignore all params except the first one and bind the query string to it.
        else {
          params[field.params[0]] = query;
          params[field.params[1]] = '';
        }
      }
    }

    // Call the endpoint asynchronously and resolve the promise when we're done.
    $http.get(BACKEND_BASE_URL + '/' + field.url, {
      params: params,
      cache: true
    }).then(function (response) {
      var values = [];

      if (response.data.hasOwnProperty('_embedded')) {

        // Relies on the fact that the property name inside the JSON response is the same
        // as the first part of the URL, before the first forward slash
        var returnPropertyName = field.url.split('/')[0];
        values = response.data._embedded[returnPropertyName];
      }

      else if (response.data instanceof Array) {
        values = response.data;
      }

      console.log('found ' + values.length + ' values');
      q.resolve(values);
    },

    function (error) {
      console.log('error querying values: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  /**
   *
   * @param point
   * @param conditional
   * @param status
   * @returns {boolean}
   */
  function evaluateConditional(point, conditional, status) {
    // Simple boolean
    if (conditional === false || conditional === true) {
      return conditional;
    }

    var results = [];

    // Chained OR condition
    if (conditional.or) {
      conditional.or.forEach(function (subConditional) {
        results.push(evaluateConditional(point, subConditional, status));
      });

      return results.indexOf(true) > -1;
    }

    // Chained AND condition
    else if (conditional.and) {
      conditional.and.forEach(function (subConditional) {
        results.push(evaluateConditional(point, subConditional, status));
      });

      return results.reduce(function(a, b){ return (a === b) ? a : false; }) === true;
    }

    var statusResult, valueResult;

    // Conditional based on the status of the request.
    if (conditional.status) {
      if (conditional.status instanceof Array) {
        statusResult = conditional.status.indexOf(status) > -1;
      } else if (typeof conditional.status === 'string') {
        statusResult = status === conditional.status;
      }
    }

    // Conditional based on the value of another property of the point, used in conjunction with the status conditional
    if (conditional.condition) {
      valueResult = evaluateConditional(point, conditional.condition, status);
    }

    // Simple value conditional without status conditional
    if (conditional.field) {
      valueResult = evaluateValueCondition(point, conditional);
    }

    if (valueResult !== undefined && statusResult !== undefined) {
      return statusResult && valueResult;
    } else if (valueResult === undefined && statusResult !== undefined) {
      return statusResult;
    } else if (valueResult !== undefined && statusResult === undefined) {
      return valueResult;
    } else {
      return false;
    }
  }

  /**
   *
   * @param point
   * @param condition
   * @returns {boolean}
   */
  function evaluateValueCondition(point, condition) {
    var value = point.properties[condition.field];
    var result = false;

    if (condition.operation === 'equals' && value === condition.value) {
      result = true;
    } else if (condition.operation === 'notEquals' && value !== condition.value) {
      result = true;
    } else if (condition.operation === 'contains' && (value && value.toString().indexOf(condition.value) > -1)) {
      result = true;
    } else if (condition.operation === 'notNull' && (value !== null && value !== undefined && value !== '')) {
      result = true;
    } else if (condition.operation === 'isNull' && (value === null || value === undefined || value === '')) {
      result = true;
    } else if (condition.operation === 'in' && (condition.value.indexOf(value) > -1)) {
      result = true;
    }

    return result;
  }

  /**
   *
   * @returns {*}
   */
  function getTagnameField() {
    /*jshint camelcase: false */
    return {
      id: 'tagname',
      type: 'text',
      editable: false,
      unique: true,
      name: 'Tagname',
      help: 'Automatically generated TIM tagname. Interrogation marks (?) indicate that a column which makes up the tagname has not yet been filled.'
    };
  }

  /**
   *
   * @returns {*}
   */
  function getUniqueTagnameConstraint() {
    return {
      'type': 'unique',
      'members': [ 'tagname' ]
    };
  }

  /**
   *
   * @returns {*}
   */
  function getProblemDescriptionField() {
    /*jshint camelcase: false */
    return {
      'id': 'problemDescription',
      'type': 'text',
      'editable': false,
      'name': 'Problem Description',
      'help': 'The alarm description that will be displayed in LASER  (automatically generated).',
    };
  }

  /**
   *
   * @returns {*}
   */
  function getFaultFamilyField() {
    /*jshint camelcase: false */
    return {
      'id': 'faultFamily',
      'type': 'text',
      'editable': false,
      'name': 'Fault Family',
      'help': 'The LASER fault family (automatically generated). Interrogation marks (?) indicate that a column which makes up the tagname has not yet been filled.',
    };
  }

  /**
   *
   * @returns {*}
   */
  function getFaultMemberField() {
    /*jshint camelcase: false */
    return {
      'id': 'faultMember',
      'type': 'text',
      'editable': false,
      'maxLength': 30,
      'name': 'Fault Member',
      'help': 'The LASER fault member (automatically generated).',
    };
  }

  /**
   *
   * @returns {*}
   */
  function getAlarmTripletConstraint() {
    return {
      'type': 'unique',
      'members': [ 'faultFamily', 'faultMember', 'pointDescription' ]
    };
  }

  return service;
}
