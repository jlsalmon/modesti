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
    getSchema: getSchema
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
      for (var i = 0, len = schema.categories.length; i < len; i++) {
        var category = schema.categories[i];

        // Tagname is shown on each category except General and Alarms
        if (category.name != 'General' && category.name != 'Alarms' && category.name != 'Alarm Help') {
          category.fields.unshift(getTagnameField());
        }
      }

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
