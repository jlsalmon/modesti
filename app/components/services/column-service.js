'use strict';

/**
 * @ngdoc service
 * @name modesti.ColumnService
 * @description # ColumnService Service in the modesti.
 */
angular.module('modesti').service('ColumnService', ColumnService);

function ColumnService($http, $translate) {
  var self = this;

  // Public API
  var service = {
    getColumn : getColumn
  };

  /**
   *
   * @param field
   * @param editable
   * @returns {{data: string, title: string, readOnly: boolean}}
   */
  function getColumn(field, editable) {
    var column = {
      data : 'properties.' + field.id,
      title : getColumnHeader(field),
      readOnly : !editable
    };

    if (field.type == 'autocomplete') {
      column = getAutocompleteColumn(column, field);
    }

    if (field.type == 'options') {
      column = getDropdownColumn(column, field);
    }

    return column;
  }

  /**
   *
   * @param field
   * @returns {string}
   */
  function getColumnHeader(field) {
    var html = '<span class="help-text" data-container="body" data-toggle="popover" data-placement="bottom" ';
    html += 'data-content="' + ($translate.use() == 'en' ? field.help_en : field.help_fr) + '">';
    html += $translate.use() == 'en' ? field.name_en : field.name_fr;
    html += '</span>';
    return html;
  }

  /**
   *
   * @param column
   * @param field
   * @returns {*}
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
   * @param column
   * @param field
   * @returns {*}
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

      if (this.prop == 'properties.location.buildingName') {
        var point = this.instance.getSourceDataAtRow(this.row);
        params['']
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
    };

    return column;
  }

  return service;
}
