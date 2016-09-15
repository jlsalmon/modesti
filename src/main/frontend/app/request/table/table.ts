import {RequestService} from '../request.service';
import {Request} from '../request';
import {Point} from '../point/point';
import {SchemaService} from '../../schema/schema.service';
import {Schema} from '../../schema/schema';
import {Category} from '../../schema/category/category';
import {Field} from '../../schema/field/field';
import {TaskService} from '../../task/task.service';
import IFilterService = angular.IFilterService;
import IQService = angular.IQService;

declare var Handsontable: any;

/**
 * The table knows nothing about schemas. It simply gets a list of column
 * definitions and a data array.
 */
export class Table {

  /** The handsontable instance */
  public hot: any;

  /** Settings object for handsontable */
  public settings: any = {
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

  public constructor(data: any[], columns: any[], private cellRenderer: Function) {
    this.settings.data = data;
    this.settings.columns = columns;
    this.settings.columns.forEach((column: any) => column.renderer = cellRenderer);
    this.hot = new Handsontable(document.getElementById('table'), this.settings);

    this.adjustTableHeight();
  }

  public reload(columns: any[] = []): void {
    if (columns) {
      this.settings.columns = columns;
      this.settings.columns.forEach((column: any) => column.renderer = this.cellRenderer);
      this.hot.updateSettings({
        columns: columns
      });
    }

    this.hot.loadData(this.settings.data);
  }

  public render(): void {
    this.hot.render();
  }

  public getSelectedLineNumbers = () => {
    if ($.isEmptyObject(this.hot)) {
      return [];
    }

    let checkboxes: any[] = this.hot.getDataAtProp('selected');
    let lineNumbers: number[] = [];

    for (let i: number = 0, len: number = checkboxes.length; i < len; i++) {
      if (checkboxes[i]) {
        // Line numbers are 1-based
        lineNumbers.push(this.settings.data[i].lineNo);
      }
    }

    return lineNumbers;
  };

  /**
   * Row headers can optionally contain a success/failure icon and a popover
   * message shown when the user hovers over the icon.
   *
   * TODO: remove this domain-specific code
   */
  public getRowHeader(row: number): any {
    let point: Point = this.settings.data[row];
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
    } else if (point.properties.cablingResult && point.properties.cablingResult.cabled === false
    && this.request.status === 'FOR_CABLING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-plug text-danger"></i></div>';
    } else if (point.properties.cablingResult && point.properties.cablingResult.cabled === true
    && this.request.status === 'FOR_CABLING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-plug text-success"></i></div>';
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

  /**
   * Make sure all the line numbers are consecutive
   */
  public normaliseLineNumbers(): void {
    for (let i: number = 0, len: number = this.settings.data.length; i < len; i++) {
      this.settings.data[i].lineNo = i + 1;
    }
  }

  /**
   * Adjust the height of the table so that it fills the entire space from
   * below the toolbar to above the footer.
   */
  public adjustTableHeight(): void {
    let mainHeader: JQuery = $('.main-header');
    let requestHeader: JQuery = $('.request-header');
    let toolbar: JQuery = $('.toolbar');
    let table: JQuery = $('.table');
    let footer: JQuery = $('.footer');

    let height: number = $(window).height() - mainHeader.outerHeight() - requestHeader.outerHeight()
                         - toolbar.outerHeight() - footer.outerHeight();

    table.height(height + 'px');
    this.hot.render();
  }
}
