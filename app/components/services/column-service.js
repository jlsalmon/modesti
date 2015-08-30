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
    getColumn: getColumn
  };

  /**
   *
   * @param field
   * @param editable
   * @returns {*}
   */
  function getColumn(field, editable) {
    var column = {
      data: 'properties.' + field.id,
      title: getColumnHeader(field),
      readOnly: !editable || field.editable === false
    };

    if (field.type == 'autocomplete') {
      column = getAutocompleteColumn(column, field);
    }

    if (field.type == 'options') {
      column = getDropdownColumn(column, field);
    }

    if (field.type == 'numeric') {
      column.type = 'numeric'
    }

    if (field.type == 'checkbox') {
      // Just use true/false dropdown until copy/paste issues are fixed.
      // See https://github.com/handsontable/handsontable/issues/2497
      field.options = ['true', 'false'];
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
    html += field.required ? '*' : '';
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
    column.editor = 'select2';

    column.select2Options = {
      data: {results: field.options.map(function (option) { return {id: option, text: option} })},
      dropdownAutoWidth: true
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
    column.editor = 'select2';

    if (field.model) {
      column.data = 'properties.' + field.id + '.' + field.model;
    } else {
      column.data = 'properties.' + field.id + '.value';
    }

    //column.renderer = getRenderer(column, field);

    column.select2Options = {
      minimumInputLength: field.minLength || 0,
      maximumInputLength: 200,

      query: getQueryFunction(column, field),

      //// Formats the items shown in the dropdown list
      //formatResult: function (option) {
      //  return option.text;
      //},
      //
      formatSelection: function (option) {
        return option;
      },

      initSelection: function(element, callback) {
        callback(element.context.value);
      },

      dropdownAutoWidth: true,
      width: 'resolve'
    };

    return column;
  }

  /**
   *
   * @param column
   * @param field
   * @returns {Function}
   */
  function getQueryFunction(column, field) {
    return function (query) {
      getOptions(field, query.term, query.callback).then(function (results) {
        query.callback({results: results, text: 'text'});
      });
    }
  }

  /**
   *
   * @param field
   * @param query
   * @returns {*}
   */
  function getOptions(field, query) {

    var params = {};
    if (field.params == undefined) {
      // By default, searches are done via parameter called 'query'
      params.query = query;
    } else {
      field.params.forEach(function (param) {

        // TODO reimplement this

        //// The parameter might be a sub-property of another property (i.e. contains a dot). In
        //// that case, find the property of the point and add it as a search parameter. This
        //// acts like a filter for a search, based on another property.
        //// TODO: add "filter" parameter to schema instead of this?
        //if (param.indexOf('.') > -1) {
        //  var props = param.split('.');
        //  if (point.properties[props[0]] && point.properties[props[0]].hasOwnProperty(props[1])) {
        //    params[props[1]] = point.properties[props[0]][props[1]];
        //  } else {
        //    params[props[1]] = '';
        //  }
        //}
        //else {
         params[param] = query;
        //}

      });
    }


    return $http.get(BACKEND_BASE_URL + '/' + field.url, {
      params: params,
      cache: true
    }).then(function (response) {
      var results = [];

      if (response.data.hasOwnProperty('_embedded')) {

        // Relies on the fact that the property name inside the JSON response is the same
        // as the first part of the URL, before the first forward slash
        var returnPropertyName = field.url.split('/')[0];
        results = response.data._embedded[returnPropertyName].map(function (option) {
          return {id: option[getModelAttribute(field)], text: option[getModelAttribute(field)]};
        });
      }

      return results;
    });
  }

  /**
   *
   * @param field
   * @returns {*}
   */
  function getModelAttribute(field) {
    // For fields that are objects but have no 'model' attribute defined, assume that
    // the object has only a single property called 'value'.
    return field.model ? field.model : 'value';
  }

  return service;
}
