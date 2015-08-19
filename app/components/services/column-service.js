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
      data : 'properties.' + field.id,
      title : getColumnHeader(field),
      readOnly : !editable || field.editable === false
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
    column.editor = 'select';
    //column.strict = false;
    //column.allowInvalid = true;

    // Dropdown options given as list
    if (field.options instanceof Array) {
      column.selectOptions = field.options;
    }

    // Dropdown options given as URL
    //else if (typeof field.options === 'string') {
    //  column.source = function (query, process) {
    //
    //    // TODO refactor this into a service
    //    $http.get(BACKEND_BASE_URL + '/' + field.options, {cache: true}).then(function (response) {
    //      if (!response.data.hasOwnProperty('_embedded')) {
    //        return [];
    //      }
    //
    //      // Relies on the fact that the property name inside the JSON response is the same
    //      // as the first part of the URL, before the first forward slash
    //      var returnPropertyName = field.options.split('/')[0];
    //      var items = response.data._embedded[returnPropertyName].map(function (item) {
    //
    //        // For fields that are objects but have no 'model' attribute defined, assume that
    //        // the object has only a single property called 'value'.
    //        if (field.model == undefined && typeof item == 'object') {
    //          return item.value;
    //        } else {
    //          return item[field.model];
    //        }
    //      });
    //
    //      process(items);
    //    });
    //  };
    //}

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
    //column.trimDropdown = false;
    column.strict = false;
    column.allowInvalid = true;
    column.filter = false;

    if (field.model) {
      column.data = 'properties.' + field.id + '.' + field.model;
    } else {
      column.data = 'properties.' + field.id + '.value';
    }


    column.source = function(query, process) {
      var instance = this.instance;
      var row = this.row;
      var point = instance.getSourceDataAtRow(row);

      // Don't make a call if the query is less than the minimum length
      if (field.minLength && query.length < field.minLength) {
        return;
      }

      var params = {};
      if (field.params == undefined) {
        // By default, searches are done via parameter called 'query'
        params.query = query;
      } else {
        for (var i in field.params) {
          var param = field.params[i];

          // The parameter might be a sub-property of another property (i.e. contains a dot). In
          // that case, find the property of the point and add it as a search parameter. This
          // acts like a filter for a search, based on another property.
          // TODO: add "filter" parameter to schema instead of this?
          if (param.indexOf('.') > -1) {
            var props = param.split('.');
            if (point.properties[props[0]] && point.properties[props[0]].hasOwnProperty(props[1])) {
              params[props[1]] = point.properties[props[0]][props[1]];
            } else {
              params[props[1]] = '';
            }
          }
          else {
            params[param] = query;
          }
        }
      }

      // TODO refactor this into a service
      $http.get(BACKEND_BASE_URL + '/' + field.url, {
        params : params,
        cache: true
      }).then(function(response) {
        if (!response.data.hasOwnProperty('_embedded')) {

          // If no option was found, mark this field as invalid
          if (!point.invalidFields) {
            point.invalidFields = [field.id];
          } else if (point.invalidFields.indexOf(field.id) == -1) {
            point.invalidFields.push(field.id);
          }

          return [];
        } else {
          point.invalidFields = [];
        }

        // Relies on the fact that the property name inside the JSON response is the same
        // as the first part of the URL, before the first forward slash
        var returnPropertyName = field.url.split('/')[0];
        var items = response.data._embedded[returnPropertyName].map(function(item) {

          //var point = instance.getSourceDataAtRow(row);
          //point.properties[field.id] = item;

          // For fields that are objects but have no 'model' attribute defined, assume that
          // the object has only a single property called 'value'.
          if (field.model == undefined && typeof item == 'object') {
            return item.value;
          } else {
            return item[field.model];
          }

          //delete item._links;
          //return item;
        });

        process(items);
      });
    };

    return column;
  }

  return service;
}
