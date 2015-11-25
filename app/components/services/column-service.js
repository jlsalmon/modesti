'use strict';

/**
 * @ngdoc service
 * @name modesti.ColumnService
 * @description # ColumnService
 */
angular.module('modesti').service('ColumnService', ColumnService);

function ColumnService($http, $translate) {

  // Public API
  var service = {
    getColumn: getColumn,
    getOptions: getOptions
  };

  /**
   *
   * @param field
   * @param editable
   * @param authorised
   * @param status
   * @returns {*}
   */
  function getColumn(field, editable, authorised, status) {
    var column = {
      data: 'properties.' + field.id,
      title: getColumnHeader(field)
    };

    if (authorised) {
      editable = true;

      // Editable given as simple boolean
      if (field.editable === true || field.editable === false) {
        editable = field.editable;
      }

      // Editable given as condition object
      else if (field.editable !== null && typeof field.editable === 'object') {
        editable = !!(field.editable.status && status === field.editable.status);
      }

      column.readOnly = !editable;
    } else {
      column.readOnly = true;
    }



    if (field.type === 'autocomplete') {
      column = getAutocompleteColumn(column, field);
    }

    if (field.type === 'options') {
      column = getDropdownColumn(column, field);
    }

    if (field.type === 'numeric') {
      column.type = 'numeric';
    }

    if (field.type === 'checkbox') {
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
    /*jshint camelcase: false */
    html += 'data-content="' + ($translate.use() === 'en' ? field.help_en : field.help_fr) + '">';
    html += $translate.use() === 'en' ? field.name_en : field.name_fr;
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
      data: {results: field.options.map(function (option) { return {id: option, text: option}; })},
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

      nextSearchTerm: function(selectedObject) {
        return selectedObject;
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
      var hot = query.element.context.instance;
      var row = query.element.context.row;
      var term = query.term;

      getOptions(field, hot, row, term).then(function (results) {
        query.callback({results: results, text: 'text'});
      });
    };
  }

  /**
   *
   * @param field
   * @param hot
   * @param row
   * @param query
   * @returns {Promise.<T>}
   */
  function getOptions(field, hot, row, query) {

    var params = {};
    if (field.params === undefined) {
      // By default, searches are done via parameter called 'query'
      params.query = query;
    } else {
      field.params.forEach(function (param) {

        // The parameter might be a sub-property of another property (i.e. contains a dot). In
        // that case, find the property of the point and add it as a search parameter. This
        // acts like a filter for a search, based on another property.
        // TODO: add "filter" parameter to schema instead of this?

        if (param === 'query') {
          params.query = query;
        } else {
          var point = hot.getSourceDataAtRow(row);

          if (param.indexOf('.') > -1) {
            var parts = param.split('.');
            var prop = parts[0];
            var subProp = parts[1];

            if (point.properties[prop] && point.properties[prop].hasOwnProperty(subProp) && point.properties[prop][subProp]) {
              params[subProp] = point.properties[prop][subProp];
            } else {
              params[subProp] = '';
            }
          } else {
            if (point.properties[param]) {
              params[param] = point.properties[param];
            } else {
              params[param] = query;
            }
          }
        }
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
