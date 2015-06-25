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
    getColumn : getColumn,
    getTagnameColumn: getTagnameColumn,
    getFaultStateColumn: getFaultStateColumn
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
      readOnly : !editable
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
      column.type = 'checkbox';
    }

    return column;
  }

  function getTagnameColumn() {
    return {
      title: 'Tagname',
      readOnly: true,
      data: 'properties.tagname',
      validator: function(value, callback) {
        var data = this.instance.getDataAtCol(this.col);
        var valid = ValidationService.validateUnique(value, data);
        if (!valid) {
          console.log('unique column validation failed');
          callback(false);
        }
        callback(true);
      }
    }
  }

  function getFaultStateColumn() {
    return {
      title: 'Fault State',
      readOnly: true,
      data: 'properties.faultState',
      validator: function(value, callback) {
        var data = this.instance.getDataAtCol(this.col);
        var valid = ValidationService.validateUnique(value, data);
        if (!valid) {
          console.log('unique column validation failed');
          callback(false);
        }
        callback(true);
      }
    }
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
    //column.strict = true;
    //column.allowInvalid = true;

    // Dropdown options given as list
    if (field.options instanceof Array) {
      column.source = field.options;
    }

    // Dropdown options given as URL
    else if (typeof field.options === 'string') {
      column.source = function (query, process) {

        // TODO refactor this into a service
        $http.get(BACKEND_BASE_URL + '/' + field.options, {cache: true}).then(function (response) {
          if (!response.data.hasOwnProperty('_embedded')) {
            return [];
          }

          // Relies on the fact that the property name inside the JSON response is the same
          // as the first part of the URL, before the first forward slash
          var returnPropertyName = field.options.split('/')[0];
          var items = response.data._embedded[returnPropertyName].map(function (item) {

            // For fields that are objects but have no 'model' attribute defined, assume that
            // the object has only a single property called 'value'.
            if (field.model == undefined && typeof item == 'object') {
              return item.value;
            } else {
              return item[field.model];
            }
          });

          process(items);
        });
      };
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


    //var CustomAutocompleteEditor = Handsontable.editors.AutocompleteEditor.prototype.extend();
    //
    //CustomAutocompleteEditor.prototype.beginEditing = function(initialValue, event) {
    //  Handsontable.editors.BaseEditor.prototype.beginEditing.call(this, initialValue, event);
    //  if (this.originalValue === undefined || this.originalValue[this.cellProperties.viewModel] === null) {
    //    this.setValue('');
    //  } else {
    //    this.setValue(this.originalValue);
    //  }
    //
    //};
    //
    //CustomAutocompleteEditor.prototype.getValue = function() {
    //  return this.customValue === undefined ? '' : this.customValue;
    //};
    //
    //CustomAutocompleteEditor.prototype.setValue = function(value) {
    //  if (this.cellProperties.viewModel && typeof value === 'object' && value[this.cellProperties.viewModel]) {
    //    this.TEXTAREA.value = value[this.cellProperties.viewModel];
    //    this.customValue = value;
    //  } else {
    //    this.TEXTAREA.value = value;
    //  }
    //};
    //
    //Handsontable.editors.registerEditor('CustomAutocompleteEditor', CustomAutocompleteEditor);
    //
    //
    //var CustomAutocompleteRenderer = function(instance, td, row, col, prop, value, cellProperties) {
    //  if (cellProperties.viewModel && value && typeof value === 'object' && value.hasOwnProperty(cellProperties.viewModel)) {
    //    td.innerHTML = value[cellProperties.viewModel] == null ? '' : value[cellProperties.viewModel];
    //  } else if (value !== undefined) {
    //    td.innerHTML = value;
    //  }
    //};
    //
    //Handsontable.CustomAutocompleteCell = {
    //  editor: CustomAutocompleteEditor,
    //  renderer: CustomAutocompleteRenderer,
    //  validator: Handsontable.AutocompleteValidator,
    //  dataType: 'customAutocomplete'
    //};
    //
    //Handsontable.cellTypes.customAutocomplete = Handsontable.CustomAutocompleteCell;





    column.type = 'autocomplete';
    //column.strict = true;
    //column.allowInvalid = true;

    //column.renderer = function autocompleteCellRenderer(instance, td, row, col, prop, value, cellProperties) {
    //  Handsontable.renderers.AutocompleteRenderer.apply(this, arguments);
    //
    //  var point = instance.getSourceDataAtRow(row);
    //  var val;
    //
    //  if (field.model) {
    //    val = point.properties[field.id][field.model];
    //  } else {
    //    val =  point.properties[field.id].value;
    //  }
    //
    //  td.innerHTML = val;
    //  return td;
    //};

    //column.editor =

    if (field.model) {
      column.data = 'properties.' + field.id + '.' + field.model;
    } else {
      column.data = 'properties.' + field.id + '.value';
    }

    //column.data = 'properties.' + field.id;
    //if (field.model) {
    //  column.viewModel = field.model;
    //} else {
    //  column.viewModel = 'value';
    //}

    column.source = function(query, process) {
      var instance = this.instance;
      var row = this.row;

      instance.addHook('beforeChange', function(change, source) {
        console.log('beforeChange()');
      });

      instance.addHook('afterChange', function(change, source) {
        if (source == 'loadData') {
          return;
        }

        // hacky hacky hack hack
        console.log('afterChange()');

        var row = change[0][0], prop = change[0][1], oldVal = change[0][2], newVal = change[0][3];

        var point = instance.getSourceDataAtRow(row);
        // get the outer object i.e. properties.location.value -> location
        prop = prop.split('.')[1];

        if (prop == 'location') {
          var parts = newVal.split('/');
          var buildingNumber  = parts[0];
          if (parts.length > 1) {
            parts = parts[1].split('-');
            var floor = parts[0];
            if (parts.length > 1) {
              var room = parts[1];
            }
          }

          var location = {
            buildingNumber: buildingNumber,
            floor: floor,
            room: room,
            value: newVal
          };

          point.properties.location = location;
        }
      });

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
            var point = instance.getSourceDataAtRow(row);
            params[props[1]] = point.properties[props[0]][props[1]];
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
          return [];
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
