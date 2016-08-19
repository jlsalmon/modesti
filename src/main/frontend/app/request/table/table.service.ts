import {SchemaService} from '../../schema/schema.service';
import {TaskService} from '../../task/task.service';
import {Utils} from '../../utils/utils';

declare var $:any;

export class TableService {
  public static $inject:string[] = ['$rootScope', 'SchemaService', 'TaskService', 'Utils'];

  public constructor(private $rootScope:any, private schemaService:SchemaService, private taskService:TaskService, private utils:Utils) {}

  public getColumns(request, schema, fields) {
    console.log('getting column definitions');
    var columns = [];

    var task = this.taskService.getCurrentTask();

    // Append "select-all" checkbox field.
    if (this.hasCheckboxColumn(request)) {
      columns.push(this.getCheckboxColumn(request, schema));
    }

    if (this.hasCommentColumn(request)) {
      columns.push(this.getCommentColumn(request, schema));
    }

    fields.forEach((field) => {

      var authorised = false;
      if (this.taskService.isCurrentUserAuthorised(task) && this.taskService.isCurrentUserAssigned(task)) {
        authorised = true;
      }

      var editable;

      // Build the right type of column based on the schema
      var column = this.getColumn(field, editable, authorised, request.status);
      columns.push(column);
    });

    return columns;
  }

  public getColumn(field, editable, authorised, status) {
    var column:any = {
      data: 'properties.' + field.id,
      title: this.getColumnHeader(field)
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
      column = this.getTextColumn(column, field);
    }

    if (field.type === 'autocomplete') {
      column = this.getAutocompleteColumn(column, field);
    }

    if (field.type === 'options') {
      column = this.getDropdownColumn(column, field);
    }

    if (field.type === 'numeric') {
      column.type = 'numeric';
    }

    if (field.type === 'checkbox') {
      // Just use true/false dropdown until copy/paste issues are fixed.
      // See https://github.com/handsontable/handsontable/issues/2497
      field.options = ['true', 'false'];
      column = this.getDropdownColumn(column, field);
    }

    return column;
  }

  public getColumnHeader(field) {
    var html = '<span class="help-text" data-container="body" data-toggle="popover" data-placement="bottom" ';
    /*jshint camelcase: false */
    html += 'data-content="' + field.help + '">';
    html += field.name;
    html += field.required ? '*' : '';
    html += '</span>';
    return html;
  }

  public getTextColumn(column, field) {
    if (field.url) {
      column.editor = 'select2';

      column.select2Options = this.getDefaultSelect2Options(column, field);

      // By default, text fields with URLs are not strict, as the queried
      // values are just suggestions
      if (field.strict !== true) {
        column.select2Options.createSearchChoice = function(term, data) {
          if ($(data).filter( function() {
                return this.text.localeCompare(term) === 0;
              }).length === 0) {
            return {id:term, text:term};
          }
        };
      }
    }

    return column;
  }

  public getAutocompleteColumn(column, field) {
    column.editor = 'select2';

    if (field.model) {
      column.data = 'properties.' + field.id + '.' + field.model;
    } else {
      column.data = 'properties.' + field.id + '.value';
    }

    column.select2Options = this.getDefaultSelect2Options(column, field);

    return column;
  }

  public getDropdownColumn(column, field) {
    column.editor = 'select2';

    var options;

    if (field.options) {
      options = field.options.map((option) => {
        if (typeof option === 'object') {
          if (option.description !== null && option.description !== undefined && option.description !== '') {
            return {id: option.value, text: option.value + ': ' + option.description};
          } else {
            return {id: option.value, text: option.value};
          }
        }

        else if (typeof option === 'string' || typeof option === 'number') {
          return {id: option, text: option.toString()};
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

  public getDefaultSelect2Options(column, field) {
    return {
      minimumInputLength: field.minLength || 0,
      maximumInputLength: 200,

      query: this.getQueryFunction(column, field),

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

  public getQueryFunction(column, field) {
    return (query) => {
      var hot = query.element.context.instance;
      var row = query.element.context.row;
      var point = hot.getSourceDataAtRow(row);

      this.schemaService.queryFieldValues(field, query.term, point).then((values) => {

        // Re-map the values in a format that the select2 editor likes
        var results = values.map((value) => {
          if (typeof value === 'string') {
            return {id: value, text: value};
          } else {
            return {id: value[this.getModelAttribute(field)], text: value[this.getModelAttribute(field)]};
          }
        });

        // Invoke the editor callback so it can populate itself
        query.callback({results: results, text: 'text'});
      });
    };
  }

  public getModelAttribute(field) {
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
  public hasCheckboxColumn(request) {
    var checkboxStates = [/*'FOR_CORRECTION', */'FOR_APPROVAL', 'FOR_CABLING', 'FOR_TESTING'];
    var task = this.taskService.getCurrentTask();
    var assigned = this.taskService.isCurrentUserAssigned(task);
    return checkboxStates.indexOf(request.status) > -1 && (request.status === 'FOR_CORRECTION' || assigned);
  }

  /**
   * TODO: remove this domain-specific code
   */
  public hasCommentColumn(request) {
    var commentStates =  ['FOR_APPROVAL', 'FOR_CABLING', 'FOR_TESTING'];
    var task = this.taskService.getCurrentTask();
    var assigned = this.taskService.isCurrentUserAssigned(task);
    return commentStates.indexOf(request.status) > -1 && assigned;
  }

  /**
   * TODO: remove this domain-specific code
   */
  public getCheckboxColumn(request, schema) {
    return {data: 'selected', type: 'checkbox', title: '<input type="checkbox" class="select-all" />'};
  }

  /**
   * TODO: remove this domain-specific code
   */
  public getCommentColumn(request, schema) {
    var property;
    if (request.status === 'FOR_APPROVAL') {
      property = 'properties.approvalResult.message';
    } else if (request.status === 'FOR_CABLING') {
      property = 'properties.cablingResult.message';
    }else if (request.status === 'FOR_TESTING') {
      property = 'properties.testResult.message';
    }

    return {data: property, type: 'text', title: 'Comment'};
  }
}
