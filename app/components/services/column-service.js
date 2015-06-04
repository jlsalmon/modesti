'use strict';

/**
 * @ngdoc service
 * @name modesti.ColumnService
 * @description # ColumnService Service in the modesti.
 */
angular.module('modesti').service('ColumnService', ColumnService);

function ColumnService($http) {
  var self = this;

  // Public API
  var service = {
    getColumn : getColumn
  };

  /**
   *
   */
  function getColumn(field, editable) {
    var column = {
      data : 'properties.' + field.id,
      title : field.name,
      readOnly : !editable
    };

    if (field.type == 'typeahead') {
      column = getAutocompleteColumn(column, field);
    }

    if (field.type == 'select') {
      column = getDropdownColumn(column, field);
    }

    return column;
  }

  /**
   *
   */
  function getDropdownColumn(column, field) {
    column.type = 'dropdown';
    column.strict = true;
    column.allowInvalid = true;

    column.source = function(query, process) {

      // TODO refactor this into a service
      $http.get(field.options).then(function(response) {
        if (!response.data.hasOwnProperty('_embedded')) {
          return [];
        }

        var items = response.data._embedded[field.returnPropertyName].map(function(item) {
          return item[field.model];
        });

        process(items);
      });
    };

    return column;
  }

  /**
   *
   */
  function getAutocompleteColumn(column, field) {
    column.type = 'autocomplete';
    //column.strict = true;
    //column.allowInvalid = true;

    if (field.model) {
      column.data = 'properties.' + field.id + '.' + field.model;
    }

    column.source = function(query, process) {

      if (field.minLength && query.length < field.minLength) {
        return;
      }

      var params = {};
      for ( var i in field.params) {
        params[field.params[i]] = query;
      }

      // TODO refactor this into a service
      $http.get(field.url, {
        params : params
      }).then(function(response) {
        if (!response.data.hasOwnProperty('_embedded')) {
          return [];
        }

        var items = response.data._embedded[field.returnPropertyName].map(function(item) {
          return item[field.model];
        });

        process(items);
      });
    }

    return column;
  }

  return service;
}
