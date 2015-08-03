'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController($scope, $http, $timeout, $modal, request, children, schema, tasks, signals, RequestService, ColumnService, SchemaService, AlertService, HistoryService) {
  var self = this;

  self.request = request;
  self.children = children;
  self.schema = schema;
  self.tasks = tasks;
  self.signals = signals;

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
    // pasteMode: 'shift_down', // problematic
    outsideClickDeselects: false,
    manualColumnResize: true,
    //manualRowMove: true,
    afterInit: afterInit,
    afterRender: afterRender,
    afterChange: afterChange,
    afterValidate: afterValidate,
    afterCreateRow: afterCreateRow,
    afterRemoveRow: afterRemoveRow
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

  self.errorLogOpen = false;

  /**
   * Public function definitions.
   */
  self.afterInit = afterInit;
  self.getRows = getRows;
  self.getRowHeaders = getRowHeaders;
  self.getColumns = getColumns;

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
  self.showHelp = showHelp;
  self.showComments = showComments;
  self.showHistory = showHistory;

  self.getSelectedPointIds = getSelectedPointIds;
  self.getNumValidationErrors = getNumValidationErrors;
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
    //getColumnHeaders();
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
      '<i class="fa fa-exclamation-circle text-danger error-indicator"\
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
      column.renderer = customRenderer;
      self.columns.push(column);
    }

    //if (self.request.status != 'IN_PROGRESS' && self.request.status != 'FOR_CORRECTION') {
    //  // Checkbox column not shown when preparing
      self.columns.push({data: 'selected', type: 'checkbox'});
    //}
  }

  /**
   * TODO clean this up and refactor it out!!!
   *
   * Tagname format: system_code|subsystem_code|’.’|functionality_code|’.’|equipment_identifier|’:’|point_attribute
   */
  function generateTagnames() {

    for (var i = 0, len = self.rows.length; i < len; i++) {
      var point = self.rows[i];

      if (!point.properties.subsystem) {
        return;
      }

      (function (point) {
        $http.get(BACKEND_BASE_URL + '/subsystems/search/find', {
          params: {query: point.properties.subsystem.value},
          cache: true
        }).then(function (response) {

          if (!response.data.hasOwnProperty('_embedded')) {
            return;
          }

          var subsystemCode;

          if (response.data._embedded.subsystems.length == 1) {
            var subsystem = response.data._embedded.subsystems[0];
            subsystemCode = subsystem.systemCode + subsystem.subsystemCode;
          } else {
            subsystemCode = '?';
          }

          var site = (point.properties.functionality && point.properties.functionality.value ? point.properties.functionality.value : '?');
          var equipmentIdentifier = getEquipmentIdentifier(point);
          var attribute = (point.properties.pointAttribute ? point.properties.pointAttribute : '?');

          if (subsystemCode == '?' && site == '?' && equipmentIdentifier == '?' && attribute == '?') {
            point.properties.tagname = '';
          } else {
            point.properties.tagname = subsystemCode + '.' + site + '.' + equipmentIdentifier + ':' + attribute;
          }
        });
      })(point);
    }

    generateFaultStates();
  }

  /**
   * TODO clean this up and refactor it out!!!
   *
   * Fault state format: system_name|’_’|subsystem_name|’_’|general_functionality|’:’|equipment_identifier|’:’|point_description
   */
  function generateFaultStates() {
    for (var i = 0, len = self.rows.length; i < len; i++) {
      var point = self.rows[i];

      if (!point.properties.subsystem) {
        return;
      }

      (function (point) {
        $http.get(BACKEND_BASE_URL + '/subsystems/search/find', {
          params: {query: point.properties.subsystem.value},
          cache: true
        }).then(function (response) {

          if (!response.data.hasOwnProperty('_embedded')) {
            return;
          }

          var systemName = '?', subsystemName = '?';

          if (response.data._embedded.subsystems.length == 1) {
            var subsystem = response.data._embedded.subsystems[0];
            systemName = subsystem.system;
            subsystemName = subsystem.subsystem;
          }

          if (point.properties.functionality.value) {
            $http.get(BACKEND_BASE_URL + '/functionalities/search/find', {
              params: {query: point.properties.functionality.value},
              cache: true
            }).then(function (response) {
              if (!response.data.hasOwnProperty('_embedded')) {
                return;
              }

              var func = '?';
              if (response.data._embedded.functionalities.length == 1) {
                var functionality = response.data._embedded.functionalities[0];
                func = functionality.generalFunctionality;
              }

              var equipmentIdentifier = getEquipmentIdentifier(point);
              var description = point.properties.pointDescription ? point.properties.pointDescription : '?';

              if (systemName == '?' && subsystemName == '?' && func == '?' && equipmentIdentifier == '?' && description == '?') {
                point.properties.faultState = '';
              } else {
                point.properties.faultState = systemName + '_' + subsystemName + '_' + func + ':' + equipmentIdentifier + ':' + description;
              }
            });
          }
        });
      })(point);
    }
  }

  /**
   *
   * @param point
   * @returns {*}
   */
  function getEquipmentIdentifier(point) {
    var equipmentIdentifier;
    var gmaoCode = point.properties.gmaoCode ? point.properties.gmaoCode.value : '';
    var otherEquipCode = point.properties.otherEquipCode;

    if (gmaoCode && otherEquipCode) {
      if (gmaoCode === otherEquipCode) {
        equipmentIdentifier = gmaoCode;
      } else {
        equipmentIdentifier = gmaoCode + '_' + otherEquipCode;
      }
    } else if (gmaoCode && !otherEquipCode) {
      equipmentIdentifier = gmaoCode;
    } else if (!gmaoCode && otherEquipCode) {
      equipmentIdentifier = otherEquipCode;
    } else {
      equipmentIdentifier = '?'
    }

    return equipmentIdentifier;
  }

  /**
   *
   */
  function getAvailableExtraCategories() {
    // TODO refactor this into a service
    $http.get(BACKEND_BASE_URL + '/domains/' + self.request.domain).then(function (response) {
      self.availableCategories = [];

      response.data.categories.map(function (category) {

        // Only add the category if we aren't already using it
        if ($.grep(self.schema.categories, function (item) {
          return item.name == category;
        }).length == 0) {
          self.availableCategories.push(category);
        }
      });

      response.data.datasources.map(function (category) {

        // Only add the category if we aren't already using it
        if ($.grep(self.schema.categories, function (item) {
          return item.name == category;
        }).length == 0) {
          self.availableCategories.push(category);
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

    SchemaService.getSchema(request, categoryName).then(function(schema) {
      console.log('fetched new schema: ' + schema.name);
      self.schema = schema;
      getAvailableExtraCategories();

      // Find the new category and activate it
      for (var i in self.schema.categories) {
        var category = self.schema.categories[i];
        if (category.name == categoryName) {
          activateCategory(category);
        }
      }
    });
  }

  /**
   *
   * @param changes
   * @param source
   */
  function afterChange(changes, source) {
    console.log('afterChange()');
    for (var i = 0, len = self.rows.length; i < len; i++) {
      self.rows[i].id = i + 1;
    }

    generateTagnames();
  }

  /**
   *
   * @param isValid
   * @param value
   * @param row
   * @param prop
   * @param source
   */
  function afterValidate(isValid, value, row, prop, source) {
    self.request.valid = isValid;
  }

  /**
   *
   * @param index
   * @param amount
   */
  function afterCreateRow(index, amount) {
    // Fix the point IDs
    //self.rows[index].id = index + 1;
    //
    for (var i = 0, len = self.rows.length; i < len; i++) {
      self.rows[i].id = i + 1;
    }
  }

  /**
   *
   * @param index
   * @param amount
   */
  function afterRemoveRow(index, amount) {
    // Fix the point IDs
    //self.rows[index].id = index + 1;
    //
    for (var i = 0, len = self.rows.length; i < len; i++) {
      self.rows[i].id = i + 1;
    }
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
   * @returns {number}
   */
  function getNumValidationErrors() {
    var n = 0;

    for (var i in self.rows) {
      var point = self.rows[i];

      for (var j in point.errors) {
        n += point.errors[j].errors.length;
      }
    }

    return n;
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

    //AlertService.add('danger', 'This is a warning')
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
    self.hot.copyPaste.copyPasteInstance.onPaste(function (value) {
      console.log('onPaste(): ' + value);
    })
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
  function showHelp() {
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/help-modal.html',
      controller: 'HelpModalController as ctrl'
    });
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
        request: function () {
          return self.request;
        }
      }
    });
  }

  /**
   *
   */
  function showHistory() {
    var modalInstance = $modal.open({
      animation: false,
      size: 'lg',
      templateUrl: 'components/request/modals/history-modal.html',
      controller: 'HistoryModalController as ctrl',
      resolve: {
        request: function () {
          return self.request;
        },
        history: function () {
          return HistoryService.getHistory(self.request.requestId);
        }
      }
    });
  }

  /**
   * Calculate the required height for the table so tha tit fills the screen.
   */
  function calculateTableHeight() {
    var mainHeader = $('.main-header');
    var requestHeader = $('.request-header');
    var toolbar = $('.toolbar');
    var table = $('.table');
    //var log = $('.log');
    var footer = $('.footer');

    var height = $(window).height() - mainHeader.outerHeight() - requestHeader.outerHeight() - toolbar.outerHeight()
    - /* log.outerHeight() - */ footer.outerHeight();

    console.log($(window).height());
    console.log(mainHeader.height());
    console.log(requestHeader.height());
    console.log(toolbar.height());
    console.log(footer.height());

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
   * @param instance
   * @param td
   * @param row
   * @param col
   * @param prop
   * @param value
   * @param cellProperties
   */
  function customRenderer(instance, td, row, col, prop, value, cellProperties) {
    switch (cellProperties.type) {
      case 'text':
        Handsontable.renderers.TextRenderer.apply(this, arguments);
        break;
      case 'numeric':
        Handsontable.renderers.NumericRenderer.apply(this, arguments);
        break;
      case 'autocomplete':
        Handsontable.renderers.AutocompleteRenderer.apply(this, arguments);
        break;
      case 'dropdown':
        Handsontable.renderers.AutocompleteRenderer.apply(this, arguments);
        break;
      case 'checkbox':
        Handsontable.renderers.CheckboxRenderer.apply(this, arguments);
        break;
    }

    if (typeof prop !== 'string') {
      return;
    }

    var point = self.rows[row];
    prop = prop.replace('properties.', '');

    for (var i in point.errors) {
      var error = point.errors[i];

      // If the property name isn't specified, then the error applies to the whole point.
      if (error.property === prop || error.property === '') {
        td.style.background = '#F2DEDE';
      }
    }
  }


  /**
   * Slightly hacky little function to make sure all the elements on the page are properly
   * initialised.
   */
  function afterRender() {

    // Initialise the popovers in the row headers
    $('.error-indicator').popover({trigger: "manual", html: true, animation: false})
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

    // Initialise the help text popovers on the column headers
    $('.help-text').popover({trigger: 'hover', delay: {"show": 500, "hide": 100}});

    //if (self.request.status != 'IN_PROGRESS' && self.request.status != 'FOR_CORRECTION') {
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

      //checkboxTd.css('width', '20px');
    //}

    // Centre checkbox columns
    var checkboxCell = $('.htCore input.htCheckboxRendererInput').parent();
    checkboxCell.css('text-align', 'center');
  }

  /**
   * When the global language is changed, this event will be fired. We catch it here and
   * update the columns to make sure the help text etc. is in the right language.
   */
  $scope.$on('event:languageChanged', function () {
    $timeout(function () {
      console.log('language changed: refreshing columns');
      getColumns();
    }, 50);
  });
}