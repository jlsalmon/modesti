import {ColumnFactory} from '../column-factory';
import {Table} from '../table';
import {SchemaService} from '../../schema/schema.service';
import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {OptionsField} from '../../schema/field/options-field';
import {AutocompleteField} from '../../schema/field/autocomplete-field';
import {TextField} from '../../schema/field/text-field';
import {Point} from '../../request/point/point';
import {Select2Editor} from './select2-editor';
import {IInterpolateService} from 'angular';

export class HandsontableColumnFactory {

  protected getColumnDefs(table: Table, meta: any): any[] {
    let columns: any[] = [];

    if (meta.authorised && meta.assigned) {

      // The schema can allow rows to be "selectable" for specified request statuses
      if (table.schema.hasRowSelectColumn(meta.requestStatus)) {
        columns.push(this.getRowSelectColumn());
      }

      // The schema can allow row comments for specified request statuses
      if (table.schema.hasRowCommentColumn(meta.requestStatus)) {
        columns.push(this.getRowCommentColumn(table.schema, meta.requestStatus));
      }
    }

    table.schema.categories.concat(table.schema.datasources).forEach((category: Category) => {
      category.fields.forEach((field: Field) => {

        // Build the right type of column based on the schema
        let column: any = this.createColumnDef(table, field, meta);
        columns.push(column);
      });
    });


    return columns;
  }

  public createColumnDef(table: Table, field: Field, meta: any): any[] {
    let column: any = {
      data: 'properties.' + field.id,
      title: this.getColumnHeader(field),
      field: field
    };

    if (meta.authorised && meta.assigned) {
      let editable: boolean = true;

      if (field.editable === true || field.editable === false) {
        // Editable given as simple boolean
        editable = field.editable;
      } else if (field.editable != null && typeof field.editable === 'object') {
        // Editable given as condition object
        editable = !!(field.editable.status && meta.requestStatus === field.editable.status);
      }

      column.readOnly = !editable;
    } else {
      column.readOnly = true;
    }

    if (field.type === 'text') {
      column = this.getTextColumn(table, column, meta, field as TextField);
    }

    if (field.type === 'autocomplete') {
      column = this.getAutocompleteColumn(table, column, meta, field as AutocompleteField);
    }

    if (field.type === 'options') {
      column = this.getOptionsColumn(table, column, meta, field as OptionsField);
    }

    if (field.type === 'numeric') {
      column.type = 'numeric';
    }

    if (field.type === 'checkbox') {
      // Just use true/false dropdown until copy/paste issues are fixed.
      // See https://github.com/handsontable/handsontable/issues/2497
      (<OptionsField> field).options = ['true', 'false'];
      column = this.getOptionsColumn(table, column, meta, field as OptionsField);
    }

    return column;
  }

  public getColumnHeader(field: Field): string {
    let html: string = '<span class="help-text" data-container="body" data-toggle="popover" data-placement="bottom" ';
    html += 'data-content="' + field.help + '">';
    html += field.name;
    html += field.required ? '*' : '';
    html += '</span>';
    return html;
  }

  public getTextColumn(table: Table, column: any, meta: any, field: TextField): any {
    if (field.url) {
      column.editor = Select2Editor;
      column.table = table;
      column.field = field;
      column.schemaService = meta.schemaService;
      column.select2Options = this.getDefaultSelect2Options(column, field, meta);

      // By default, text fields with URLs are not strict, as the queried
      // values are just suggestions
      if (field.strict !== true) {
        column.select2Options.createSearchChoice = function(term: any, data: any): any {
          if (data.filter((item: any) => {
              return item.text.localeCompare(term) === 0;
            }).length === 0) {
            return {id: term, text: term};
          }
        };
      }
    }

    return column;
  }

  public getAutocompleteColumn(table: Table, column: any, meta: any, field: AutocompleteField): any {
    column.editor = Select2Editor;

    if (field.model) {
      column.data = 'properties.' + field.id + '.' + field.model;
    } else {
      column.data = 'properties.' + field.id + '.value';
    }

    column.table = table;
    column.field = field;
    column.schemaService = meta.schemaService;
    column.select2Options = this.getDefaultSelect2Options(column, field, meta);
    return column;
  }

  public getOptionsColumn(table: Table, column: any, meta: any, field: OptionsField): any {
    column.editor = Select2Editor;

    let options: any;

    if (field.options) {
      options = field.options.map((option: any) => {
        if (typeof option === 'object') {
          if (option.description != null && option.description !== '') {
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

    column.table = table;
    column.field = field;
    column.schemaService = meta.schemaService;
    //column.select2Options = this.getDefaultSelect2Options(column, field, meta);
    return column;
  }

  public getDefaultSelect2Options(column: any, field: Field, meta: any): any {
    return {
      minimumInputLength: field.minLength || 0,
      maximumInputLength: 200,
      query: this.getQueryFunction(column, field, meta.schemaService, meta.interpolate),
      dropdownAutoWidth: true,
      width: 'resolve'
    };
  }

  public getQueryFunction(column: any, field: Field, schemaService: SchemaService, interpolate: IInterpolateService): any {
    return (query: any) => {
      let hot: any = query.element[0].instance;
      let row: number = query.element[0].row;
      let point: Point = hot.getSourceDataAtRow(row);

      schemaService.queryFieldValues(field, query.term, point).then((values: any[]) => {

        // Re-map the values in a format that the select2 editor likes
        let results: any[] = values.map((value: any) => {
          if (typeof value === 'string') {
            return {id: value, text: value.toString(), data: value};
          } else {
            return {id: value[this.getModelAttribute(field)], text: this.getAutocompleteText(field, value, interpolate), data: value};
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

  public getAutocompleteText(field: Field, value: any, interpolate: IInterpolateService): string {
    // If the `"template": "{{value}}: {{description}}"` attribute is set, the placeholders
    // needs to be replaced by the real values.
    return field.template ? interpolate(field.template)(value) : value[this.getModelAttribute(field)].toString;
  }

  public getRowSelectColumn(): any {
    return {data: 'selected', type: 'checkbox', title: '<input type="checkbox" class="select-all" />'};
  }

  public getRowCommentColumn(schema: Schema, requestStatus: string): any {
    let property: string;

    schema.rowCommentStates.forEach((rowCommentState: any) => {
      if (rowCommentState.status === requestStatus) {
        property = rowCommentState.property;
      }
    });

    return {data: 'properties.' + property, type: 'text', title: 'Comment'};
  }
}
