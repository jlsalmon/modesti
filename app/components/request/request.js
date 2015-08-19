'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController($scope, $http, $timeout, $modal, $filter, request, children, schema, tasks, signals,
                           RequestService, ColumnService, SchemaService, AlertService, HistoryService, TaskService) {
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
    beforeChange: beforeChange,
    afterChange: afterChange,
    afterValidate: afterValidate,
    afterCreateRow: afterCreateRow,
    afterRemoveRow: afterRemoveRow
  };

  //self.tableExpanded = true;
  //self.toggleTableStretch = function() {
  //  if (self.tableExpanded === true) {
  //    self.hot.updateSettings({stretchH: 'none'});
  //    self.tableExpanded = false;
  //    self.hot.render();
  //  } else {
  //    self.hot.updateSettings({stretchH: 'all'});
  //    self.tableExpanded = true;
  //    self.hot.render();
  //  }
  //};

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
  self.activateDefaultCategory = activateDefaultCategory;
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

      //renderRowBackgrounds();
    });
  }

  /**
   *
   */
  function activateDefaultCategory() {
    console.log('activating default category');
    activateCategory(self.schema.categories[0]);
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

        // Display only alarms
        if (point.properties['priorityCode']) {
          rows.push(point);
        }
      }
    }

    else if (self.request.status == 'FOR_ADDRESSING' || self.request.status == 'FOR_CABLING') {

      // TODO display only points which require cabling
      rows = self.request.points;
    }

    else {
      rows = self.request.points;
    }

    if (self.request.status !== 'IN_PROGRESS' && self.request.status !== 'FOR_CORRECTION') {
      // If the request is not in preparation, set maxRows to prevent new rows being added
      self.settings.maxRows = rows.length;
    }

    return rows;
  }

  /**
   * Row headers can optionally contain a success/failure icon and a popover message shown when the user hovers over
   * the icon.
   *
   * @param row
   * @returns {*}
   */
  function getRowHeaders(row) {
    var point = self.rows[row];

    if (point.valid === false) {
      return '<div class="row-header">' + point.id + ' <i class="fa fa-exclamation-circle text-danger"></i></div>';
    }
    //else if (point.valid === true) {
    //  return '<div class="row-header">' + point.id + ' <i class="fa fa-check-circle text-success"></i></div>';
    //}

    else if (point.approval.approved === false) {
      return '<div class="row-header">' + point.id + ' <i class="fa fa-exclamation-circle text-danger"></i></div>';
    }

    else if (point.approval.approved === true && self.request.status === 'FOR_APPROVAL') {
      return '<div class="row-header">' + point.id + ' <i class="fa fa-check-circle text-success"></i></div>';
    }



    return point.id;
  }

  /**
   * Note: currently ngHandsontable requires that columns be pushed into the array after the table has been initialised.
   * It does not accept a function, nor will it accept an array returned from a function call.
   * See https://github.com/handsontable/handsontable/issues/590. Hopefully this will be fixed in a later release.
   */
  function getColumns() {
    self.columns.length = 0;

    //self.columns.push({data: 'id', title: '#', readOnly: true, width: 30, className: "htCenter"});

    // Add a column for displaying icons
   // self.columns.push({readOnly: true, renderer: iconRenderer});

    // Append "select-all" checkbox field.
    if (hasCheckboxColumn()) {
      self.columns.push(getCheckboxColumn());
    }

    if (hasCommentColumn()) {
      self.columns.push(getCommentColumn());
    }

    var field, editable;
    for (var i = 0; i < self.activeCategory.fields.length; i++) {
      field = self.activeCategory.fields[i];

      // A column is editable only if the category is marked as an editable state for the current request status.

      // TODO: don't allow editing until the task is claimed (if it is a claimable task)

      if (isCurrentUserAuthorised()) {
        editable = self.activeCategory.editableStates.indexOf(self.request.status) > -1;
      } else {
        editable = false;
      }

      // Build the right type of column based on the schema
      var column = ColumnService.getColumn(field, editable);
      column.renderer = customRenderer;
      self.columns.push(column);
    }
  }

  function iconRenderer(instance, td, row, col, prop, value, cellProperties) {
    var point = self.rows[row];
    td.innerHTML = '';

    if (point.properties.priorityCode) {
      td.innerHTML += '<i class="fa fa-fw fa-bell"></i>';
    }


    //td.innerHTML = '<i class="fa fa-fw fa-bell"></i><i class="fa fa-fw fa-plug"></i><i class="fa fa-fw fa-terminal"></i>';
  }

  /**
   * The "select-all" checkbox column is shown when the request is in either state FOR_APPROVAL, FOR_ADDRESSING,
   * FOR_CABLING or FOR_TESTING, except when the task is not yet claimed or the user is not authorised.
   *
   * @returns {boolean}
   */
  function hasCheckboxColumn() {
    var checkboxStates = ['FOR_CORRECTION', 'FOR_APPROVAL', 'FOR_ADDRESSING', 'FOR_CABLING', 'FOR_TESTING'];

    var assigned = false;
    for (var key in self.tasks) {
      var task = self.tasks[key];
      if (TaskService.isCurrentUserAssigned(task)) {
        assigned = true;
      }
    }

    return checkboxStates.indexOf(self.request.status) > -1 && (self.request.status === 'FOR_CORRECTION' || assigned);
  }

  /**
   *
   * @returns {boolean}
   */
  function hasCommentColumn() {
    var commentStates =  ['FOR_APPROVAL', 'FOR_TESTING'];

    var assigned = false;
    for (var key in self.tasks) {
      var task = self.tasks[key];
      if (TaskService.isCurrentUserAssigned(task)) {
        assigned = true;
      }
    }

    return commentStates.indexOf(self.request.status) > -1 && assigned;
  }

  /**
   *
   * @returns {{data: string, type: string, title: string}}
   */
  function getCheckboxColumn() {
    return {data: 'selected', type: 'checkbox', title: '<input type="checkbox" class="select-all" />', renderer: customRenderer}
  }

  /**
   *
   * @returns {{data: *, type: string, title: string}}
   */
  function getCommentColumn() {
    var property;
    if (self.request.status == 'FOR_APPROVAL') {
      property = 'approval.message';
    } else if (self.request.status == 'FOR_TESTING') {
      property = 'testing.message';
    }

    return {data: property, type: 'text', title: 'Comment', renderer: customRenderer}
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentUserAuthorised() {
    for (var key in self.tasks) {
      if (TaskService.isCurrentUserAuthorised(self.tasks[key])) {
        return true;
      }
    }
    return false;
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

          if (point.properties.functionality && point.properties.functionality.value) {
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
                point.properties.faultFamily = '';
                point.properties.faultMember = '';
              } else {
                point.properties.faultState = systemName + '_' + subsystemName + '_' + func + ':' + equipmentIdentifier + ':' + description;
                point.properties.faultFamily = systemName + '_' + subsystemName + '_' + func;
                point.properties.faultMember = equipmentIdentifier;
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
    var equipmentIdentifier, gmaoCode;

    if (point.properties.gmaoCode && point.properties.gmaoCode.value) {
      gmaoCode = point.properties.gmaoCode.value
    } else if (point.properties.csamCsename && point.properties.csamCsename.value) {
      gmaoCode = point.properties.csamCsename.value;
    }

    var otherEquipCode;

    if (point.properties.otherEquipCode) {
      otherEquipCode = point.properties.otherEquipCode;
    } else if(point.properties.csamDetector && point.properties.csamDetector.value) {
      otherEquipCode = point.properties.csamDetector.value;
    }

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
   * @param activate {boolean} whether to activate the newly added category
   */
  function addExtraCategory(categoryName, activate) {
    if (typeof(activate) === 'undefined') activate = true;
    console.log("adding category " + categoryName);

    SchemaService.getSchema(request, categoryName).then(function(schema) {
      console.log('fetched new schema: ' + schema.name);
      self.schema = schema;

      getAvailableExtraCategories();

      if (activate) {
        // Find the new category and activate it
        for (var i in self.schema.categories) {
          var category = self.schema.categories[i];
          if (category.name == categoryName) {
            activateCategory(category);
          }
        }
      }
    });
  }

  /**
   * Called before a change is made to the table.
   *
   * @param changes a 2D array containing information about each of the edited cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: "alter", "empty", "edit", "populateFromArray", "loadData", "autofill", "paste"
   */
  function beforeChange(changes, source) {
    if (source == 'loadData') {
      return;
    }

    var change, row, property, oldValue, newValue;
    for (var i = 0, ilen = changes.length; i < ilen; i++) {
      change = changes[i];
      row = change[0];
      property = change[1];
      oldValue = change[2];
      newValue = change[3];

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
   * @param changes a 2D array containing information about each of the edited cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: "alter", "empty", "edit", "populateFromArray", "loadData", "autofill", "paste"
   */
  function afterChange(changes, source) {
    console.log('afterChange()');

    // Normalise point ids.
    // TODO is this necessary anymore?
    for (var i = 0, len = self.rows.length; i < len; i++) {
      self.rows[i].id = i + 1;
    }

    generateTagnames();
    generateFaultStates();
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
    if ($.isEmptyObject(self.hot)) {
      return [];
    }

    var checkboxes = self.hot.getDataAtProp('selected');
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
        break;
      }
    }
  }


  /**
   * Slightly hacky little function to make sure all the elements on the page are properly
   * initialised.
   */
  function afterRender() {
    // Initialise the popovers in the row headers
    $('.row-header').popover({trigger: 'hover', delay: {"show": 100, "hide": 100}});

    // Initialise the help text popovers on the column headers
    $('.help-text').popover({trigger: 'hover', delay: {"show": 500, "hide": 100}});

    if (hasCheckboxColumn()) {

      var firstColumnHeader = $('.htCore colgroup col.rowHeader');
      var lastColumnHeader = $('.htCore colgroup col:last-child');
      var checkboxColumn = $('.htCore colgroup col:nth-child(2)');

      // Fix the width of the "select-all" checkbox column (second column) and add the surplus to the last column
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
      if (hasCommentColumn()) {
        header = $('.htCore thead th:nth-child(3)');
        cells = $('.htCore tbody td:nth-child(3)');
      } else {
        header = $('.htCore thead th:nth-child(2)');
        cells = $('.htCore tbody td:nth-child(2)');
      }

      // Add a thicker border between the control column(s) and the first data column
      header.css('border-right', '5px double #ccc');
      cells.css('border-right', '5px double #ccc');

      // Listen for the change event on the "select-all" checkbox and act accordingly
      checkboxHeader.change(function () {
        for (var i = 0, len = self.rows.length; i < len; i++) {
          self.rows[i].selected = this.checked;
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
  }

  /**
   *
   * @returns {*}
   */
  function getCheckboxHeaderState() {
    if (self.getSelectedPointIds().length === self.rows.length) {
      return 'checked';
    } else if (self.getSelectedPointIds().length > 0) {
      return 'indeterminate'
    } else {
      return 'unchecked';
    }
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
