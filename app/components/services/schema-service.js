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
    getSchemas: getSchemas
  };

  /**
   *
   * @param request
   * @param extraCategory
   * @returns {*}
   */
  function getSchema(request, extraCategory) {
    console.log('fetching schema');
    var q = $q.defer();

    var url = request._links.schema.href;

    if (extraCategory) {
      if (url.indexOf('?categories') > -1) {
        url += ',' + extraCategory;
      } else {
        url += '?categories=' + extraCategory;
      }
    }

    $http.get(url).then(function (response) {
      var schema = response.data;
      console.log('fetched schema: ' + schema.name);

      // Save the URL
      request._links.schema.href = url;

      // Save the new category
      if (extraCategory && request.categories.indexOf(extraCategory) == -1) {
        request.categories.push(extraCategory);
      }

      // Prepend tagname and fault state fields
      schema.categories.forEach(function (category) {

        // Tagname is shown on each category except General and Alarms
        if (category.id !== 'general' && category.id !== 'alarms' && category.id !== 'alarmHelp') {
          category.fields.unshift(getTagnameField());
        }
      });

      schema.datasources.forEach(function (datasource) {
        datasource.fields.unshift(getTagnameField());
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

  return service;
}
