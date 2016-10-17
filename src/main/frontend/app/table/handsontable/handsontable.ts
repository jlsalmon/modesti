import {Table} from '../table';
import {TableState} from '../table-state';
import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {ColumnFactory} from '../column-factory';
import {Point} from '../../request/point/point';
import './select2-editor.ts';

// TODO: import this properly without require()
let Handsontable: any = require('handsontable-pro');


export class HandsonTable extends Table {

  public hot: Handsontable.Core;
  public hotOptions: any;
  public hiddenColumnsPlugin: any;

  public constructor(schema: Schema, data: any[], state: TableState, settings: any) {
    super(schema, data, state, settings);

    let columnDefs: any[] = this.getColumnDefs();
    columnDefs.forEach((column: any) => column.renderer = settings.cellRenderer);

    this.hotOptions = {
      data: data,
      columns: columnDefs,
      hiddenColumns: {
        columns: this.determineInitialHiddenColumns(columnDefs)
      },
      fixedColumnsLeft: this.determineNumFixedColumns(),
      contextMenu: ['row_above', 'row_below', '---------', 'remove_row', '---------', 'undo', 'redo'],
      stretchH: 'all',
      minSpareRows: 0,
      undo: true,
      outsideClickDeselects: false,
      manualColumnResize: true,
      rowHeaders: (row: any) => this.getRowHeader(row),
      onBeforeChange: (changes: any, source: any) => this.beforeChange(changes, source),
      onAfterCreateRow: () => this.normaliseLineNumbers(),
      onAfterRemoveRow: () => this.normaliseLineNumbers()
    };

    this.hot = new Handsontable(document.getElementById('table'), this.hotOptions);
    this.hiddenColumnsPlugin = this.hot.getPlugin('hiddenColumns');

    // Map and register hooks from the external settings
    this.hot.addHook('afterChange', settings.afterChange);
    this.hot.addHook('afterRender', settings.afterRender);

    // Make sure the table fills the container height
    this.adjustTableHeight();

    // Trigger an initial render
    this.hot.render();
  }

  public determineInitialHiddenColumns(columnDefs: any[]): number[] {
    let hiddenColumns: number[] = [];

    if (this.state.getHiddenColumns().length > 0) {
      // If the table state holds a list of hidden columns, use that
      columnDefs.forEach((columnDef: any, index: number) => {
        if (this.state.getHiddenColumns().indexOf(columnDef.field.id) === -1) {
          hiddenColumns.push(index);
        }
      });

    } else {
      // Otherwise, initially show only the first category
      let firstCategory: Category = this.schema.categories[0];
      columnDefs.forEach((columnDef: any, index: number) => {
        if (firstCategory.fields.indexOf(columnDef.field) === -1) {
          hiddenColumns.push(index);
        }
      });
    }

    return hiddenColumns;
  }

  public determineNumFixedColumns(): number {
    let numFixedColumns: number = 0;

    this.schema.getAllFields().forEach((field: Field) => {
      if (field.fixed === true) {
        numFixedColumns++;
      }
    });

    if (this.schema.hasRowSelectColumn(this.settings.requestStatus)) {
      numFixedColumns++;
    }

    if (this.schema.hasRowCommentColumn(this.settings.requestStatus)) {
      numFixedColumns++;
    }

    return numFixedColumns;
  }

  public refreshData(): void {}

  public refreshColumnDefs(): void {}

  public showColumn(field: Field): void {
    this.hiddenColumnsPlugin.showColumn(this.getColumnIndex(field));
    this.hot.render();
  }

  public hideColumn(field: Field): void {
    if (!field.fixed) {
      this.hiddenColumnsPlugin.hideColumn(this.getColumnIndex(field));
      this.hot.render();
    }
  }

  public toggleColumn(field: Field): void {
    let colIndex: number = this.getColumnIndex(field);
    if (this.hiddenColumnsPlugin.isHidden(colIndex)) {
      this.hiddenColumnsPlugin.showColumn(colIndex);
    } else {
      this.hiddenColumnsPlugin.hideColumn(colIndex);
    }
    this.hot.render();
  }

  public isVisibleColumn(field: Field): boolean {
    return !this.hiddenColumnsPlugin.isHidden(this.getColumnIndex(field));
  }

  public toggleColumnGroup(fields: Field[]): void {
    let columnIndices: number[] = fields.map((field: Field) => {
      if (!field.fixed) {
        return this.getColumnIndex(field);
      }
    });

    if (this.isVisibleColumnGroup(fields)) {
      this.hiddenColumnsPlugin.hideColumns(columnIndices);
    } else {
      this.hiddenColumnsPlugin.showColumns(columnIndices);
    }
    this.hot.render();
  }

  public isVisibleColumnGroup(fields: Field[]): boolean {
    let visible: boolean = true;

    fields.forEach((field: Field) => {
      if (this.hiddenColumnsPlugin.isHidden(this.getColumnIndex(field))) {
        visible = false;
        return;
      }
    });

    return visible;
  }

  private getColumnIndex(field: Field): number {
    return this.hotOptions.columns.indexOf(this.getColumn(field));
  }

  private getColumnDefs(): any[] {
    let meta: any = {
      requestStatus: this.settings.requestStatus,
      authorised: this.settings.authorised,
      schemaService: this.settings.schemaService
    };

    return ColumnFactory.getColumnDefinitions('handsontable', this, meta);
  }

  private getColumn(field: Field): any {
    let column: any;

    this.hotOptions.columns.forEach((col: any) => {
      if (col.field && col.field.id === field.id) {
        column = col;
      }
    });

    return column;
  }

  public getActiveDatasources(): Category[] {
    let result: Category[] = [];

    this.data.forEach((point: Point) => {
      this.schema.datasources.forEach((datasource: Category) => {

        if (point.properties.pointType &&
        (point.properties.pointType === angular.uppercase(datasource.id)
        || point.properties.pointType === angular.uppercase(datasource.name))) {
          if (result.indexOf(datasource) === -1) {
            result.push(datasource);
          }
        }
      });
    });

    return result;
  }

  /**
   * Return true if the given category is "invalid", i.e. there are points in
   * the current request that have errors that relate to the category.
   *
   * @param category
   */
  public isInvalidCategory(category: Category): boolean {
    let fieldIds: string[] = category.fields.map((field: Field) => field.id);
    let invalid: boolean = false;

    this.data.forEach((point: Point) => {
      if (point.errors && point.errors.length > 0) {
        point.errors.forEach((error: any) => {
          if (!error.category) {
            let property: string = error.property.split('.')[0];

            if (fieldIds.indexOf(property) !== -1) {
              invalid = true;
            }
          } else if (error.category === category.name || error.category === category.id) {
            invalid = true;
          }
        });
      }
    });

    return invalid;
  }

  /**
   * Make sure all the line numbers are consecutive
   */
  private normaliseLineNumbers(): void {
    for (let i: number = 0, len: number = this.settings.data.length; i < len; i++) {
      this.settings.data[i].lineNo = i + 1;
    }
  }

  /**
   * Adjust the height of the table so that it fills the entire space from
   * below the toolbar to above the footer.
   */
  private adjustTableHeight(): void {
    let mainHeader: JQuery = $('.main-header');
    let requestHeader: JQuery = $('.request-header');
    let toolbar: JQuery = $('.toolbar');
    let table: JQuery = $('.table');
    let footer: JQuery = $('.footer');

    let height: number = $(window).height() - mainHeader.outerHeight() - requestHeader.outerHeight()
      - toolbar.outerHeight() - footer.outerHeight();

    table.height(height + 'px');
  }

  /**
   * Row headers can optionally contain a success/failure icon and a popover
   * message shown when the user hovers over the icon.
   *
   * TODO: remove this domain-specific code
   */
  private getRowHeader(row: number): any {
    let point: Point = this.data[row];
    let text: string = '';

    if (point && point.valid === false) {
      point.errors.forEach((e: any) => {
        e.errors.forEach((error: string) => {
          text += '<i class="fa fa-fw fa-exclamation-circle text-danger"></i> ' + error + '<br />';
        });
      });

      return '<div class="row-header" data-container="body" data-toggle="popover" data-placement="right" '
        + 'data-html="true" data-content="' + text.replace(/"/g, '&quot;') + '">' + point.lineNo
        + ' <i class="fa fa-exclamation-circle text-danger"></i></div>';

      // TODO: add a row header callback function for plugins

    } else if (point.properties.approvalResult && point.properties.approvalResult.approved === false) {
      text = 'Operator comment: <b>' + point.properties.approvalResult.message + '</b>';
      return '<div class="row-header" data-container="body" data-toggle="popover" data-placement="right" '
        + 'data-html="true" data-content="' + text.replace(/"/g, '&quot;') + '">' + point.lineNo
        + ' <i class="fa fa-comments text-yellow"></i></div>';
    } else if (point.properties.approvalResult && point.properties.approvalResult.approved === true
      && this.request.status === 'FOR_APPROVAL') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-check-circle text-success"></i></div>';
    } else if (point.properties.testResult && point.properties.testResult.passed === false
      && this.request.status === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-times-circle text-danger"></i></div>';
    } else if (point.properties.testResult && point.properties.testResult.passed === true
      && this.request.status === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-check-circle text-success"></i></div>';
    } else if (point.properties.testResult && point.properties.testResult.postponed === true
      && this.request.status === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-minus-circle text-muted"></i></div>';
    }

    return point.lineNo;
  }

  /**
   * Called before a change is made to the table.
   *
   * @param changes a 2D array containing information about each of the edited
   *                cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: 'alter', 'empty', 'edit',
   *               'populateFromArray', 'loadData', 'autofill', 'paste'
   */
  public beforeChange(changes: any[], source: string): void {
    if (source === 'loadData') {
      return;
    }

    let change: any, row: number, property: string, oldValue: any, newValue: any;
    for (let i: number = 0, ilen: number = changes.length; i < ilen; i++) {
      change = changes[i];
      row = change[0];
      property = change[1];
      oldValue = change[2];
      newValue = change[3];

      if (typeof property !== 'string') {
        continue;
      }

      // get the outer object i.e. properties.location.value -> location
      let prop: string = property.split('.')[1];

      for (let j: number = 0, jlen: number = this.settings.columns.length; j < jlen; j++) {
        let field: Field = this.settings.columns[j].field;

        if (field.id === prop) {

          // Remove accented characters
          newValue = this.$filter('latinize')(newValue);

          // Force uppercase if necessary
          if (field.uppercase === true) {
            newValue = this.$filter('uppercase')(newValue);
          }

          changes[i][3] = newValue;
          break;
        }
      }
    }
  }
}
