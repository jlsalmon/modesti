'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController($http, $timeout, $modal, request, children, schema, tasks, RequestService, ColumnService, AlertService) {
  var self = this;

  self.request = request;
  self.children = children;
  self.schema = schema;
  self.tasks = tasks;

  /**
   * The handsontable instance
   */
  self.hot = {};

  /**
   * Settings object for handsontable
   */
  self.settings = {
    rowHeaders: getRowHeaders,
    contextMenu: true,
    stretchH: 'all',
    // To enable sorting, a mapping needs to be done from the source array to the displayed array
    columnSorting: false,
    //currentRowClassName: 'currentRow',
    //comments: true,
    minSpareRows: 0,
    search: true,
    pasteMode: 'shift_down',
    outsideClickDeselects: false,
    manualColumnResize: true,
    //manualRowMove: true,
    afterInit: afterInit,
    afterRender: afterRender
  };

  /**
   * The data rows that will be given to the table
   *
   * @type {Array}
   */
  self.rows = getRows();

  /**
   * The columns that will be displayed for the currently active category. See getColumns().
   * @type {Array}
   */
  self.columns = [];

  /**
   * Stores the available extra categories that can potentially be added to the request.
   *
   * @type {Array}
   */
  self.availableExtraCategories = [];

  /**
   * Public function definitions.
   */
  self.afterInit = afterInit;
  self.getRows = getRows;
  self.getRowHeaders = getRowHeaders;
  self.getColumns = getColumns;
  self.getColumnHeaders = getColumnHeaders;

  self.activateCategory = activateCategory;
  self.addExtraCategory = addExtraCategory;
  self.getAvailableExtraCategories = getAvailableExtraCategories;
  self.resetSorting = resetSorting;
  self.save = save;
  self.undo = undo;
  self.redo = redo;
  self.cut = cut;
  self.copy = copy;
  self.paste = paste;
  self.search = search;
  self.showComments = showComments;
  self.showActivity = showActivity;

  self.getSelectedPointIds = getSelectedPointIds;
  self.renderRowBackgrounds = renderRowBackgrounds;

  /**
   * Called when the handsontable table has finished initialising.
   */
  function afterInit() {
    console.log('afterInit()');
    self.hot = this;

    calculateTableHeight();

    // Retrieve the list of available extra categories
    getAvailableExtraCategories();

    $timeout(function () {
      // Activate the first category
      activateCategory(self.schema.categories[0]);

      renderRowBackgrounds();
    });
  }

  /**
   *
   * @param category
   */
  function activateCategory(category) {
    console.log('activating category');
    var categories = self.schema.categories;

    for (var key in categories) {
      if (categories.hasOwnProperty(key)) {
        categories[key].active = false;
        console.log('category:' + categories[key]);
      }
    }

    category.active = true;
    console.log(category);
    self.activeCategory = category;


    getColumns();
    getColumnHeaders();
  }

  /**
   * Requests with certain statuses require that only some types points be displayed, i.e. requests in state
   * 'FOR_APPROVAL' should only display alarms. So we disconnect the data given to the table from the request and
   * include in it only those points which must be displayed.
   *
   * @returns {Array}
   */
  function getRows() {
    var rows = [];

    if (self.request.status == 'FOR_APPROVAL') {

      var point;
      for (var i = 0, len = self.request.points.length; i < len; i++) {
        point = self.request.points[i];

        if (point.properties['priorityCode']) {
          rows.push(point);
        }
      }

      // Also set maxRows to prevent new rows being added
      self.settings.maxRows = rows.length;
    }

    else {
      rows = self.request.points;
    }

    return rows;
  }

  /**
   *
   * @param row
   * @returns {*}
   */
  function getRowHeaders(row) {
    var point = self.rows[row];

    if (point.approval && point.approval.approved == false) {
      var html =
        '<i class="fa fa-exclamation-circle text-danger"\
            data-container="body" \
            data-toggle="popover" \
            data-placement="right" \
            data-content="' + 'Operator comment: ' + point.approval.message + '"> \
         </i>';

      return point.id + ' ' + html;
    }

    else {
      return point.id;
    }
  }

  /**
   * Note: currently ngHandsontable requires that columns be pushed into the array after the table has been initialised.
   * It does not accept a function, nor will it accept an array returned from a function call.
   * See https://github.com/handsontable/handsontable/issues/590. Hopefully this will be fixed in a later release.
   */
  function getColumns() {
    // Get the columns
    self.columns.length = 0;

    //self.columns.push({data: 'id', title: '#', readOnly: true, width: 30, className: "htCenter"});

    var field, editable;
    for (var i = 0; i < self.activeCategory.fields.length; i++) {
      field = self.activeCategory.fields[i];
      editable = self.activeCategory.editableStates.indexOf(self.request.status) > -1;

      // Build the right type of column based on the schema
      var column = ColumnService.getColumn(field, editable);
      self.columns.push(column);
    }

    // Checkbox column
    self.columns.push({data: 'selected', type: 'checkbox'});
  }

  /**
   *
   */
  function getColumnHeaders() {
    var colHeaders = [];

    for (var i = 0; i < self.activeCategory.fields.length; i++) {
      var field = self.activeCategory.fields[i];
      colHeaders.push(field.name);
    }

    //colHeaders.push('<input type="checkbox" class="select-all"  style="margin: 0" ' + (isChecked() ?
    // 'checked="checked"' : '') + '>');
    colHeaders.push('&nbsp;');

    // Set the column headers
    self.hot.updateSettings({ colHeaders: colHeaders });
  }

  /**
   *
   */
  function getAvailableExtraCategories() {
    // TODO refactor this into a service
    $http.get('http://localhost:8080/domains/' + self.request.domain).then(function (response) {
      self.availableCategories = [];

      response.data.datasources.map(function (category) {

        // Only add the category if we aren't already using it
        if ($.grep(self.schema.categories, function (item) {
            return item.name == category.name;
          }).length == 0) {
          self.availableCategories.push(category.name);
        }
      });
    });
  }

  /**
   *
   * @param categoryName
   */
  function addExtraCategory(categoryName) {
    console.log("adding category " + categoryName);

    var schemaLink = self.request._links.schema.href;

    if (schemaLink.indexOf('?categories') > -1) {
      schemaLink += ',' + categoryName;
    } else {
      schemaLink += '?categories=' + categoryName;
    }

    // TODO refactor this into a service
    $http.get(schemaLink).then(function (response) {
        console.log('fetched new schema: ' + response.data.name);
        self.schema = response.data;
        self.request._links.schema.href = schemaLink;

        getAvailableExtraCategories();

        // Find the new category and activate it
        for (var i in self.schema.categories) {
          var category = self.schema.categories[i];
          if (category.name == categoryName) {
            activateCategory(category);
          }
        }
      },

      function (error) {
        console.log('error fetching schema: ' + error);
      });
  }

  /**
   *
   */
  function getSelectedPointIds() {
    var checkboxes = self.hot.getDataAtCol(self.columns.length - 1);
    var pointIds = [];

    for (var i = 0, len = checkboxes.length; i < len; i++) {
      if (checkboxes[i]) {
        // Point IDs are 1-based
        pointIds.push(self.rows[i].id);
      }
    }

    return pointIds;
  }

  /**
   *
   */
  function save() {
    var request = self.request;

    RequestService.saveRequest(request).then(function () {
      console.log('saved request');
    }, function (error) {
      console.log('error saving request');
    });

    AlertService.add('danger', 'This is a warning')
  }

  /**
   *
   */
  function undo() {
    self.hot.undo();
  }

  /**
   *
   */
  function redo() {
    self.hot.redo();
  }

  /**
   *
   */
  function cut() {
    self.hot.copyPaste.triggerCut();
  }

  /**
   *
   */
  function copy() {
    self.hot.copyPaste.setCopyableText();
  }

  /**
   *
   */
  function paste() {
    self.hot.copyPaste.triggerPaste();
  }

  /**
   *
   * @param query
   */
  function search(query) {

    var result = self.hot.search.query(query);
    //self.hot.loadData(result);
  }

  /**
   *
   */
  function resetSorting() {
    // Hack to clear sorting
    self.hot.updateSettings({columnSorting: false});
    self.hot.updateSettings({columnSorting: true});
  }

  /**
   *
   */
  function showComments() {
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/comments-modal.html',
      controller: 'CommentsModalController as ctrl',
      resolve: {
        request: function() {
          return self.request;
        }
      }
    });
  }

  /**
   *
   */
  function showActivity() {
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/activity-modal.html',
      controller: 'ActivityModalController as ctrl',
      resolve: {
        request: function() {
          return self.request;
        }
      }
    });
  }

  /**
   *
   */
  function calculateTableHeight() {
    var mainHeader = $('.main-header');
    var requestHeader = $('.request-header');
    var toolbar = $('.toolbar');
    var table = $('.table');
    var footer = $('.footer');

    var offset = table.offset();
    console.log('window:' + $(window).height());
    console.log('table offset top:' + offset.top);
    console.log('mainHeader:' + mainHeader.outerHeight());
    console.log('requestHeader:' + requestHeader.outerHeight());
    console.log('toolbar:' + toolbar.outerHeight());
    console.log('table:' + table.outerHeight());
    console.log('footer:' + footer.outerHeight());

    var height = $(window).height() - mainHeader.outerHeight() - requestHeader.outerHeight() - toolbar.outerHeight()
      - footer.outerHeight();

    table.height(height + 'px');
  }

  /**
   *
   */
  function renderRowBackgrounds() {
    var point;

    self.hot.updateSettings({
      cells: function (row, col, prop) {
        point = self.rows[row];

        if (self.request.status == 'FOR_APPROVAL') {
          if (point.approval && point.approval.approved == false) {
            return {renderer: dangerCellRenderer};
          }
//          else if (point.approval && point.approval.approved == true) {
//            return {renderer: successCellRenderer};
//          }
        }
      }
    });
  }

  /**
   *
   * @param instance
   * @param td
   * @param row
   * @param col
   * @param prop
   * @param value
   * @param cellProperties
   */
  function dangerCellRenderer(instance, td, row, col, prop, value, cellProperties) {
    // Make sure to render the last column as a checkbox
    if (prop == 'selected') {
      Handsontable.renderers.CheckboxRenderer.apply(this, arguments);
    }

    // All the other columns can be rendered as text boxes at this point
    else {
      Handsontable.renderers.TextRenderer.apply(this, arguments);
    }

    // Make the background red
    td.style.background = '#F2DEDE';
  }

  /**
   *
   * @param instance
   * @param td
   * @param row
   * @param col
   * @param prop
   * @param value
   * @param cellProperties
   */
  function successCellRenderer(instance, td, row, col, prop, value, cellProperties) {
    // Make sure to render the last column as a checkbox
    if (prop == 'selected') {
      Handsontable.renderers.CheckboxRenderer.apply(this, arguments);
    }

    // All the other columns can be rendered as text boxes at this point
    else {
      Handsontable.renderers.TextRenderer.apply(this, arguments);
    }

    // Make the background red
    td.style.background = '#DFF0D8';
  }

  /**
   *
   */
  function afterRender() {

    // Initialise the popovers in the row headers
    $('[data-toggle="popover"]').popover({trigger: "manual", html: true, animation: false})
      .on("mouseenter", function () {
        var _this = this;
        $(this).popover("show");
        $(".popover").on("mouseleave", function () {
          $(_this).popover('hide');
        });
      })
      .on("mouseleave", function () {
        var _this = this;
        setTimeout(function () {
          if (!$(".popover:hover").length) {
            $(_this).popover("hide");
          }
        }, 300);
      });

    // Fix the width of the last column and add the surplus to the first column
    var firstColumnHeader = $('.htCore colgroup col.rowHeader');
    var secondColumnHeader = $('.htCore colgroup col:nth-child(2)');
    var secondColumnHeaderWidth = secondColumnHeader.width();
    var checkboxHeader = $('.htCore colgroup col:last-child');
    var checkboxHeaderWidth = checkboxHeader.width();
    secondColumnHeaderWidth = secondColumnHeaderWidth + (checkboxHeaderWidth - 30);
    secondColumnHeader.width(secondColumnHeaderWidth);
    checkboxHeader.width('30px');
    firstColumnHeader.width('45px');

    // Centre the checkbox in the last column
    var checkboxCell = $('.htCore input.htCheckboxRendererInput').parent();
    checkboxCell.css('text-align', 'center');
    //checkboxTd.css('width', '20px');
  }
}