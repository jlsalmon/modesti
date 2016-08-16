import {TableService} from './table.service';
import {RequestService} from '../request.service';
import {TaskService} from '../../task/task.service';
import {SchemaService} from '../../schema/schema.service';
import {Utils} from '../../utils/utils';

declare var $:any;

export class RequestTableComponent implements ng.IComponentOptions {
  public templateUrl:string = '/request/table/table.component.html';
  public controller:Function = RequestTableController;
  public bindings:any = {
    request: '=',
    tasks: '=',
    schema: '=',
    table: '=',
    activeCategory: '=',
    history: '='
  };
}

class RequestTableController {
  public static $inject:string[] = ['$scope', '$q', '$filter', '$localStorage', 'TableService', 'RequestService', 'TaskService', 'SchemaService', 'Utils'];

  /** The handsontable instance */
  public table:any = {};

  public request:any;
  public tasks:any;
  public schema:any;
  public activeCategory:any;
  public history:any;

  /** Settings object for handsontable */
  public settings:any = {
    contextMenu: ['row_above', 'row_below', '---------', 'remove_row', '---------', 'undo', 'redo'],
    stretchH: 'all',
    minSpareRows: 0,
    outsideClickDeselects: false,
    manualColumnResize: true,
    rowHeaders: function(row) { return _this.getRowHeader(row) },
    onAfterInit: function() { _this.afterInit(this) },
    afterRender: function() { _this.getAfterRenderFunction(this.request) },
    onBeforeChange: function(changes, source) { _this.beforeChange(changes, source) },
    onAfterChange: function(changes, source) { _this.afterChange(changes, source) },
    onAfterCreateRow: function() { _this.normaliseLineNumbers(this) },
    onAfterRemoveRow: function() { _this.normaliseLineNumbers(this) }
  };

  /** The columns that will be displayed for the currently active category. */
  public columns:any = [];

  public constructor(private $scope:any, private $q:any, private $filter:any, private $localStorage:any,
                     private tableService:TableService, private requestService:RequestService, private taskService:TaskService,
                     private schemService:SchemaService, private utils:Utils) {
    $localStorage.$default({
      lastActiveCategory: {}
    });
  }

  /**
   * Called when the handsontable table has finished initialising.
   */
  public afterInit(table) {
    console.log('afterInit()');

    // Save a reference to the handsontable instance and enhance it with some
    // extra utility methods.
    this.table = table;
    this.table.activateCategory = this.activateCategory;
    this.table.activateDefaultCategory = this.activateDefaultCategory;
    this.table.navigateToField = this.navigateToField;
    this.table.getSelectedLineNumbers = this.getSelectedLineNumbers;

    this.table.activateDefaultCategory();
    RequestTableController.adjustTableHeight();

    // Evaluate "editable" conditions for the active category. This is because
    // we need to evaluate the editability of individual cells based on the
    // value of other cells in the row, and we cannot do this in the table
    // service.
    // TODO: refactor this somewhere else
    this.table.updateSettings( {
      cells: (row, col, prop) => {
        if (typeof prop !== 'string') {
          return;
        }

        var task = this.taskService.getCurrentTask();

        var authorised = false;
        if (this.taskService.isCurrentUserAuthorised(task) && this.taskService.isCurrentUserAssigned(task)) {
          authorised = true;
        }

        var editable = false;
        if (authorised) {
          var point = this.request.points[row];

          // Evaluate "editable" condition of the category
          if (this.activeCategory.editable !== null && typeof this.activeCategory.editable === 'object') {
            var conditional = this.activeCategory.editable;

            if (conditional !== undefined && conditional !== null) {
              editable = this.schemService.evaluateConditional(point, conditional, this.request.status);
            }
          }

          // Evaluate "editable" condition of the field as it may override the category
          this.activeCategory.fields.forEach((field) => {
            if (field.id === prop.split('.')[1]) {
              var conditional = field.editable;

              if (conditional !== undefined && conditional !== null) {
                editable = this.schemService.evaluateConditional(point, conditional, this.request.status);
              }
            }
          });

          if (this.tableService.hasCheckboxColumn(this.request) && prop === 'selected') {
            editable = true;
          }
          else if (this.tableService.hasCommentColumn(this.request) && prop.contains('message')) {
            editable = true;
          }
        }

        return { readOnly: !editable };
      }
    });
  }
  
  public activateDefaultCategory = () => {
    var categoryId = this.$localStorage.lastActiveCategory[this.request.requestId];
    var category;

    if (!categoryId) {
      console.log('activating default category');
      category = this.schema.categories[0];
    } else {
      console.log('activating last active category: ' + categoryId);

      this.schema.categories.concat(this.schema.datasources).forEach((cat) => {
        if (cat.id === categoryId) {
          category = cat;
        }
      });

      if (!category) {
        category = this.schema.categories[0];
      }
    }

    this.activateCategory(category);
  }

  public activateCategory = (category) => {
    console.log('activating category "' + category.id + '"');
    this.activeCategory = category;
    this.$localStorage.lastActiveCategory[this.request.requestId] = category.id;
    this.refreshColumns(category);
  }

  /**
   * Note: currently ngHandsontable requires that columns be pushed into the
   * array after the table has been initialised. It does not accept a
   * function, nor will it accept an array returned from a function call.
   * See https://github.com/handsontable/handsontable/issues/590. Hopefully
   * this will be fixed in a later release.
   */
  public refreshColumns(category) {
    if (!category) {
      return;
    }

    this.columns.length = 0;

    var columns = this.tableService.getColumns(this.request, this.schema, category.fields, this.history);

    columns.forEach((column) => {
      this.columns.push(column);
    });

    this.table.render();
  }

  /**
   * Row headers can optionally contain a success/failure icon and a popover
   * message shown when the user hovers over the icon.
   *
   * TODO: remove this domain-specific code
   *
   * @param row
   * @returns {*}
   */
  public getRowHeader(row) {
    var point = this.request.points[row];
    var text = '';

    if (point && point.valid === false) {
      point.errors.forEach((e) => {
        e.errors.forEach((error) => {
          text += '<i class="fa fa-fw fa-exclamation-circle text-danger"></i> ' + error + '<br />';
        });
      });

      return '<div class="row-header" data-container="body" data-toggle="popover" data-placement="right" data-html="true" data-content="' +
          text.replace(/"/g, '&quot;') + '">' + point.lineNo + ' <i class="fa fa-exclamation-circle text-danger"></i></div>';
    }
    //else if (point.properties.valid === true) {
    //  return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-check-circle text-success"></i></div>';
    //}

    else if (point.properties.approvalResult && point.properties.approvalResult.approved === false) {
      text = 'Operator comment: <b>' + point.properties.approvalResult.message + '</b>';
      return '<div class="row-header" data-container="body" data-toggle="popover" data-placement="right" data-html="true" data-content="' +
          text.replace(/"/g, '&quot;') + '">' + point.lineNo + ' <i class="fa fa-comments text-yellow"></i></div>';
    }

    else if (point.properties.approvalResult && point.properties.approvalResult.approved === true && this.request.status === 'FOR_APPROVAL') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-check-circle text-success"></i></div>';
    }

    else if (point.properties.cablingResult && point.properties.cablingResult.cabled === false && this.request.status === 'FOR_CABLING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-plug text-danger"></i></div>';
    }

    else if (point.properties.cablingResult && point.properties.cablingResult.cabled === true && this.request.status === 'FOR_CABLING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-plug text-success"></i></div>';
    }

    else if (point.properties.testResult && point.properties.testResult.passed === false && this.request.status === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-times-circle text-danger"></i></div>';
    }

    else if (point.properties.testResult && point.properties.testResult.passed === true && this.request.status === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-check-circle text-success"></i></div>';
    }

    else if (point.properties.testResult && point.properties.testResult.postponed === true && this.request.status === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-minus-circle text-muted"></i></div>';
    }

    return point.lineNo;
  }

  /**
   * Slightly hacky little function to make sure all the elements on the page
   * are properly initialised.
   */
  public getAfterRenderFunction(request) {
    return () => {

      // Initialise the popovers in the row headers
      $('.row-header').popover({trigger: 'hover', delay: {'show': 100, 'hide': 100}});

      // Initialise the help text popovers on the column headers
      $('.help-text').popover({trigger: 'hover', delay: {'show': 500, 'hide': 100}});

      if (this.tableService.hasCheckboxColumn(request)) {

        var firstColumnHeader = $('.htCore colgroup col.rowHeader');
        var lastColumnHeader = $('.htCore colgroup col:last-child');
        var checkboxColumn = $('.htCore colgroup col:nth-child(2)');

        // Fix the width of the 'select-all' checkbox column (second column) and add the surplus to the last column
        var lastColumnHeaderWidth = lastColumnHeader.width() + (checkboxColumn.width() - 30);
        lastColumnHeader.width(lastColumnHeaderWidth);
        checkboxColumn.width('30px');

        // Fix the width of the first column (point id column)
        firstColumnHeader.width('45px');

        // Centre checkboxes
        var checkboxCells = $('.htCore input.htCheckboxRendererInput').parent();
        checkboxCells.css('text-align', 'center');

        // Initialise checkbox header state
        var checkboxHeader = $('.select-all:checkbox');
        checkboxHeader.prop(this.getCheckboxHeaderState(), true);

        var header, cells;
        if (this.tableService.hasCommentColumn(request)) {
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
          for (var i = 0, len = this.request.points.length; i < len; i++) {
            this.request.points[i].selected = this.checked;
          }

          // Need to explicitly trigger a digest loop here because we are out of the angularjs world and in the happy land
          // of jquery hacking
          this.$scope.$apply();
        });

        // Listen for change events on all checkboxes
        $('.htCheckboxRendererInput:checkbox').change(() => {
          $('.select-all:checkbox').prop(this.getCheckboxHeaderState(), true);
        });
      }
    };
  }
  
  public getCheckboxHeaderState() {
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
   * Called before a change is made to the table.
   *
   * @param changes a 2D array containing information about each of the edited
   *                cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: 'alter', 'empty', 'edit',
   *               'populateFromArray', 'loadData', 'autofill', 'paste'
   */
  public beforeChange(changes, source) {
    if (source === 'loadData') {
      return;
    }

    var change, row, property, oldValue, newValue;
    for (var i = 0, ilen = changes.length; i < ilen; i++) {
      change = changes[i];
      row = change[0];
      property = change[1];
      oldValue = change[2];
      newValue = change[3];

      if (typeof property !== 'string') {
        continue;
      }

      // get the outer object i.e. properties.location.value -> location
      var prop = property.split('.')[1];

      for (var j = 0, jlen = this.activeCategory.fields.length; j < jlen; j++) {
        var field = this.activeCategory.fields[j];

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
   * Called after a change is made to the table (edit, paste, etc.)
   *
   * @param changes a 2D array containing information about each of the edited
   *                cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: "alter", "empty", "edit",
   *               "populateFromArray", "loadData", "autofill", "paste"
   */
  public afterChange(changes, source) {
    // When the table is initially loaded, this callback is invoked with
    // source === 'loadData'. In that case, we don't want to do anything.
    if (source === 'loadData') {
      return;
    }

    console.log('afterChange()');

    // Make sure the line numbers are consecutive
    this.request.points.forEach((row, i) => {
      row.lineNo = i + 1;
    });

    var promises = [];

    // Loop over the changes and check if anything actually changed. Mark any changed points as dirty.
    var change, row, property, oldValue, newValue, dirty = false;
    for (var i = 0, len = changes.length; i < len; i++) {
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
      if (newValue === undefined || newValue === null || newValue === '') {
        //var point = this.parent.hot.getSourceDataAtRow(row);
        var point = this.request.points[row];
        var propName = property.split('.')[1];

        var prop = point.properties[propName];

        if (typeof prop === 'object') {
          for (var attribute in prop) {
            if (prop.hasOwnProperty(attribute)) {
              prop[attribute] = null;
            }
          }
        } else {
          prop = null;
        }

      }

      // This is a workaround. See function documentation for info.
      var promise = this.saveNewValue(row, property, newValue);
      promises.push(promise);
    }

    // Wait for all new values to be updated
    this.$q.all(promises).then(() => {

      // If nothing changed, there's nothing to do! Otherwise, save the request.
      if (dirty) {
        this.request.valid = false;

        this.requestService.saveRequest(this.request).then(() => {

          // Reload the history
          this.requestService.getRequestHistory(this.request.requestId).then((history) => {
            this.history = history;
            this.table.render();
          });
        });
      }
    });
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
   * @param property
   * @param newValue
   */
  public saveNewValue(row, property, newValue) {
    var q = this.$q.defer();
    var point = this.request.points[row];
    var outerProp;

    if (typeof property === 'string') {
      // get the outer object i.e. properties.location.value -> location
      outerProp = property.split('.')[1];
    } else {
      outerProp = this.activeCategory.fields[property].id;
    }

    var field = this.utils.getField(this.schema, outerProp);

    // If there is no corresponding field in the schema, then it must be a "virtual"
    // column (such as "comment" or "checkbox")
    if (field === undefined) {
      q.resolve();
      return q.promise;
    }

    // For autocomplete fields, re-query the values and manually save it back to the point.
    if (field.type === 'autocomplete') {
      this.schemService.queryFieldValues(field, newValue, point).then((values) => {

        values.forEach((item) => {
          var value = (field.model === undefined && typeof item === 'object') ? item.value : item[field.model];

          if (value === newValue) {
            console.log('saving new value');
            delete item._links;
            point.properties[outerProp] = item;
          }
        });

        q.resolve();
      });
    }

    // For non-autocomplete fields, just manually save the new value.
    else {
      point.properties[outerProp] = newValue;
      q.resolve();
    }

    return q.promise;
  }

  public normaliseLineNumbers() {
    for (var i = 0, len = this.request.points.length; i < len; i++) {
      this.request.points[i].lineNo = i + 1;
    }
  }

  public getSelectedLineNumbers = () => {
    if ($.isEmptyObject(this.table)) {
      return [];
    }

    var checkboxes = this.table.getDataAtProp('selected');
    var lineNumbers = [];

    for (var i = 0, len = checkboxes.length; i < len; i++) {
      if (checkboxes[i]) {
        // Line numbers are 1-based
        lineNumbers.push(this.request.points[i].lineNo);
      }
    }

    return lineNumbers;
  }

  /**
   * Navigate somewhere to focus on a particular field.
   *
   * @param categoryName
   * @param fieldId
   */
  public navigateToField = (categoryName, fieldId) => {

    // Find the category which contains the field.
    var category;

    if (fieldId.indexOf('.') !== -1) {
      fieldId = fieldId.split('.')[0];
    }

    this.schema.categories.concat(this.schema.datasources).forEach((cat) => {
      if (cat.name === categoryName || cat.id === categoryName) {
        cat.fields.forEach((field) => {
          if (field.id === fieldId || cat.name === fieldId || cat.id === fieldId) {
            category = cat;
          }
        });
      }
    });

    if (category) {
      this.table.activateCategory(category);
    }
  }

  /**
   * Adjust the height of the table so that it fills the entire space from
   * below the toolbar to above the footer.
   */
  public static adjustTableHeight() {
    var mainHeader = $('.main-header');
    var requestHeader = $('.request-header');
    var toolbar = $('.toolbar');
    var table = $('.table');
    var footer = $('.footer');

    var height = $(window).height() - mainHeader.outerHeight() -
      requestHeader.outerHeight() - toolbar.outerHeight() - footer.outerHeight();

    table.height(height + 'px');
  }
}