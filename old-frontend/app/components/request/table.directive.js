'use strict';

angular.module('modesti').directive('requestTable', function RequestTableDirective() {
  return {
    controller: RequestTableController,
    controllerAs: 'ctrl',
    templateUrl: 'components/request/table.directive.html',
    scope: {},
    bindToController: {
      request: '=',
      tasks: '=',
      schema: '=',
      table: '=',
      activeCategory: '='
    },
    link: function () {
      /**
       * Calculate the required height for the table so that it fills the screen.
       */
      function adjustTableHeight() {
        var mainHeader = $('.main-header');
        var requestHeader = $('.request-header');
        var toolbar = $('.toolbar');
        var table = $('.table');
        var footer = $('.footer');

        var height = $(window).height() - mainHeader.outerHeight() -
          requestHeader.outerHeight() - toolbar.outerHeight() - footer.outerHeight();

        table.height(height + 'px');
      }

      adjustTableHeight();
    }
  };
});

function RequestTableController($scope, $q, $filter, $localStorage, TableService, RequestService, TaskService, SchemaService, Utils) {
  var self = this;

  /** The handsontable instance */
  self.table = {};

  /** Settings object for handsontable */
  self.settings = {
    rowHeaders: getRowHeaders,
    //colHeaders: true,
    contextMenu: ['row_above', 'row_below', '---------', 'remove_row', '---------', 'undo', 'redo'],
    stretchH: 'all',
    // To enable sorting, a mapping needs to be done from the source array to the displayed array
    columnSorting: false,
    //currentRowClassName: 'currentRow',
    //comments: true,
    minSpareRows: 0,
    search: true,
    // pasteMode: 'shift_down', // problematic
    outsideClickDeselects: false,
    manualColumnResize: true,
    //manualRowMove: true,
    afterInit: afterInit,
    afterRender: getAfterRenderFunction(self.request),
    beforeChange: beforeChange,
    afterChange: afterChange,
    afterCreateRow: normaliseLineNumbers,
    afterRemoveRow: normaliseLineNumbers
  };

  /** The columns that will be displayed for the currently active category. */
  self.columns = [];

  $localStorage.$default({
    lastActiveCategory: {}
  });

  /**
   * Called when the handsontable table has finished initialising.
   */
  function afterInit() {
    /*jshint validthis:true */
    console.log('afterInit()');

    // Save a reference to the handsontable instance and enhance it with some
    // extra utility methods.
    self.table = this;
    self.table.activateCategory = activateCategory;
    self.table.activateDefaultCategory = activateDefaultCategory;
    self.table.navigateToField = navigateToField;
    self.table.getSelectedLineNumbers = getSelectedLineNumbers;

    self.table.activateDefaultCategory();

    // Evaluate "editable" conditions for the active category. This is because
    // we need to evaluate the editability of individual cells based on the
    // value of other cells in the row, and we cannot do this in the table
    // service.
    // TODO: refactor this somewhere else
    self.table.updateSettings( {
      cells: function (row, col, prop) {
        if (typeof prop !== 'string') {
          return;
        }

        var task = TaskService.getCurrentTask();

        var authorised = false;
        if (TaskService.isCurrentUserAuthorised(task) && TaskService.isCurrentUserAssigned(task)) {
          authorised = true;
        }

        var editable = false;
        if (authorised) {
          var point = self.request.points[row];

          // Evaluate "editable" condition of the category
          if (self.activeCategory.editable !== null && typeof self.activeCategory.editable === 'object') {
            var conditional = self.activeCategory.editable;

            if (conditional !== undefined && conditional !== null) {
              editable = SchemaService.evaluateConditional(point, conditional, self.request.status);
            }
          }

          // Evaluate "editable" condition of the field as it may override the category
          self.activeCategory.fields.forEach(function (field) {
            if (field.id === prop.split('.')[1]) {
              var conditional = field.editable;

              if (conditional !== undefined && conditional !== null) {
                editable = SchemaService.evaluateConditional(point, conditional, self.request.status);
              }
            }
          });

          if (TableService.hasCheckboxColumn(self.request) && prop === 'selected') {
            editable = true;
          }
          else if (TableService.hasCommentColumn(self.request) && prop.contains('message')) {
            editable = true;
          }
        }

        return { readOnly: !editable };
      }
    });
  }

  ///**
  // * Watch the active category for changes and refresh the columns of the table
  // */
  //$scope.$watch('ctrl.activeCategory', function handleActiveCategoryChange(newValue) {
  //  refreshColumns(newValue);
  //}, true);

  /**
   * Note: currently ngHandsontable requires that columns be pushed into the
   * array after the table has been initialised. It does not accept a
   * function, nor will it accept an array returned from a function call.
   * See https://github.com/handsontable/handsontable/issues/590. Hopefully
   * this will be fixed in a later release.
   */
  function refreshColumns(category) {
    if (!category) {
      return;
    }

    self.columns.length = 0;

    var columns = TableService.getColumns(self.request, self.schema, category.fields);

    columns.forEach(function (column) {
      self.columns.push(column);
    });
  }

  /**
   *
   */
  function activateDefaultCategory() {
    var categoryId = $localStorage.lastActiveCategory[self.request.requestId];
    var category;

    if (!categoryId) {
      console.log('activating default category');
      category = self.schema.categories[0];
    } else {
      console.log('activating last active category: ' + categoryId);

      self.schema.categories.concat(self.schema.datasources).forEach(function (cat) {
        if (cat.id === categoryId) {
          category = cat;
        }
      });

      if (!category) {
        category = self.schema.categories[0];
      }
    }

    activateCategory(category);
  }

  /**
   *
   * @param category
   */
  function activateCategory(category) {
    console.log('activating category "' + category.id + '"');
    self.activeCategory = category;
    $localStorage.lastActiveCategory[self.request.requestId] = category.id;
    refreshColumns(category);
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
  function getRowHeaders(row) {
    var point = self.request.points[row];
    var text = '';

    if (point && point.valid === false) {
      point.errors.forEach(function (e) {
        e.errors.forEach(function (error) {
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

    else if (point.properties.approvalResult && point.properties.approvalResult.approved === true && self.request.status === 'FOR_APPROVAL') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-check-circle text-success"></i></div>';
    }

    else if (point.properties.cablingResult && point.properties.cablingResult.cabled === false && self.request.status === 'FOR_CABLING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-plug text-danger"></i></div>';
    }

    else if (point.properties.cablingResult && point.properties.cablingResult.cabled === true && self.request.status === 'FOR_CABLING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-plug text-success"></i></div>';
    }

    else if (point.properties.testResult && point.properties.testResult.passed === false && self.request.status === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-times-circle text-danger"></i></div>';
    }

    else if (point.properties.testResult && point.properties.testResult.passed === true && self.request.status === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-check-circle text-success"></i></div>';
    }

    else if (point.properties.testResult && point.properties.testResult.postponed === true && self.request.status === 'FOR_TESTING') {
      return '<div class="row-header">' + point.lineNo + ' <i class="fa fa-minus-circle text-muted"></i></div>';
    }

    return point.lineNo;
  }

  /**
   * Slightly hacky little function to make sure all the elements on the page
   * are properly initialised.
   */
  function getAfterRenderFunction(request) {
    return function afterRender() {

      // Initialise the popovers in the row headers
      $('.row-header').popover({trigger: 'hover', delay: {'show': 100, 'hide': 100}});

      // Initialise the help text popovers on the column headers
      $('.help-text').popover({trigger: 'hover', delay: {'show': 500, 'hide': 100}});

      if (TableService.hasCheckboxColumn(request)) {

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
        checkboxHeader.prop(getCheckboxHeaderState(), true);

        var header, cells;
        if (TableService.hasCommentColumn(request)) {
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
        checkboxHeader.change(function () {
          for (var i = 0, len = self.request.points.length; i < len; i++) {
            self.request.points[i].selected = this.checked;
          }

          // Need to explicitly trigger a digest loop here because we are out of the angularjs world and in the happy land
          // of jquery hacking
          $scope.$apply();
        });

        // Listen for change events on all checkboxes
        $('.htCheckboxRendererInput:checkbox').change(function () {
          $('.select-all:checkbox').prop(getCheckboxHeaderState(), true);
        });
      }
    };
  }

  /**
   *
   * @returns {*}
   */
  function getCheckboxHeaderState() {
    if (!self.table.hasOwnProperty('getSelectedLineNumbers')) {
      return 'unchecked';
    }

    if (self.table.getSelectedLineNumbers().length === self.request.points.length) {
      return 'checked';
    } else if (self.table.getSelectedLineNumbers().length > 0) {
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
  function beforeChange(changes, source) {
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

      for (var j = 0, jlen = self.activeCategory.fields.length; j < jlen; j++) {
        var field = self.activeCategory.fields[j];

        if (field.id === prop) {

          // Remove accented characters
          newValue = $filter('latinize')(newValue);

          // Force uppercase if necessary
          if (field.uppercase === true) {
            newValue = $filter('uppercase')(newValue);
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
  function afterChange(changes, source) {
    // When the table is initially loaded, this callback is invoked with
    // source === 'loadData'. In that case, we don't want to do anything.
    if (source === 'loadData') {
      return;
    }

    console.log('afterChange()');

    // Make sure the line numbers are consecutive
    self.request.points.forEach(function (row, i) {
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
        console.log('dirty point: ' + self.request.points[row].lineNo);
        dirty = true;
        self.request.points[row].dirty = true;
      }

      // If the value was cleared, make sure any other properties of the object are also cleared.
      if (newValue === undefined || newValue === null || newValue === '') {
        //var point = self.parent.hot.getSourceDataAtRow(row);
        var point = self.request.points[row];
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
      var promise = saveNewValue(row, property, newValue);
      promises.push(promise);
    }

    // Wait for all new values to be updated
    $q.all(promises).then(function () {

      // If nothing changed, there's nothing to do! Otherwise, save the request.
      if (dirty) {
        self.request.valid = false;

        RequestService.saveRequest(self.request).then(function () {

          // Reload the history
          //RequestService.getRequestHistory(self.request.requestId).then(function (history) {
          //  self.history = history;
          //  self.hot.render();
          //});
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
  function saveNewValue(row, property, newValue) {
    var q = $q.defer();
    var point = self.request.points[row];

    // get the outer object i.e. properties.location.value -> location
    var outerProp = property.split('.')[1];
    var field = Utils.getField(self.schema, outerProp);

    // If there is no corresponding field in the schema, then it must be a "virtual"
    // column (such as "comment" or "checkbox")
    if (field === undefined) {
      q.resolve();
      return q.promise;
    }

    // For autocomplete fields, re-query the values and manually save it back to the point.
    if (field.type === 'autocomplete') {
      SchemaService.queryFieldValues(field, newValue, point).then(function (values) {

        values.forEach(function (item) {
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

  /**
   *
   */
  function normaliseLineNumbers() {
    for (var i = 0, len = self.request.points.length; i < len; i++) {
      self.request.points[i].lineNo = i + 1;
    }
  }

  /**
   *
   */
  function getSelectedLineNumbers() {
    if ($.isEmptyObject(self.table)) {
      return [];
    }

    var checkboxes = self.table.getDataAtProp('selected');
    var lineNumbers = [];

    for (var i = 0, len = checkboxes.length; i < len; i++) {
      if (checkboxes[i]) {
        // Line numbers are 1-based
        lineNumbers.push(self.request.points[i].lineNo);
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
  function navigateToField(categoryName, fieldId) {

    // Find the category which contains the field.
    var category;

    if (fieldId.indexOf('.') !== -1) {
      fieldId = fieldId.split('.')[0];
    }

    self.schema.categories.concat(self.schema.datasources).forEach(function (cat) {
      if (cat.name === categoryName || cat.id === categoryName) {
        cat.fields.forEach(function (field) {
          if (field.id === fieldId || cat.name === fieldId || cat.id === fieldId) {
            category = cat;
          }
        });
      }
    });

    if (category) {
      self.table.activateCategory(category);
    }
  }
}
