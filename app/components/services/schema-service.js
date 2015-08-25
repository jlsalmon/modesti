'use strict';

/**
 * @ngdoc service
 * @name modesti.SchemaService
 * @description # SchemaService Service in the modesti.
 */
angular.module('modesti').service('SchemaService', SchemaService);

function SchemaService($q, $http) {
  var self = this;

  // Public API
  var service = {
    getSchema: getSchema,
    getSchemas: getSchemas,
    generateTagnames: generateTagnames,
    generateFaultStates: generateFaultStates
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
          category.fields.unshift(getTagnameField());
        }
      });

      q.resolve(schema);
    },

    function (error) {
      console.log('error fetching schema: ' + error);
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
   * @returns {*}
   */
  function getTagnameField() {
    return {
      id: 'tagname',
      type: 'text',
      editable: false,
      unique: true,
      name_en: "Tagname",
      name_fr: "Tagname",
      help_en: "",
      help_fr: ""
    }
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

          if (response.data._embedded.subsystems.length == 1) {
            var subsystem = response.data._embedded.subsystems[0];
            subsystemCode = subsystem.systemCode + subsystem.subsystemCode;
          } else {
            subsystemCode = '?';
          }

          var site = (point.properties.functionality && point.properties.functionality.value ? point.properties.functionality.value : '?');
          var equipmentIdentifier = getEquipmentIdentifier(point);
          equipmentIdentifier = equipmentIdentifier ? '?' : equipmentIdentifier;
          var attribute = (point.properties.pointAttribute ? point.properties.pointAttribute : '?');

          if (subsystemCode == '?' && site == '?' && equipmentIdentifier == '?' && attribute == '?') {
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

      point.properties.faultMember = getEquipmentIdentifier(point);

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

          var systemName = '?', subsystemName = '?';

          if (response.data._embedded.subsystems.length == 1) {
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
              if (response.data._embedded.functionalities.length == 1) {
                var functionality = response.data._embedded.functionalities[0];
                func = functionality.generalFunctionality;
              }

              if (systemName == '?' && subsystemName == '?' && func == '?') {
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
      gmaoCode = point.properties.gmaoCode.value
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

  return service;
}
