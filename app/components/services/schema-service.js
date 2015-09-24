'use strict';

/**
 * @ngdoc service
 * @name modesti.SchemaService
 * @description # SchemaService Service in the modesti.
 */
angular.module('modesti').service('SchemaService', SchemaService);

function SchemaService($q, $http) {

  // Public API
  var service = {
    getSchema: getSchema,
    getSchemas: getSchemas,
    evaluateConditional: evaluateConditional,
    generateTagnames: generateTagnames,
    generateFaultStates: generateFaultStates,
    generateAlarmCategories: generateAlarmCategories
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
      console.log('fetched schema: ' + schema.name);

      // Prepend tagname and fault state fields
      schema.categories.concat(schema.datasources).forEach(function (category) {

        // Tagname is shown on each category except General and Alarms
        if (category.id !== 'general' && category.id !== 'alarms' && category.id !== 'alarmHelp') {
          category.fields.push(getTagnameField());
        }

        // Fault member, fault code and problem description are shown on 'alarms' and 'alarmHelp' categories
        if (category.id === 'alarms' || category.id === 'alarmHelp') {
          category.fields.push(getFaultFamilyField());
          category.fields.push(getFaultMemberField());
          category.fields.push(getProblemDescriptionField());
        }

        // Fault member, fault code and problem description must make a unique triplet.
        if (category.id === 'alarms') {
          category.constraints.push(getAlarmTripletConstraint());
        }
      });

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
      console.log('error fetching schemas: ' + error);
      q.reject();
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

    // Chained OR condition
    if (conditional.or) {
      var results = [];

      conditional.or.forEach(function (subConditional) {
        results.push(evaluateConditional(point, subConditional, status));
      });

      return results.indexOf(true) > -1;
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
      valueResult = evaluateValueCondition(point, conditional.condition);
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
    } else if (condition.operation === 'contains' && value && value.toString().indexOf(condition.value) > -1) {
      result = true;
    } else if (condition.operation === 'notNull' && value !== null && value !== undefined && value !== '') {
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
      name_en: 'Tagname',
      name_fr: 'Tagname',
      help_en: '',
      help_fr: ''
    };
  }

  /**
   *
   * @returns {*}
   */
  function getProblemDescriptionField() {
    /*jshint camelcase: false */
    return {
      'id': 'pointDescription',
      'type': 'text',
      'editable': false,
      'name_en': 'Problem Description',
      'name_fr': 'Problem Description',
      'help_en': '',
      'help_fr': ''
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
      'name_en': 'Fault Family',
      'name_fr': 'Fault Family',
      'help_en': '',
      'help_fr': ''
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
      'name_en': 'Fault Member',
      'name_fr': 'Fault Member',
      'help_en': '',
      'help_fr': ''
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

  /**
   * Tagname format: system_code|subsystem_code|’.’|functionality_code|’.’|equipment_identifier|’:’|point_attribute
   */
  function generateTagnames(request) {

    request.points.forEach(function (point) {

      if (!point.properties.subsystem) {
        return;
      }

      (function (point) {
        $http.get(BACKEND_BASE_URL + '/subsystems/search/find', {
          params: {query: point.properties.subsystem.value},
          cache: true
        }).then(function (response) {

          if (!response.data.hasOwnProperty('_embedded')) {
            return;
          }

          var subsystemCode;

          if (response.data._embedded.subsystems.length === 1) {
            var subsystem = response.data._embedded.subsystems[0];
            subsystemCode = subsystem.systemCode + subsystem.subsystemCode;
          } else {
            subsystemCode = '?';
          }

          var site = (point.properties.functionality && point.properties.functionality.value ? point.properties.functionality.value : '?');
          var equipmentIdentifier = getEquipmentIdentifier(point);
          equipmentIdentifier = equipmentIdentifier || '?';
          var attribute = (point.properties.pointAttribute ? point.properties.pointAttribute : '?');

          if (subsystemCode === '?' && site === '?' && equipmentIdentifier === '?' && attribute === '?') {
            point.properties.tagname = '';
          } else {
            point.properties.tagname = subsystemCode + '.' + site + '.' + equipmentIdentifier + ':' + attribute;
          }
        });
      })(point);
    });
  }

  /**
   * Fault state format: system_name|’_’|subsystem_name|’_’|general_functionality|’:’|equipment_identifier|’:’|point_description
   */
  function generateFaultStates(request) {
    request.points.forEach(function (point) {

      if (point.properties.priorityCode === undefined || point.properties.priorityCode === null || point.properties.priorityCode === '') {
        point.properties.faultFamily = '';
        point.properties.faultMember = '';
        return;
      }

      if (!point.properties.subsystem) {
        return;
      }

      point.properties.faultMember = getEquipmentIdentifier(point);

      (function (point) {
        $http.get(BACKEND_BASE_URL + '/subsystems/search/find', {
          params: {query: point.properties.subsystem.value},
          cache: true
        }).then(function (response) {

          if (!response.data.hasOwnProperty('_embedded')) {
            return;
          }

          var systemName = '?', subsystemName = '?';

          if (response.data._embedded.subsystems.length === 1) {
            var subsystem = response.data._embedded.subsystems[0];
            systemName = subsystem.system;
            subsystemName = subsystem.subsystem;
          }

          if (point.properties.functionality && point.properties.functionality.value) {
            $http.get(BACKEND_BASE_URL + '/functionalities/search/find', {
              params: {query: point.properties.functionality.value},
              cache: true
            }).then(function (response) {
              if (!response.data.hasOwnProperty('_embedded')) {
                return;
              }

              var func = '?';
              if (response.data._embedded.functionalities.length === 1) {
                var functionality = response.data._embedded.functionalities[0];
                func = functionality.generalFunctionality;
              }

              if (systemName === '?' && subsystemName === '?' && func === '?') {
                point.properties.faultFamily = '';
              } else {
                point.properties.faultFamily = systemName + '_' + subsystemName + '_' + func;
              }
            });
          }
        });
      })(point);
    });
  }

  /**
   *
   * @param point
   * @returns {*}
   */
  function getEquipmentIdentifier(point) {
    var equipmentIdentifier, gmaoCode;

    if (point.properties.gmaoCode && point.properties.gmaoCode.value) {
      gmaoCode = point.properties.gmaoCode.value;
    } else if (point.properties.csamCsename && point.properties.csamCsename.value) {
      gmaoCode = point.properties.csamCsename.value;
    }

    var otherEquipCode;

    if (point.properties.otherEquipCode) {
      otherEquipCode = point.properties.otherEquipCode;
    } else if(point.properties.csamDetector && point.properties.csamDetector.value) {
      otherEquipCode = point.properties.csamDetector.value;
    }

    if (gmaoCode && otherEquipCode) {
      if (gmaoCode === otherEquipCode) {
        equipmentIdentifier = gmaoCode;
      } else {
        equipmentIdentifier = gmaoCode + '_' + otherEquipCode;
      }
    } else if (gmaoCode && !otherEquipCode) {
      equipmentIdentifier = gmaoCode;
    } else if (!gmaoCode && otherEquipCode) {
      equipmentIdentifier = otherEquipCode;
    } else {
      equipmentIdentifier = '';
    }

    return equipmentIdentifier;
  }

  /**
   * Alarm category format: 'CERN.SRVS.'|FUNC_GEN|'.'|TES_SYSTEM_NAME
   */
  function generateAlarmCategories(request) {
    request.points.forEach(function (point) {

      if (point.properties.priorityCode || point.properties.alarmValue) {

        if (!point.properties.subsystem) {
          return;
        }

        (function (point) {
          $http.get(BACKEND_BASE_URL + '/subsystems/search/find', {
            params: {query: point.properties.subsystem.value},
            cache: true
          }).then(function (response) {

            if (!response.data.hasOwnProperty('_embedded')) {
              return;
            }

            var systemName;

            if (response.data._embedded.subsystems.length === 1) {
              var subsystem = response.data._embedded.subsystems[0];
              systemName = subsystem.system;
            }

            if (point.properties.functionality && point.properties.functionality.value) {
              $http.get(BACKEND_BASE_URL + '/functionalities/search/find', {
                params: {query: point.properties.functionality.value},
                cache: true
              }).then(function (response) {
                if (!response.data.hasOwnProperty('_embedded')) {
                  return;
                }

                var generalFunctionality;
                if (response.data._embedded.functionalities.length === 1) {
                  var functionality = response.data._embedded.functionalities[0];
                  generalFunctionality = functionality.generalFunctionality;
                }

                if (systemName && generalFunctionality) {
                  point.properties.alarmCategory = {value: 'CERN.SRVS.' + generalFunctionality + '.' + systemName};
                }

                // TODO remove this domain-specific code...
                if (request.domain === 'CSAM' && point.properties.priorityCode && point.properties.priorityCode === 3) {
                  point.properties.alarmCategory = 'POMPIER';
                }
              });
            }
          });
        })(point);
      }
    });
  }

  return service;
}
