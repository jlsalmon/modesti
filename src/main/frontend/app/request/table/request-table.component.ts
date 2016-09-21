import {Table} from '../../table/table';
import {TableFactory} from '../../table/table-factory';
import {RequestService} from '../request.service';
import {Request} from '../request';
import {Point} from '../point/point';
import {TaskService} from '../../task/task.service';
import {Task} from '../../task/task';
import {SchemaService} from '../../schema/schema.service';
import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {AutocompleteField} from '../../schema/field/autocomplete-field';
import {Conditional} from '../../schema/conditional';
import {Change} from '../history/change';

import {IComponentOptions, IPromise, IDeferred, IScope, IQService, IFilterService} from 'angular';
import 'jquery';

// TODO: import this properly without require()
let Handsontable: any = require('handsontable-pro');

export class RequestTableComponent implements IComponentOptions {
  public templateUrl: string = '/request/table/request-table.component.html';
  public controller: Function = RequestTableController;
  public bindings: any = {
    request: '=',
    tasks: '=',
    schema: '=',
    table: '=',
    activeCategory: '=',
    history: '='
  };
}

class RequestTableController {
  public static $inject: string[] = ['$scope', '$q', '$filter', '$localStorage',
                                     'RequestService', 'TaskService', 'SchemaService'];

  public request: Request;
  public tasks: Task[];
  public schema: Schema;
  public table: Table;
  public activeCategory: Category;
  public history: Change[];

  public constructor(private $scope: IScope, private $q: IQService, private $filter: IFilterService,
                     private $localStorage: any, private requestService: RequestService,
                     private taskService: TaskService, private schemaService: SchemaService) {

    let task: Task = this.taskService.getCurrentTask();
    let authorised: boolean = false;

    if (this.taskService.isCurrentUserAuthorised(task) && this.taskService.isCurrentUserAssigned(task)) {
      authorised = true;
    }

    let settings: any = {
      authorised: authorised,
      requestStatus: this.request.status,
      // TODO: is there a better way than passing the service?
      schemaService: this.schemaService,
      cellRenderer: this.renderCell,
      cells: this.evaluateCellSettings,
      afterChange: this.onAfterChange,
      afterRender: this.onAfterRender
    };

    this.table = TableFactory.createTable('handsontable', this.schema, this.request.points, settings);

    // Add additional helper methods
    this.table.navigateToField = this.navigateToField;
  }

  /**
   * Evaluate "editable" state of each cell
   *
   * TODO: this could happen on init
   */
  public evaluateCellSettings = (row: number, col: number, prop: any) => {
    if (typeof prop !== 'string') {
      return;
    }

    let task: Task = this.taskService.getCurrentTask();

    let authorised: boolean = false;
    if (this.taskService.isCurrentUserAuthorised(task) && this.taskService.isCurrentUserAssigned(task)) {
      authorised = true;
    }

    let editable: boolean = false;
    if (authorised) {
      let point: Point = this.request.points[row];

      // Evaluate "editable" condition of the category
      if (this.activeCategory.editable !== undefined && typeof this.activeCategory.editable === 'object') {
        let conditional: any = this.activeCategory.editable;

        if (conditional !== undefined) {
          editable = this.schemaService.evaluateConditional(point, conditional, this.request.status);
        }
      }

      // Evaluate "editable" condition of the field as it may override the category
      this.activeCategory.fields.forEach((field: Field) => {
        if (field.id === prop.split('.')[1]) {
          let conditional: Conditional = field.editable;

          if (conditional !== undefined) {
            editable = this.schemaService.evaluateConditional(point, conditional, this.request.status);
          }
        }
      });

      if (this.schema.hasRowSelectColumn(this.request.status) && prop === 'selected') {
        editable = true;
      } else if (this.schema.hasRowCommentColumn(this.request.status) && prop.contains('message')) {
        editable = true;
      }
    }

    return { readOnly: !editable };
  };

  public renderCell = (instance: any, td: HTMLElement, row: number, col: number, prop: string,
                                          value: any, cellProperties: any): void => {
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
      default: break;
    }

    if (cellProperties.editor === 'select2') {
      Handsontable.renderers.AutocompleteRenderer.apply(this, arguments);
    }

    if (typeof prop !== 'string' || prop.indexOf('properties') === -1) {
      return;
    }

    let point: Point = this.request.points[row];
    if (!point || point.isEmpty()) {
      return;
    }

    let props: string[] = prop.split('.').slice(1, 3);

    let field: Field = this.schema.getField(props[0]);
    if (!field) {
      return;
    }


    // FIXME: looks like in this version of handsontable we can save objects
    // directly...
    //if (value && typeof value === 'object') {
    //  td.innerHTML = (field.model ? value[field.model] : value.value);
    //}


    // Check if we need to fill in a default value for this point.
    this.setDefaultValue(point, field);

    // Highlight errors in a cell by making the background red.
    angular.forEach(point.errors, (error: any) => {

      if (error.property === prop.replace('properties.', '') || error.property === props[0] || error.property === '') {
        // If the property name isn't specified, then the error applies to the whole point.
        td.className += ' alert-danger';
        return;
      } else if (!error.property || error.property === error.category) {
        // Highlight an entire category if the property matches a category name.
        let category: Category = this.schema.getCategory(error.category);

        if (category) {
          category.fields.forEach((f: Field) => {
            if (f.id === field.id) {
              td.className += ' alert-danger';
              return;
            }
          });
          return;
        }
      }
    });


    if (this.request.type === 'UPDATE' && point.dirty === true) {
      let changes: Change[] = [];
      $(td).popover('destroy');

      this.history.events.forEach((event: any) => {
        event.changes.forEach((change: any) => {
          if (change.path.indexOf(field.id) !== -1 && change.path.indexOf('[' + point.lineNo + ']') !== -1) {
            changes.push(change);
          }
        });
      });

      if (changes.length > 0) {
        let latest: any = changes[changes.length - 1];
        let original: any, modified: any;

        if (field.type === 'autocomplete') {
          original = field.model ? latest.original[field.model] : latest.original.value;
          modified = field.model ? latest.modified[field.model] : latest.modified.value;
        } else {
          original = latest.original;
          modified = latest.modified;
        }

        let content: string = '<samp><table>';
        content += '<tr><td style="background-color: #ffecec">&nbsp;- ' + original + '&nbsp;</td></tr>';
        content += '<tr><td style="background-color: #dbffdb">&nbsp;+ ' + modified + '&nbsp;</td></tr></table></samp>';

        td.style.background = '#fcf8e3';
        $(td).popover({ trigger: 'hover', placement: 'top', container: 'body', html: true, content: content });
      }
    }
  };

  /**
   * Inspect the given field and set the default value in the point if supplied. The default value can refer
   * to another property of the point via mustache-syntax, so interpolate that as well.
   *
   * @param point
   * @param field
   */
  public setDefaultValue(point: Point, field: Field): void {
    let currentValue: any;

    if (field.type === 'autocomplete') {
      if (point.properties.hasOwnProperty(field.id) && point.properties[field.id]) {
        currentValue = field.model ?
          point.properties[field.id][field.model] : point.properties[field.id].value;
      }
    } else {
      currentValue = point.properties[field.id];
    }

    if (currentValue === undefined || currentValue === '') {
      let regex: RegExp = /^\{\{\s*[\w\.]+\s*}}/g;

      if (field.default && typeof field.default === 'string' && regex.test(field.default)) {
        let matches: string = field.default.match(regex).map((x: string) => {
          return x.match(/[\w\.]+/)[0];
        });

        let props: string[] = matches[0].split('.');

        if (point.properties.hasOwnProperty(props[0])) {
          let outerProp: string = point.properties[props[0]];

          if (outerProp.hasOwnProperty(props[1])) {
            let value: any = outerProp[props[1]];
            let model: string = field.model ? field.model : 'value';

            console.log('setting default value ' + value + ' for ' + field.id + '.' + model
                        + ' on point ' + point.lineNo);

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

  /**
   * Slightly hacky little function to make sure all the elements on the page
   * are properly initialised.
   */
  public onAfterRender = () => {
    console.log('onAfterRender');

    // Initialise the popovers in the row headers
    $('.row-header').popover({trigger: 'hover', delay: {'show': 100, 'hide': 100}});

    // Initialise the help text popovers on the column headers
    $('.help-text').popover({trigger: 'hover', delay: {'show': 500, 'hide': 100}});

    if (this.schema.hasRowSelectColumn(this.schema, this.request.status)) {

      let firstColumnHeader: JQuery = $('.htCore colgroup col.rowHeader');
      let lastColumnHeader: JQuery = $('.htCore colgroup col:last-child');
      let checkboxColumn: JQuery = $('.htCore colgroup col:nth-child(2)');

      // Fix the width of the 'select-all' checkbox column (second column)
      // and add the surplus to the last column
      let lastColumnHeaderWidth: number = lastColumnHeader.width() + (checkboxColumn.width() - 30);
      lastColumnHeader.width(lastColumnHeaderWidth);
      checkboxColumn.width('30px');

      // Fix the width of the first column (point id column)
      firstColumnHeader.width('45px');

      // Centre checkboxes
      let checkboxCells: JQuery = $('.htCore input.htCheckboxRendererInput').parent();
      checkboxCells.css('text-align', 'center');

      // Initialise checkbox header state
      let checkboxHeader: JQuery = $('.select-all:checkbox');
      checkboxHeader.prop(this.getCheckboxHeaderState(), true);

      let header: JQuery, cells: JQuery;
      if (this.schema.hasRowCommentColumn(this.schema, this.request.status)) {
        header = $('.htCore thead th:nth-child(3)');
        cells = $('.htCore tbody td:nth-child(3)');
      } else {
        header = $('.htCore thead th:nth-child(2)');
        cells = $('.htCore tbody td:nth-child(2)');
      }

      // Add a thicker border between the control column(s) and the first data column
      header.css('border-right', '5px double #ccc');
      cells.css('border-right', '5px double #ccc');

      // Listen for the change event on the 'select-all' checkbox and act accordingly
      checkboxHeader.change(() => {
        for (let i: number = 0, len: number = this.request.points.length; i < len; i++) {
          this.request.points[i].selected = this.checked;
        }

        // Need to explicitly trigger a digest loop here because we are out
        // of the angularjs world and in the happy land of jquery hacking
        this.$scope.$apply();
      });

      // Listen for change events on all checkboxes
      $('.htCheckboxRendererInput:checkbox').change(() => {
        $('.select-all:checkbox').prop(this.getCheckboxHeaderState(), true);
      });
    }
  };

  public getCheckboxHeaderState(): string {
    if (!this.table.hasOwnProperty('getSelectedLineNumbers')) {
      return 'unchecked';
    }

    if (this.table.getSelectedLineNumbers().length === this.request.points.length) {
      return 'checked';
    } else if (this.table.getSelectedLineNumbers().length > 0) {
      return 'indeterminate';
    } else {
      return 'unchecked';
    }
  }

  /**
   * Called after a change is made to the table (edit, paste, etc.)
   *
   * @param changes a 2D array containing information about each of the edited
   *                cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: "alter", "empty", "edit",
   *               "populateFromArray", "loadData", "autofill", "paste"
   */
  public onAfterChange = (changes: any[], source: string): void => {
    // When the table is initially loaded, this callback is invoked with
    // source === 'loadData'. In that case, we don't want to do anything.
    if (source === 'loadData') {
      return;
    }

    console.log('onAfterChange()');

    // Make sure the line numbers are consecutive
    // this.normaliseLineNumbers();

    let promises: IPromise<any>[] = [];

    // Loop over the changes and check if anything actually changed. Mark any changed points as dirty.
    let change: any, row: number, property: string, oldValue: any, newValue: any, dirty: boolean = false;
    for (let i: number = 0, ilen: number = changes.length; i < ilen; i++) {
      change = changes[i];
      row = change[0];
      property = change[1];
      oldValue = change[2];
      newValue = change[3];

      // Mark the point as dirty.
      if (newValue !== oldValue) {
        console.log('dirty point: ' + this.request.points[row].lineNo);
        dirty = true;
        this.request.points[row].dirty = true;
      }

      // If the value was cleared, make sure any other properties of the object are also cleared.
      if (newValue === undefined || newValue === '') {
        // let point = this.parent.hot.getSourceDataAtRow(row);
        let point: Point = this.request.points[row];
        let propName: string = property.split('.')[1];

        let prop: any = point.properties[propName];

        if (typeof prop === 'object') {
          for (let attribute in prop) {
            if (prop.hasOwnProperty(attribute)) {
              prop[attribute] = undefined;
            }
          }
        } else {
          prop = undefined;
        }

      }

      // This is a workaround. See function documentation for info.
      let promise: IPromise<any> = this.saveNewValue(row, property, newValue);
      promises.push(promise);
    }

    // Wait for all new values to be updated
    this.$q.all(promises).then(() => {

      // If nothing changed, there's nothing to do! Otherwise, save the request.
      if (dirty) {
        this.request.valid = false;

        this.requestService.saveRequest(this.request).then((request: Request) => {
          this.request = request;

          // Reload the history
          this.requestService.getRequestHistory(this.request.requestId).then((history: any) => {
            this.history = history;

            this.table.hot.render();
          });
        });
      }
    });
  };

  /**
   * Currently Handsontable does not support columns backed by complex objects,
   * so for now it's necessary to manually save the object in the background.
   * See https://github.com/handsontable/handsontable/issues/2578.
   *
   * Also, sometimes after a modification, Handsontable does not properly save
   * the new value to the underlying point. So we manually save the value in
   * the background to be doubly sure that the new value is persisted.
   *
   * @param row
   * @param property
   * @param newValue
   */
  public saveNewValue(row: number, property: string, newValue: any): IPromise<any> {
    let q: IDeferred<any> = this.$q.defer();
    let point: Point = this.request.points[row];
    let outerProp: string;

    if (typeof property === 'string') {
      // get the outer object i.e. properties.location.value -> location
      outerProp = property.split('.')[1];
    } else {
      outerProp = this.activeCategory.fields[property].id;
    }

    let field: Field = this.schema.getField(outerProp);

    // If there is no corresponding field in the schema, then it must be a "virtual"
    // column (such as "comment" or "checkbox")
    if (field === undefined) {
      q.resolve();
      return q.promise;
    }

    if (field.type === 'autocomplete') {
      // For autocomplete fields, re-query the values and manually save it back to the point.
      this.schemaService.queryFieldValues(field, newValue, point).then((values: any[]) => {

        values.forEach((item: any) => {
          let value: any = (field.model === undefined && typeof item === 'object') ? item.value : item[field.model];

          if (value === newValue) {
            console.log('saving new value');
            delete item._links;
            point.properties[outerProp] = item;
          }
        });

        q.resolve();
      });
    } else {
      // For non-autocomplete fields, just manually save the new value.
      point.properties[outerProp] = newValue;
      q.resolve();
    }

    return q.promise;
  }

  /**
   * Navigate somewhere to focus on a particular field.
   *
   * @param categoryName the name of the category to which the field belongs
   * @param fieldId the id of the field to focus on
   */
   public navigateToField = (categoryName: string, fieldId: string) => {
    // Find the category which contains the field
    let category: Category;

    if (fieldId.indexOf('.') !== -1) {
      fieldId = fieldId.split('.')[0];
    }

    this.schema.categories.concat(this.schema.datasources).forEach((cat: Category) => {
      if (cat.name === categoryName || cat.id === categoryName) {
        cat.fields.forEach((field: Field) => {
          if (field.id === fieldId || cat.name === fieldId || cat.id === fieldId) {
            category = cat;
          }
        });
      }
    });

    if (category) {
      this.activeCategory = category;
    }
   };
}
