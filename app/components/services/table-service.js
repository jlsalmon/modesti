'use strict';

/**
 * @ngdoc service
 * @name modesti.TableService
 * @description # TableService
 */
angular.module('modesti').service('TableService', TableService);

function TableService($rootScope, SchemaService, TaskService, Utils) {

  // Public API
  var service = {
    getColumns: getColumns,
    getColumn: getColumn,
    hasCheckboxColumn: hasCheckboxColumn,
    hasCommentColumn: hasCommentColumn,
  };

  function getColumns(request, schema, fields) {
    var columns = [];

    var task = TaskService.getCurrentTask();

    // Append "select-all" checkbox field.
    if (hasCheckboxColumn(request)) {
      columns.push(getCheckboxColumn(request, schema));
    }

    if (hasCommentColumn(request)) {
      columns.push(getCommentColumn(request, schema));
    }

    fields.forEach(function (field) {

      var authorised = false;
      if (TaskService.isCurrentUserAuthorised(task) && TaskService.isCurrentUserAssigned(task)) {
        authorised = true;
      }

      var editable;

      // Build the right type of column based on the schema
      var column = getColumn(field, editable, authorised, request.status);
      column.renderer = getRenderer(request, schema);
      columns.push(column);
    });

    return columns;
  }

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
      options = field.options.map(function (option) {
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

  /**
   * The "select-all" checkbox column is shown when the request is in either state FOR_APPROVAL, FOR_ADDRESSING,
   * FOR_CABLING or FOR_TESTING, except when the task is not yet claimed or the user is not authorised.
   *
   * TODO: remove this domain-specific code
   *
   * @returns {boolean}
   */
  function hasCheckboxColumn(request) {
    var checkboxStates = [/*'FOR_CORRECTION', */'FOR_APPROVAL', 'FOR_CABLING', 'FOR_TESTING'];
    var task = TaskService.getCurrentTask();
    var assigned = TaskService.isCurrentUserAssigned(task);
    return checkboxStates.indexOf(request.status) > -1 && (request.status === 'FOR_CORRECTION' || assigned);
  }

  /**
   * TODO: remove this domain-specific code
   */
  function hasCommentColumn(request) {
    var commentStates =  ['FOR_APPROVAL', 'FOR_CABLING', 'FOR_TESTING'];
    var task = TaskService.getCurrentTask();
    var assigned = TaskService.isCurrentUserAssigned(task);
    return commentStates.indexOf(request.status) > -1 && assigned;
  }

  /**
   * TODO: remove this domain-specific code
   */
  function getCheckboxColumn(request, schema) {
    return {data: 'selected', type: 'checkbox', title: '<input type="checkbox" class="select-all" />', renderer: getRenderer(request, schema)};
  }

  /**
   * TODO: remove this domain-specific code
   */
  function getCommentColumn(request, schema) {
    var property;
    if (request.status === 'FOR_APPROVAL') {
      property = 'properties.approvalResult.message';
    } else if (request.status === 'FOR_CABLING') {
      property = 'properties.cablingResult.message';
    }else if (request.status === 'FOR_TESTING') {
      property = 'properties.testResult.message';
    }

    return {data: property, type: 'text', title: 'Comment', renderer: getRenderer(request, schema)};
  }


  function getRenderer(request, schema) {
    return function customRenderer(instance, td, row, col, prop, value, cellProperties) {
      /*jshint validthis:true */

      switch (cellProperties.type) {
        case 'text':
          Handsontable.renderers.TextRenderer.apply(this, arguments);
          break;
        case 'numeric':
          Handsontable.renderers.NumericRenderer.apply(this, arguments);
          break;
        case 'checkbox':
          Handsontable.renderers.CheckboxRenderer.apply(this, arguments);
          break;
      }

      if (cellProperties.editor === 'select2') {
        Handsontable.renderers.AutocompleteRenderer.apply(this, arguments);
      }

      if (typeof prop !== 'string') {
        return;
      }

      var point = request.points[row];
      if (!point || Utils.isEmptyPoint(point)) {
        return;
      }

      var props = prop.split('.').slice(1, 3);
      //prop = prop.replace('properties.', '');

      // Check if we need to fill in a default value for this point.
      var field = Utils.getField(schema, props[0]);
      if (field) {
        setDefaultValue(point, field);
      }

      // Highlight errors in a cell by making the background red.
      for (var i in point.errors) {
        var error = point.errors[i];

        // If the property name isn't specified, then the error applies to the whole point.
        if (error.property === prop.replace('properties.', '') || error.property === props[0] || error.property === '') {
          td.className += ' alert-danger';
          break;
        }
        // Highlight an entire category if the property matches a category name.
        else if (!error.property || error.property === error.category) {
          var category = Utils.getCategory(schema, error.category);

          if (category) {
            for (var j in category.fields) {
              var f = category.fields[j];

              if (f.id === field.id) {
                td.className += ' alert-danger';
                return;
              }
            }
          }
        }
      }


      //if (self.request.type === 'UPDATE' && point.dirty === true) {
      //  var changes = [];
      //  $(td).popover('destroy');
      //
      //  self.history.events.forEach(function (event) {
      //    event.changes.forEach(function (change) {
      //      if (change.path.indexOf(field.id) !== -1 && change.path.indexOf('[' + point.lineNo + ']') !== -1) {
      //        changes.push(change);
      //      }
      //    });
      //  });
      //
      //  if (changes.length > 0) {
      //    var latest = changes[changes.length - 1];
      //    var original, modified;
      //
      //    if (field.type === 'autocomplete') {
      //      original = field.model ? latest.original[field.model] : latest.original.value;
      //      modified = field.model ? latest.modified[field.model] : latest.modified.value;
      //    } else {
      //      original = latest.original;
      //      modified = latest.modified;
      //    }
      //
      //    var content = '<samp><table>';
      //    content    += '<tr><td style="background-color: #ffecec">&nbsp;- ' + original + '&nbsp;</td></tr>';
      //    content    += '<tr><td style="background-color: #dbffdb">&nbsp;+ ' + modified + '&nbsp;</td></tr></table></samp>';
      //
      //    td.style.background = '#fcf8e3';
      //    $(td).popover({ trigger: 'hover', placement: 'top', container: 'body', html: true, content: content/*, delay: { 'show': 0, 'hide': 100000 }*/ });
      //  }
      //}
    }
  }

  /**
   * Inspect the given field and set the default value in the point if supplied. The default value can refer
   * to another property of the point via mustache-syntax, so interpolate that as well.
   *
   * @param point
   * @param field
   */
  function setDefaultValue(point, field) {
    var currentValue;

    if (field.type === 'autocomplete') {
      if (point.properties.hasOwnProperty(field.id) && point.properties[field.id]) {
        currentValue = field.model ? point.properties[field.id][field.model] : point.properties[field.id].value;
      }
    } else {
      currentValue = point.properties[field.id];
    }

    if (currentValue === undefined || currentValue === null || currentValue === '') {
      var regex = /^\{\{\s*[\w\.]+\s*}}/g;

      if (field.default && typeof field.default === 'string' && regex.test(field.default)) {
        var matches = field.default.match(regex).map(function(x) {
          return x.match(/[\w\.]+/)[0];
        });

        var props = matches[0].split('.');

        if (point.properties.hasOwnProperty(props[0])) {
          var outerProp = point.properties[props[0]];

          if (outerProp.hasOwnProperty(props[1])) {
            var value = outerProp[props[1]];
            var model = field.model ? field.model : 'value';

            console.log('setting default value ' + value + ' for ' + field.id + '.' + model + ' on point ' + point.lineNo);

            if (point.properties.hasOwnProperty(field.id)) {
              point.properties[field.id][model] = value;
            } else {
              point.properties[field.id] = {};
              point.properties[field.id][model] = value;
            }
          }
        }
      }
    }
  }

  return service;
}
