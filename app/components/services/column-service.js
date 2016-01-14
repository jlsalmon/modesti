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
      var point = hot.getSourceDataAtRow(row);

      SchemaService.queryFieldValues(field, query.term, point).then(function (values) {

        // Re-map the values in a format that the select2 editor likes
        var results = values.map(function (value) {
          return {id: value[getModelAttribute(field)], text: value[getModelAttribute(field)]};
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
