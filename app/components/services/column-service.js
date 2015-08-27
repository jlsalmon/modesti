'use strict';

/**
 * @ngdoc service
 * @name modesti.ColumnService
 * @description # ColumnService Service in the modesti.
 */
angular.module('modesti').service('ColumnService', ColumnService);

function ColumnService($http, $localStorage, $translate) {
  var self = this;

  if (!$localStorage.optionsCache) {
    $localStorage.optionsCache = {};
  }

  self.optionsCache = $localStorage.optionsCache;


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

  //
  //var opts = [{id: 1, text: 'Boolean'}, {id: 2, text: 'Float'}, {id: 3, text: 'Integer'}, {
  //  id: 4,
  //  text: 'Double'
  //}, {id: 5, text: 'String'}];

  /**
   *
   * @param column
   * @param field
   * @returns {*}
   */
  function getDropdownColumn(column, field) {
    column.editor = 'select2';

    column.renderer = function (instance, td, row, col, prop, value, cellProperties) {
      if (typeof prop !== 'string' || prop.split('.')[1] !== field.id || !value) {
        return;
      }

      value = getValue(value, field.options, getModelAttribute(field));
      td.innerHTML = value;
    };

    column.select2Options = {
      data: {results: field.options, text: getModelAttribute(field)},
      dropdownAutoWidth: true,
      width: 'resolve',

      formatResult: function (option) {
        return option[getModelAttribute(field)];
      },

      formatSelection: function (option) {
        return option[getModelAttribute(field)];
      }
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
    column.renderer = getRenderer(column, field);
    column.select2Options = {
      minimumInputLength: field.minLength || 0,

      query: getQueryFunction(column, field),

      formatResult: function (option) {
        return option[getModelAttribute(field)];
      },

      formatSelection: function (option) {
        return option[getModelAttribute(field)];
      },

      //id: getModelAttribute(field),
      text: getModelAttribute(field),
      dropdownAutoWidth: true,
      width: 'resolve'
    };

    return column;
  }


  function getQueryFunction(column, field) {
    return function (query) {
      getOptions(field, query.term, query.callback).then(function (results) {
        query.callback({results: results, text: getModelAttribute(field)});
      });
    }
  }

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
        //  params[param] = query;
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
        results = response.data._embedded[returnPropertyName];
      }

      //self.optionsCache[field.id] = results;
      return results;
    });
  }

  function getRenderer(column, field) {
    return function (instance, td, row, col, prop, value, cellProperties) {
      if (typeof prop !== 'string' || prop.split('.')[1] !== field.id || !value) {
        return;
      }

      var cachedValues = self.optionsCache[field.id];

      if (cachedValues) {
        cachedValues.forEach(function (cachedValue) {
          if (cachedValue.id === value || cachedValue.id === parseInt(value)) {
            value = cachedValue;
          }
        });
      }

      if (self.optionsCache[field.id]) {
        self.optionsCache[field.id] = [{id: value, value: value}];
      } else {
        self.optionsCache[field.id].push(value);
      }

      td.innerHTML = value;
    }
  }

  function getValue(key, options, model) {
    for (var index = 0; index < options.length; index++) {
      if (key === options[index].id || parseInt(key) === options[index].id) {
        return options[index][model];
      }
    }
  }

  function getModelAttribute(field) {
    // For fields that are objects but have no 'model' attribute defined, assume that
    // the object has only a single property called 'value'.
    return field.model ? field.model : 'value';
  }

  //column.type = 'autocomplete';
  ////column.trimDropdown = false;
  //column.strict = false;
  //column.allowInvalid = true;
  //column.filter = false;
  //
  //if (field.model) {
  //  column.data = 'properties.' + field.id + '.' + field.model;
  //} else {
  //  column.data = 'properties.' + field.id + '.value';
  //}
  //
  //
  //column.source = function (query, process) {
  //  var instance = this.instance;
  //  var row = this.row;
  //  var point = instance.getSourceDataAtRow(row);
  //
  //  // Don't make a call if the query is less than the minimum length
  //  if (field.minLength && query.length < field.minLength) {
  //    return;
  //  }
  //
  //  var params = {};
  //  if (field.params == undefined) {
  //    // By default, searches are done via parameter called 'query'
  //    params.query = query;
  //  } else {
  //    for (var i in field.params) {
  //      var param = field.params[i];
  //
  //      // The parameter might be a sub-property of another property (i.e. contains a dot). In
  //      // that case, find the property of the point and add it as a search parameter. This
  //      // acts like a filter for a search, based on another property.
  //      // TODO: add "filter" parameter to schema instead of this?
  //      if (param.indexOf('.') > -1) {
  //        var props = param.split('.');
  //        if (point.properties[props[0]] && point.properties[props[0]].hasOwnProperty(props[1])) {
  //          params[props[1]] = point.properties[props[0]][props[1]];
  //        } else {
  //          params[props[1]] = '';
  //        }
  //      }
  //      else {
  //        params[param] = query;
  //      }
  //    }
  //  }
  //
  //  // TODO refactor this into a service
  //  $http.get(BACKEND_BASE_URL + '/' + field.url, {
  //    params: params,
  //    cache: true
  //  }).then(function (response) {
  //    if (!response.data.hasOwnProperty('_embedded')) {
  //
  //      // If no option was found, mark this field as invalid
  //      if (!point.invalidFields) {
  //        point.invalidFields = [field.id];
  //      } else if (point.invalidFields.indexOf(field.id) == -1) {
  //        point.invalidFields.push(field.id);
  //      }
  //
  //      return [];
  //    } else {
  //      point.invalidFields = [];
  //    }
  //
  //    // Relies on the fact that the property name inside the JSON response is the same
  //    // as the first part of the URL, before the first forward slash
  //    var returnPropertyName = field.url.split('/')[0];
  //    var items = response.data._embedded[returnPropertyName].map(function (item) {
  //
  //      //var point = instance.getSourceDataAtRow(row);
  //      //point.properties[field.id] = item;
  //
  //      // For fields that are objects but have no 'model' attribute defined, assume that
  //      // the object has only a single property called 'value'.
  //      if (field.model == undefined && typeof item == 'object') {
  //        return item.value;
  //      } else {
  //        return item[field.model];
  //      }
  //
  //      //delete item._links;
  //      //return item;
  //    });
  //
  //    process(items);
  //  });
  //};

  //  return column;
  //}


  return service;
}
