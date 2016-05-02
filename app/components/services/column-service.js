'use strict';

/**
 * @ngdoc service
 * @name modesti.ColumnService
 * @description # ColumnService
 */
angular.module('modesti').service('ColumnService', ColumnService);

function ColumnService($translate, SchemaService) {

  // Public API
  var service = {
    getColumn: getColumn
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

    if (field.type === 'text') {
      column = getTextColumn(column, field);
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
    html += 'data-content="' + field.help + '">';
    html += field.name;
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
  function getTextColumn(column, field) {
    if (field.url) {
      column.editor = 'select2';

      column.select2Options = getDefaultSelect2Options(column, field);

      // By default, text fields with URLs are not strict, as the queried
      // values are just suggestions
      if (field.strict !== true) {
        column.select2Options.createSearchChoice = function(term, data) {
          if ( $(data).filter( function() {
              return this.text.localeCompare(term)===0;
            }).length===0) {
            return {id:term, text:term};
          }
        };
      }
    }

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

    column.select2Options = getDefaultSelect2Options(column, field);

    return column;
  }

  /**
   *
   * @param column
   * @param field
   * @returns {*}
   */
  function getDropdownColumn(column, field) {
    column.editor = 'select2';

    var options;

    if (field.options) {
      var options = field.options.map(function (option) {
        if (typeof option === 'object') {
          if (option.description !== null && option.description !== undefined && option.description !== '') {
            return {id: option.value, text: option.value + ': ' + option.description};
          } else {
            return {id: option.value, text: option.value};
          }
        }

        else if (typeof (option === 'string')) {
          return {id: option, text: option};
        }
      });
    } else {
      options = {};
    }

    column.select2Options = {
      data: {results: options},
      dropdownAutoWidth: true
    };

    return column;
  }

  /**
   *
   * @param column
   * @param field
   * @returns {}
   */
  function getDefaultSelect2Options(column, field) {
    return {
      minimumInputLength: field.minLength || 0,
      maximumInputLength: 200,

      query: getQueryFunction(column, field),

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
      var point = hot.getSourceDataAtRow(row);

      SchemaService.queryFieldValues(field, query.term, point).then(function (values) {

        // Re-map the values in a format that the select2 editor likes
        var results = values.map(function (value) {
          if (typeof value === 'string') {
            return {id: value, text: value};
          } else {
            return {id: value[getModelAttribute(field)], text: value[getModelAttribute(field)]};
          }
        });

        // Invoke the editor callback so it can populate itself
        query.callback({results: results, text: 'text'});
      });
    };
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
