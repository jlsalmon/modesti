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
import { CacheService } from '../../cache/cache.service';

import {IComponentOptions, IPromise, IDeferred, IScope, IQService, IFilterService, IInterpolateService} from 'angular';
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
    history: '='
  };
}

class RequestTableController {
  public static $inject: string[] = ['$scope', '$q', '$filter', '$localStorage', '$interpolate',
                                     'RequestService', 'TaskService', 'SchemaService', 'CacheService'];

  public request: Request;
  public tasks: Task[];
  public schema: Schema;
  public table: Table;
  public history: Change[];

  public constructor(private $scope: IScope, private $q: IQService, private $filter: IFilterService,
                     private $localStorage: any, private $interpolate: IInterpolateService, private requestService: RequestService,
                     private taskService: TaskService, private schemaService: SchemaService, private cacheService: CacheService) {

    let settings: any = {
      requestStatus: this.request.status,
      requestType: this.request.type,
      // TODO: is there a better way than passing the services?
      schemaService: this.schemaService,
      taskService: this.taskService,
      cellRenderer: this.renderCell,
      afterChange: this.onAfterChange,
      afterRender: this.onAfterRender,
      interpolate: this.$interpolate,
      cacheService: this.cacheService,
      afterCreateRow: (index: number, count: number, source: any) => this.onAfterCreateRow(index, count, source),
      afterRemoveRow: () => this.onAfterRemoveRow()
    };

    this.table = TableFactory.createTable('handsontable', this.schema, this.request.points, settings);
    this.table.render();
  }

  public renderCell = (instance: any, td: HTMLElement, row: number, col: number, prop: string,
                                          value: any, cellProperties: any): void => {
    if (this.table == null) {
      return;
    }

    if (this.table.hotOptions.columns[col] == null) {
      return;
    }

    let field: Field = this.table.hotOptions.columns[col].field;

    let type: string = field ? field.type : cellProperties.type;
    switch (type) {
      case 'text':
      case 'numeric':
        Handsontable.renderers.TextRenderer.apply(this, arguments);
        break;
      case 'checkbox':
        Handsontable.renderers.CheckboxRenderer.apply(this, arguments);
        break;
      case 'autocomplete':
      case 'options':
        Handsontable.renderers.AutocompleteRenderer.apply(this, arguments);
        break;
      default: break;
    }

    let point: Point = this.request.points[row];
    if (!point || point.isEmpty()) {
      return;
    }

    if (typeof prop !== 'string') {
      return;
    }

    if (field) {
      // Check if we need to fill in a default value for this point.
      this.setDefaultValue(point, field);
    }

    // Highlight errors in a cell by making the background red.
    angular.forEach(point.errors, (error: any) => {

      if (prop.indexOf('properties.') !== -1) {
        prop = prop.replace('properties.', '');
      }

      if (prop.indexOf('.') !== -1) {
        prop = prop.split('.')[0];
      }

      if (!error.property) {
        return;
      }

      if (error.property === prop || error.property.split('.')[0] === prop || error.property === '') {
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

    if (!field) {
      return;
    }

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
          if (latest.original != null) {
              original = field.model ? latest.original[field.model] : latest.original.value;
          }
          modified = field.model ? latest.modified[field.model] : latest.modified.value;
        } else {
          original = latest.original;
          modified = latest.modified;
        }

        let content: string = '<samp><table>';
        if (original != null && original != '') {
          content += '<tr><td style="background-color: #ffecec">&nbsp;- ' + original + '&nbsp;</td></tr>';
        }
        if (modified != null && modified != '') {
          content += '<tr><td style="background-color: #dbffdb">&nbsp;+ ' + modified + '&nbsp;</td></tr></table></samp>';
        }

        td.setAttribute('style', 'background-color: #fcf8e3 !important');
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

    if (currentValue == null || currentValue === '') {
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

    if (this.schema.hasRowSelectColumn(this.request.status)) {

      // let firstColumnHeader: JQuery = $('.htCore colgroup col.rowHeader');
      // let lastColumnHeader: JQuery = $('.htCore colgroup col:last-child');
      // let checkboxColumn: JQuery = $('.htCore colgroup col:nth-child(2)');

      // Fix the width of the 'select-all' checkbox column (second column)
      // and add the surplus to the last column
      // let lastColumnHeaderWidth: number = lastColumnHeader.width() + (checkboxColumn.width() - 30);
      // lastColumnHeader.width(lastColumnHeaderWidth);
      // checkboxColumn.width('30px');

      // Fix the width of the first column (point id column)
      // firstColumnHeader.width('45px');

      // Centre checkboxes
      let checkboxCells: JQuery = $('.htCore input.htCheckboxRendererInput').parent();
      checkboxCells.css('text-align', 'center');

      // Initialise checkbox header state
      let checkboxHeader: JQuery = $('.select-all:checkbox');
      checkboxHeader.prop(this.getCheckboxHeaderState(), true);

      let header: JQuery, cells: JQuery;
      if (this.schema.hasRowCommentColumn(this.request.status)) {
        header = $('.htCore thead th:nth-child(3)');
        cells = $('.htCore tbody td:nth-child(3)');
      } else {
        header = $('.htCore thead th:nth-child(2)');
        cells = $('.htCore tbody td:nth-child(2)');
      }

      // Add a thicker border between the control column(s) and the first data column
      // header.css('border-right', '5px double #ccc');
      // cells.css('border-right', '5px double #ccc');

      // Listen for the change event on the 'select-all' checkbox and act accordingly
      // checkboxHeader.change(() => {
      //  for (let i: number = 0, len: number = this.request.points.length; i < len; i++) {
      //    this.request.points[i].selected = this.checked;
      //  }
      //
      //  // Need to explicitly trigger a digest loop here because we are out
      //  // of the angularjs world and in the happy land of jquery hacking
      //  this.$scope.$apply();
      // });
      //
      // Listen for change events on all checkboxes
      // $('.htCheckboxRendererInput:checkbox').change(() => {
      //  $('.select-all:checkbox').prop(this.getCheckboxHeaderState(), true);
      // });
    }
  };

  public getCheckboxHeaderState(): string {
    if (!this.table || !this.table.hasOwnProperty('getSelectedLineNumbers')) {
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
      
      if (property === 'properties.pointType' && oldValue != newValue) {  
        let fieldChangePromises : IPromise<any>[] = this.deleteOldPointTypeProperties(row, oldValue, newValue); 
        if (fieldChangePromises.length > 0) {
          promises = promises.concat(fieldChangePromises);
          dirty = true;
        }
      }

      let field: Field;
      if (typeof property === 'string') {
        let col: number = this.table.hot.propToCol(property);
        field = this.table.hotOptions.columns[col].field;
      } else {
        field = this.table.hotOptions.columns[property].field;
      }

      // Mark the point as dirty.
      if (newValue !== oldValue) {
        console.log('dirty point: ' + this.request.points[row].lineNo);
        dirty = true;
        this.request.points[row].dirty = true;
      }

      // If the value was cleared, make sure any other properties of the object are also cleared.
      if (field && (newValue == null || newValue === '')) {
        this.setPropertyToNull(row, field);
      }

      // This is a workaround. See function documentation for info.
      let promise: IPromise<any> = this.saveNewValue(row, field, newValue);
      promises.push(promise);
    }

    // Wait for all new values to be updated
    this.$q.all(promises).then(() => {

      // If nothing changed, there's nothing to do! Otherwise, save the request.
      if (dirty) {
        this.request.valid = false;

        this.requestService.saveRequest(this.request).then((request: Request) => {
          this.request = request;

          if (request.type === 'UPDATE') {
            // Reload the history
            this.requestService.getRequestHistory(this.request.requestId).then((history: any) => {
              this.history = history;
              this.table.render();
            });
          }
        });
      }
    });
  };
  

  private setPropertyToNull(row: number, field: Field) {
    let point: Point = this.request.points[row];

    if (typeof point.properties[field.id] === 'object') {
      for (let attribute in point.properties[field.id]) {
        if (point.properties[field.id].hasOwnProperty(attribute)) {
          point.properties[field.id][attribute] = null;
        }
      }
    } else {
      point.properties[field.id] = null;
    }
  }


   /**
   * Called when the 'pointType' property is modified, the properties specific
   *   to the old category will be removed.
   * 
   * @param row the row number being modified
   * @param oldSource the old data source in the 'pointType' field
   * @param newSource the new data source in the 'pointType' field
   * @return Array of promises with the requests of the modified fields
   */
  public deleteOldPointTypeProperties(row:number, oldSource: string, newSource: string): IPromise<Request>[]  {       
    let point: Point = this.request.points[row];    
	let oldCategory = this.schema.getCategory(oldSource);
	let newCategory = this.schema.getCategory(newSource);
	let promises: IPromise<Request>[] = [];
        let diffFields = [];
	
        if (oldCategory === undefined) {
            return promises;
        } else if (newCategory === undefined) {
            diffFields = oldCategory.fields;
        } else {
          diffFields = oldCategory.fields.filter(function (obj) {
            return !newCategory.fields.some(function(obj2) {
        	return obj.id == obj2.id;
            });
          });
        }
		
	diffFields.forEach((field : Field) => {

          if (point.getProperty(field.id) != null) {
              console.log("Setting to null field: " + field.id );
              this.setPropertyToNull(row, field);
              let promise: IPromise<any> = this.saveNewValue(row, field, null);
              promises.push(promise);
	  }
	}
	
	return promises;
  } 
  
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
   * @param field
   * @param newValue
   */
  public saveNewValue(row: number, field: Field, newValue: any): IPromise<any> {
    let q: IDeferred<any> = this.$q.defer();
    let point: Point = this.request.points[row];

    // If there is no corresponding field in the schema, then it must be a "virtual"
    // column (such as "comment" or "checkbox")
    if (field == null) {
      q.resolve();
      return q.promise;
    }

    if (field.type === 'autocomplete') {
      // For autocomplete fields, re-query the values and manually save it back to the point.
      this.schemaService.queryFieldValues(field, newValue, point).then((values: any[]) => {

        values.forEach((item: any) => {
          let value: any = (field.model == null && typeof item === 'object') ? item.value : item[field.model];

          if (value === newValue) {
            console.log('saving new value');
            delete item._links;
            point.properties[field.id] = item;
          }
        });

        q.resolve();
      });
    } else {
      // For non-autocomplete fields, just manually save the new value.
      point.properties[field.id] = newValue;
      q.resolve();
    }

    return q.promise;
  }

  public onAfterCreateRow = (index: number, count: number, source: any): void => {
    this.request.points[index] = new Point().deserialize(this.request.points[index]);
    this.requestService.saveRequest(this.request);
  };

  public onAfterRemoveRow = (): void => {
    this.requestService.saveRequest(this.request);
  };
}
