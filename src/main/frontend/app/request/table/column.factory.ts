import {SchemaService} from '../../schema/schema.service';
import {Schema} from '../../schema/schema';
import {Field} from '../../schema/field';
import {OptionsField} from '../../schema/options-field';
import {TextField} from '../../schema/text-field';
import {AutocompleteField} from '../../schema/autocomplete-field';
import {TaskService} from '../../task/task.service';
import {Task} from '../../task/task';
import {Request} from '../request';
import {Point} from '../point/point';

declare var $: JQuery;

export class ColumnFactory {
  public static $inject: string[] = ['SchemaService', 'TaskService'];

  public constructor(private schemaService: SchemaService, private taskService: TaskService) {}

  public getColumns(request: Request, schema: Schema, fields: Field[]): any[] {
    console.log('getting column definitions');
    let columns: any[] = [];

    let task: Task = this.taskService.getCurrentTask();

    // Append "select-all" checkbox field.
    if (this.hasCheckboxColumn(request)) {
      columns.push(this.getCheckboxColumn(request, schema));
    }

    if (this.hasCommentColumn(request)) {
      columns.push(this.getCommentColumn(request, schema));
    }

    fields.forEach((field: Field) => {

      let authorised: boolean = false;
      if (this.taskService.isCurrentUserAuthorised(task) && this.taskService.isCurrentUserAssigned(task)) {
        authorised = true;
      }

      let editable: any;

      // Build the right type of column based on the schema
      let column: any = this.getColumn(field, editable, authorised, request.status);
      columns.push(column);
    });

    return columns;
  }

  public getColumn(field: Field, editable: any, authorised: boolean, status: string): any[] {
    let column: any = {
      data: 'properties.' + field.id,
      title: this.getColumnHeader(field)
    };

    if (authorised) {
      editable = true;

      if (field.editable === true || field.editable === false) {
        // Editable given as simple boolean
        editable = field.editable;
      } else if (field.editable !== undefined && typeof field.editable === 'object') {
        // Editable given as condition object
        editable = !!(field.editable.status && status === field.editable.status);
      }

      column.readOnly = !editable;
    } else {
      column.readOnly = true;
    }

    if (field.type === 'text') {
      column = this.getTextColumn(column, field as TextField);
    }

    if (field.type === 'autocomplete') {
      column = this.getAutocompleteColumn(column, field as AutocompleteField);
    }

    if (field.type === 'options') {
      column = this.getOptionsColumn(column, field as OptionsField);
    }

    if (field.type === 'numeric') {
      column.type = 'numeric';
    }

    if (field.type === 'checkbox') {
      // Just use true/false dropdown until copy/paste issues are fixed.
      // See https://github.com/handsontable/handsontable/issues/2497
      (<OptionsField> field).options = ['true', 'false'];
      column = this.getOptionsColumn(column, field as OptionsField);
    }

    return column;
  }

  public getColumnHeader(field: Field): string {
    let html: string = '<span class="help-text" data-container="body" data-toggle="popover" data-placement="bottom" ';
    /*jshint camelcase: false */
    html += 'data-content="' + field.help + '">';
    html += field.name;
    html += field.required ? '*' : '';
    html += '</span>';
    return html;
  }

  public getTextColumn(column: any, field: TextField): any {
    if (field.url) {
      column.editor = 'select2';

      column.select2Options = this.getDefaultSelect2Options(column, field);

      // By default, text fields with URLs are not strict, as the queried
      // values are just suggestions
      if (field.strict !== true) {
        column.select2Options.createSearchChoice = function(term: any, data: any): any {
          if ($(data).filter(() => {
                return this.text.localeCompare(term) === 0;
              }).length === 0) {
            return {id: term, text: term};
          }
        };
      }
    }

    return column;
  }

  public getAutocompleteColumn(column: any, field: AutocompleteField): any {
    column.editor = 'select2';

    if (field.model) {
      column.data = 'properties.' + field.id + '.' + field.model;
    } else {
      column.data = 'properties.' + field.id + '.value';
    }

    column.select2Options = this.getDefaultSelect2Options(column, field);
    return column;
  }

  public getOptionsColumn(column: any, field: OptionsField): any {
    column.editor = 'select2';

    let options: any;

    if (field.options) {
      options = field.options.map((option: any) => {
        if (typeof option === 'object') {
          if (option.description !== undefined && option.description !== '') {
            return {id: option.value, text: option.value + ': ' + option.description};
          } else {
            return {id: option.value, text: option.value.toString()};
          }
        } else if (typeof option === 'string' || typeof option === 'number') {
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

  public getDefaultSelect2Options(column: any, field: Field): any {
    return {
      minimumInputLength: field.minLength || 0,
      maximumInputLength: 200,

      query: this.getQueryFunction(column, field),

      formatSelection: (option: any) => {
        return option;
      },

      initSelection: (element: any, callback: any) => {
        callback(element.context.value);
      },

      nextSearchTerm: (selectedObject: any) => {
        return selectedObject;
      },

      dropdownAutoWidth: true,
      width: 'resolve'
    };
  }

  public getQueryFunction(column: any, field: Field): any {
    return (query: any) => {
      let hot: any = query.element.context.instance;
      let row: number = query.element.context.row;
      let point: Point = hot.getSourceDataAtRow(row);

      this.schemaService.queryFieldValues(field, query.term, point).then((values: any[]) => {

        // Re-map the values in a format that the select2 editor likes
        let results: any[] = values.map((value: any) => {
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

  public getModelAttribute(field: Field): string {
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
  public hasCheckboxColumn(request: Request): boolean {
    let checkboxStates: string[] = [/*'FOR_CORRECTION', */'FOR_APPROVAL', 'FOR_CABLING', 'FOR_TESTING'];
    let task: Task = this.taskService.getCurrentTask();
    let assigned: boolean = this.taskService.isCurrentUserAssigned(task);
    return checkboxStates.indexOf(request.status) > -1 && (request.status === 'FOR_CORRECTION' || assigned);
  }

  /**
   * TODO: remove this domain-specific code
   */
  public hasCommentColumn(request: Request): boolean {
    let commentStates: string[] =  ['FOR_APPROVAL', 'FOR_CABLING', 'FOR_TESTING'];
    let task: Task = this.taskService.getCurrentTask();
    let assigned: boolean = this.taskService.isCurrentUserAssigned(task);
    return commentStates.indexOf(request.status) > -1 && assigned;
  }

  /**
   * TODO: remove this domain-specific code
   */
  public getCheckboxColumn(request: Request, schema: Schema): any {
    return {data: 'selected', type: 'checkbox', title: '<input type="checkbox" class="select-all" />'};
  }

  /**
   * TODO: remove this domain-specific code
   */
  public getCommentColumn(request: Request, schema: Schema): any {
    let property: string;
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
