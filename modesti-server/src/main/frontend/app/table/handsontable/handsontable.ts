import {Table} from '../table';
import {CopyPasteAware} from '../copy-paste-aware';
import {UndoRedoAware} from '../undo-redo-aware';
import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {Conditional} from '../../schema/conditional';
import {SchemaService} from '../../schema/schema.service';
import {TaskService} from '../../task/task.service';
import {ColumnFactory} from '../column-factory';
import {Point} from '../../request/point/point';

import 'latinize';
import {ContextMenuFactory} from "./context-menu-factory";
import {IInterpolateService} from 'angular';
declare var latinize: any;

// TODO: import this properly without require()
let Handsontable: any = require('handsontable-pro');


export class HandsonTable extends Table implements CopyPasteAware, UndoRedoAware {

  public hot: Handsontable.Core;
  public hotOptions: any;
  public hiddenColumnsPlugin: any;
  public schemaService: SchemaService;
  public taskService: TaskService;
  public interpolate: IInterpolateService;

  public constructor(schema: Schema, data: any[], settings: any) {
    super(schema, data, settings);
    // Save references to services
    this.schemaService = settings.schemaService;
    this.taskService = settings.taskService;
    this.interpolate = settings.interpolate;

    let columnDefs: any[] = this.getColumnDefs();

    this.hotOptions = {
      data: data,
      columns: columnDefs,
      hiddenColumns: {
        columns: this.determineInitialHiddenColumns(columnDefs)
      },
      fixedColumnsLeft: this.determineNumFixedColumns(),
      contextMenu: ContextMenuFactory.getContextMenu(settings.requestType, settings.requestStatus),
      stretchH: 'all',
      minSpareRows: 0,
      undo: true,
      outsideClickDeselects: false,
      manualColumnResize: true,
      rowHeaders: (row: any) => this.getRowHeader(row),
      beforeChange: (changes: any, source: any) => this.beforeChange(changes, source),
      afterCreateRow: () => this.normaliseLineNumbers(),
      afterRemoveRow: () => this.normaliseLineNumbers()
    };

    this.hot = new Handsontable(document.getElementById('table'), this.hotOptions);
    this.hiddenColumnsPlugin = this.hot.getPlugin('hiddenColumns');

    // Map and register hooks from the external settings
    this.hot.addHook('afterChange', settings.afterChange);
    this.hot.addHook('afterRender', settings.afterRender);
    this.hot.addHook('afterCreateRow', settings.afterCreateRow);
    this.hot.addHook('afterRemoveRow', settings.afterRemoveRow);

    // Make sure the table fills the container height
    this.adjustTableHeight();

    this.hot.updateSettings({
      cells: this.evaluateCellSettings,
      maxRows: settings.requestStatus === 'IN_PROGRESS' ? undefined : data.length
    });

    // Trigger an initial render
    this.render();
  }

  /**
   * Evaluate "editable" state of each cell
   */
  public evaluateCellSettings = (row: number, col: number, prop: any) => {
    if (typeof prop !== 'string') {
      return;
    }

    let editable: boolean = false;
    let assigned: boolean = this.taskService.isCurrentUserAssigned();
    let point: Point = this.data[row];
    let field: Field = this.schema.getField(prop);

    if (assigned && field != null) {

      // Evaluate "editable" condition of the category
      let category: Category = this.schema.getCategoryForField(field);
      let categoryConditional: Conditional = category.editable;

      if (categoryConditional != null) {
        editable = this.settings.schemaService.evaluateConditional(point, categoryConditional, this.settings.requestStatus);
      }

      // Evaluate "editable" condition of the field as it may override the category
      let fieldConditional: Conditional = field.editable;

      if (fieldConditional != null) {

        // If the category-level conditional specifies status(es) and the field-level
        // conditional does not, take the category-level statuses into account relative
        // to the field-level conditional
        if (categoryConditional.status && !fieldConditional.status) {
          fieldConditional = {
            status: categoryConditional.status,
            condition: fieldConditional
          }
        }

        editable = this.settings.schemaService.evaluateConditional(point, fieldConditional, this.settings.requestStatus);
      }
    }

    if (this.schema.hasRowSelectColumn(this.settings.requestStatus) && prop === 'selected') {
      editable = true;
    } else if (this.schema.hasRowCommentColumn(this.settings.requestStatus) && prop.contains('message')) {
      editable = true;
    }

    return { readOnly: !editable };
  };

  public canAddRows(): boolean {
    return this.settings.requestStatus === 'IN_PROGRESS' && this.settings.requestType === 'CREATE';
  }

  public determineInitialHiddenColumns(columnDefs: any[]): number[] {
    let hiddenColumns: number[] = [];

    // We may have select and/or comment columns, so offset those
    // let offset: number = this.determineNumFixedColumns();

    //if (this.state.getHiddenColumns().length > 0) {
      // If the table state holds a list of hidden columns, use that
      //columnDefs.forEach((columnDef: any, index: number) => {
      //  if (this.state.getHiddenColumns().indexOf(columnDef.field.id) === -1) {
      //    hiddenColumns.push(index);
      //  }
      //});

    //} else {
      // Otherwise, initially show only the first category
      let firstCategory: Category = this.schema.categories[0];
      columnDefs.forEach((columnDef: any, index: number) => {
        if (columnDef.data === 'selected' || columnDef.data.endsWith('message')) {
          return;
        }

        if (firstCategory.fields.indexOf(columnDef.field) === -1) {
          hiddenColumns.push(index);
        }
      });
    //}

    return hiddenColumns;
  }

  public determineNumFixedColumns(): number {
    let numFixedColumns: number = 0;

    this.schema.getAllFields().forEach((field: Field) => {
      if (field.fixed === true) {
        numFixedColumns++;
      }
    });

    let assigned: boolean = this.taskService.isCurrentUserAssigned();
    if (assigned) {
      if (this.schema.hasRowSelectColumn(this.settings.requestStatus)) {
        numFixedColumns++;
      }

      if (this.schema.hasRowCommentColumn(this.settings.requestStatus)) {
        numFixedColumns++;
      }
    }

    return numFixedColumns;
  }

  public refreshData(): void {
    return;
  }

  public refreshColumnDefs(): void {
    let columnDefs: any[] = this.getColumnDefs();

    this.hot.updateSettings({
      columns: columnDefs,
      hiddenColumns: {
        columns: this.determineInitialHiddenColumns(columnDefs)
      },
      fixedColumnsLeft: this.determineNumFixedColumns()
    });

    this.render();
  }

  public render(): void {
    this.hot.render();
  }

  public showColumn(field: Field): void {
    this.hiddenColumnsPlugin.showColumn(this.getColumnIndex(field));
    this.render();
  }

  public hideColumn(field: Field): void {
    if (!field.fixed) {
      this.hiddenColumnsPlugin.hideColumn(this.getColumnIndex(field));
      this.render();
    }
  }

  public toggleColumn(field: Field): void {
    let colIndex: number = this.getColumnIndex(field);
    if (this.hiddenColumnsPlugin.isHidden(colIndex)) {
      this.hiddenColumnsPlugin.showColumn(colIndex);
    } else {
      this.hiddenColumnsPlugin.hideColumn(colIndex);
    }
    this.render();
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

    // Render twice, because handsontable craps itself if you hide all columns
    // and then show some again
    this.render();
    this.render();
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
      authorised: this.taskService.isCurrentUserAuthorised(),
      assigned: this.taskService.isCurrentUserAssigned(),
      schemaService: this.schemaService,
      interpolate: this.interpolate
    };

    let columnDefs: any[] = ColumnFactory.getColumnDefinitions('handsontable', this, meta);
    columnDefs.forEach((column: any) => column.renderer = this.settings.cellRenderer);
    return columnDefs;
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

  public isInvalidField(field: Field): boolean {
    let invalid: boolean = false;

    this.data.forEach((point: Point) => {
      if (point.errors && point.errors.length > 0) {
        point.errors.forEach((error: any) => {

          if (error.property) {
            let property: string = error.property.split('.')[0];

            if (field.id === property) {
              invalid = true;
            }
          }
        });
      }
    });

    return invalid;
  }

  /**
   * Return the line numbers of all rows that are currently selected (i.e.
   * the current state is a "selectable" state and the user has selected
   * the checkbox in the first column for that row).
   *
   * @returns {Array}
   */
  public getSelectedLineNumbers(): number[] {
    var checkboxes = this.hot.getDataAtProp('selected');
    var lineNumbers = [];

    for (var i = 0, len = checkboxes.length; i < len; i++) {
      if (checkboxes[i]) {
        // Line numbers are 1-based
        lineNumbers.push(this.data[i].lineNo);
      }
    }

    return lineNumbers;
  }

  /**
   * Make sure all the line numbers are consecutive
   */
  private normaliseLineNumbers(): void {
    for (let i: number = 0, len: number = this.data.length; i < len; i++) {
      this.data[i].lineNo = i + 1;
    }
  }

  public undo(): void {
    this.hot.undo();
  }

  public redo(): void {
    this.hot.redo();
  }

  public cut(): void {
    this.hot.copyPaste.triggerCut();
  }

  public copy(): void {
    this.hot.copyPaste.setCopyableText();
  }

  public paste(): void {
    this.hot.copyPaste.triggerPaste();
    this.hot.copyPaste.copyPasteInstance.onPaste((value: any) => {
      console.log('onPaste(): ' + value);
    });
  }

  /**
   * Navigate somewhere to highlight a particular cell.
   *
   * @param categoryName the name of the category to which the field belongs
   * @param fieldId the id of the field to focus on
   * @param lineNo the row to be highlighted
   */
  public highlightCell = (categoryName: string, fieldId: string, lineNo: number) => {
    let field: Field = this.schema.getField(fieldId);

    // Get the column number from the field id
    let col: number = this.hot.propToCol('properties.' + field.getModelPath());

    // Make sure the category is visible
    if (!this.isVisibleColumn(field)) {
      // Find the category which contains the field
      let category: Category = this.schema.getCategoryForField(field);
      this.toggleColumnGroup(category.fields);
    }

    this.hot.selectCell(lineNo - 1, col);
  };

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
      && this.settings.requestStatus === 'FOR_APPROVAL') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-check-circle text-success"></i></div>';
    } else if (point.properties.testResult && point.properties.testResult.passed === false
      && this.settings.requestStatus === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-times-circle text-danger"></i></div>';
    } else if (point.properties.testResult && point.properties.testResult.passed === true
      && this.settings.requestStatus === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-check-circle text-success"></i></div>';
    } else if (point.properties.testResult && point.properties.testResult.postponed === true
      && this.settings.requestStatus === 'FOR_TESTING') {
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

      if (typeof newValue !== 'string' || newValue == null) {
        continue;
      }

      let field: Field = this.schema.getField(property);
      if (field) {
        // Remove accented characters
        newValue = latinize(newValue);

        // Force uppercase if necessary
        if (field.uppercase === true) {
          newValue = newValue.toUpperCase();
        }

        changes[i][3] = newValue;
      }
    }
  }
}