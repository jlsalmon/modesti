'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController
 */
angular.module('modesti').controller('RequestController', RequestController);

angular.module('modesti').directive('customRequestControls', function($compile, $http, $ocLazyLoad) {
  return {
    scope: {
      request: '=',
      tasks: '=',
      schema: '=',
      table: '='
    },

    link: function(scope, element) {
      var schemaId = scope.request.domain;
      var status = scope.request.status.split('_').join('-').toLowerCase();

      $http.get(BACKEND_BASE_URL + '/plugins/' + schemaId + '/assets').then(function (response) {
        var assets = response.data;
        console.log(assets);

        $ocLazyLoad.load(assets, {serie: true}).then(function() {

          var template = '<div ' + status + '-controls request="request" tasks="tasks" schema="schema" table="table"></div>';
          element.append($compile(template)(scope));
        });
      });
    }
  };
});

angular.module('modesti').directive('showIf', function(TaskService, ngIfDirective) {
  return {
    transclude: 'element',
    priority: 600,
    link: function(scope, element, attrs) {
      attrs.ngIf = function() {
        var expression = attrs.showIf;
        var conditions = expression.split(" && ");
        var task = TaskService.getCurrentTask();
        var results = [];

        conditions.forEach(function (condition) {
          var result = false;

          if (condition.indexOf('user-authorised-for-task') != -1) {
            result = TaskService.isCurrentUserAuthorised(task);
            result = condition.indexOf('!') != -1 ? !result : result;
          }
          else if (condition.indexOf('task-assigned-to-current-user') != -1) {
            result = TaskService.isCurrentUserAssigned(task);
            result = condition.indexOf('!') != -1 ? !result : result;
          }
          else if (condition.indexOf('task-assigned') != -1) {
            result = TaskService.isTaskClaimed(task);
            result = condition.indexOf('!') != -1 ? !result : result;
          }

          results.push(result);
        });

        return results.reduce(function (a, b) {
          return (a === b) ? a : false;
        }) === true;
      };

      ngIfDirective[0].link.apply(ngIfDirective[0], arguments);
    }
  }
});

angular.module('modesti').directive('enableIf', function(TaskService, ngDisabledDirective) {
  return {
    link: function(scope, element, attrs) {
      attrs.ngDisabled = function() {
        var expression = attrs.enableIf;
        var conditions = expression.split(" && ");
        var task = TaskService.getCurrentTask();
        var results = [];

        conditions.forEach(function (condition) {
          var result = false;

          if (condition.indexOf('user-authorised-for-task') != -1) {
            result = TaskService.isCurrentUserAuthorised(task);
            result = condition.indexOf('!') != -1 ? !result : result;
          }
          else if (condition.indexOf('task-assigned-to-current-user') != -1) {
            result = TaskService.isCurrentUserAssigned(task);
            result = condition.indexOf('!') != -1 ? !result : result;
          }
          else if (condition.indexOf('task-assigned') != -1) {
            result = TaskService.isTaskClaimed(task);
            result = condition.indexOf('!') != -1 ? !result : result;
          }

          results.push(result);
        });

        return !results.reduce(function(a, b){ return (a === b) ? a : false; }) === true;
      };

      ngDisabledDirective[0].link.apply(ngDisabledDirective[0], arguments);
    }
  }
});


function RequestController($scope, $q, $state, $timeout, $modal, $filter, $localStorage,
                           request, children, schema, tasks, signals,
                           RequestService, ColumnService, SchemaService, HistoryService, TaskService, AuthService, AlertService, ValidationService, Utils) {
  var self = this;

  self.request = request;
  self.children = children;
  self.schema = schema;
  self.tasks = tasks;
  self.signals = signals;
  //self.history = history;

  /** The handsontable instance */
  self.hot = {};

  /** Settings object for handsontable */
  self.settings = {
    rowHeaders: getRowHeaders,
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
    afterRender: afterRender,
    beforeChange: beforeChange,
    afterChange: afterChange,
    afterCreateRow: afterCreateRow,
    afterRemoveRow: afterRemoveRow
  };

  /** The columns that will be displayed for the currently active category. */
  self.columns = [];

  /** Open state flag for the error log at the bottom of the page */
  self.errorLogOpen = false;

  /**
   * Public function definitions.
   */
  self.afterInit = afterInit;
  self.getRowHeaders = getRowHeaders;
  self.getColumns = getColumns;
  self.activateCategory = activateCategory;
  self.activateDefaultCategory = activateDefaultCategory;

  self.assignTask = assignTask;
  self.assignTaskToCurrentUser = assignTaskToCurrentUser;
  self.claim = claim;
  self.getAssignee = getAssignee;
  self.isCurrentTaskRestricted = isCurrentTaskRestricted;
  self.getSelectedPointIds = getSelectedPointIds;
  self.isInvalidCategory = isInvalidCategory;
  self.navigateToField = navigateToField;

  self.save = save;
  self.undo = undo;
  self.redo = redo;
  self.cut = cut;
  self.copy = copy;
  self.paste = paste;
  self.showHelp = showHelp;
  self.showComments = showComments;
  self.showHistory = showHistory;
  self.cloneRequest = cloneRequest;
  self.deleteRequest = deleteRequest;
  self.saveNewValue = saveNewValue;

  $localStorage.$default({
    lastActiveCategory: {}
  });

  /**
   * Called when the handsontable table has finished initialising.
   */
  function afterInit() {
    console.log('afterInit()');
    /*jshint validthis:true */
    self.hot = this;

    calculateTableHeight();
    activateDefaultCategory();

    // Evaluate "editable" conditions for the active category. This is because we need to evaluate the editability of individual cells based on the
    // value of other cells in the row, and we cannot do this in the column service.
    self.hot.updateSettings( {
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

          if (hasCheckboxColumn() && prop === 'selected') {
            editable = true;
          }
          else if (hasCommentColumn() && prop.contains('message')) {
            editable = true;
          }

          // Empty points should not be editable
          //if (Utils.isEmptyPoint(point)) {
          //  editable = false;
          //}
        }

        return { readOnly: !editable };
      }
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
    getColumns();
  }

  ///**
  // * Requests with certain statuses require that only some types points be displayed, i.e. requests in state
  // * 'FOR_APPROVAL' should only display alarms. So we disconnect the data given to the table from the request and
  // * include in it only those points which must be displayed.
  // *
  // * @returns {Array}
  // */
  //function getRows() {
  //  var rows = [];
  //
  //  if (self.request.status === 'FOR_APPROVAL') {
  //
  //    // TODO display only points which require approval?
  //    rows = self.request.points;
  //  }
  //
  //  else if (self.request.status === 'FOR_ADDRESSING' || self.request.status === 'FOR_CABLING') {
  //
  //    // TODO display only points which require cabling?
  //    rows = self.request.points;
  //  }
  //
  //  else {
  //    rows = self.request.points;
  //  }
  //
  //  if (self.request.status !== 'IN_PROGRESS' && self.request.status !== 'FOR_CORRECTION') {
  //    // If the request is not in preparation, set maxRows to prevent new rows being added
  //    self.settings.maxRows = rows.length;
  //  }
  //
  //  return rows;
  //}

  /**
   * Row headers can optionally contain a success/failure icon and a popover message shown when the user hovers over
   * the icon.
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
   * Note: currently ngHandsontable requires that columns be pushed into the array after the table has been initialised.
   * It does not accept a function, nor will it accept an array returned from a function call.
   * See https://github.com/handsontable/handsontable/issues/590. Hopefully this will be fixed in a later release.
   */
  function getColumns() {
    self.columns.length = 0;

    // Append "select-all" checkbox field.
    if (hasCheckboxColumn()) {
      self.columns.push(getCheckboxColumn());
    }

    if (hasCommentColumn()) {
      self.columns.push(getCommentColumn());
    }

    self.activeCategory.fields.forEach(function (field) {

      var authorised = false;
      var task = TaskService.getCurrentTask();

      var authorised = false;
      if (TaskService.isCurrentUserAuthorised(task) && TaskService.isCurrentUserAssigned(task)) {
        authorised = true;
      }

      var editable;

      // Build the right type of column based on the schema
      var column = ColumnService.getColumn(field, editable, authorised, self.request.status);
      column.renderer = customRenderer;
      self.columns.push(column);
    });
  }

  /**
   * The "select-all" checkbox column is shown when the request is in either state FOR_APPROVAL, FOR_ADDRESSING,
   * FOR_CABLING or FOR_TESTING, except when the task is not yet claimed or the user is not authorised.
   *
   * @returns {boolean}
   */
  function hasCheckboxColumn() {
    var checkboxStates = [/*'FOR_CORRECTION', */'FOR_APPROVAL', 'FOR_CABLING', 'FOR_TESTING'];

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
    var commentStates =  ['FOR_APPROVAL', 'FOR_CABLING', 'FOR_TESTING'];

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
    return {data: 'selected', type: 'checkbox', title: '<input type="checkbox" class="select-all" />', renderer: customRenderer};
  }

  /**
   *
   * @returns {{data: *, type: string, title: string}}
   */
  function getCommentColumn() {
    var property;
    if (self.request.status === 'FOR_APPROVAL') {
      property = 'properties.approvalResult.message';
    } else if (self.request.status === 'FOR_CABLING') {
      property = 'properties.cablingResult.message';
    }else if (self.request.status === 'FOR_TESTING') {
      property = 'properties.testResult.message';
    }

    return {data: property, type: 'text', title: 'Comment', renderer: customRenderer};
  }

  /**
   *
   */
  function assignTask() {
    var task = self.tasks[Object.keys(self.tasks)[0]];

    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/assignment-modal.html',
      controller: 'AssignmentModalController as ctrl',
      resolve: {
        task: function () {
          return task;
        }
      }
    });

    modalInstance.result.then(function (assignee) {
      console.log('assigning request to user ' + assignee.username);

      TaskService.assignTask(task.name, self.request.requestId, assignee.username).then(function (task) {
        console.log('assigned request');
        self.tasks[task.name] = task;
        self.request.assignee = assignee.username;
        activateDefaultCategory();
      });
    });
  }

  /**
   *
   */
  function assignTaskToCurrentUser() {
    var task = TaskService.getCurrentTask();
    var username = AuthService.getCurrentUser().username;

    TaskService.assignTask(task.name, self.request.requestId, username).then(function (task) {
      console.log('assigned request');
      self.tasks[task.name] = task;
      self.request.assignee = username;
      activateDefaultCategory();
    });
  }

  /**
   *
   */
  function claim(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    assignTaskToCurrentUser();
  }

  /**
   *
   */
  function getAssignee() {
    var task = self.tasks[Object.keys(self.tasks)[0]];

    if (!task) {
      return null;
    }

    return task.assignee;
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentTaskRestricted() {
    var task = self.tasks[Object.keys(self.tasks)[0]];
    return task && task.candidateGroups.length === 1 && task.candidateGroups[0] === 'modesti-administrators';
  }

  /**
<<<<<<< HEAD
   *
   * @returns {boolean}
   */
  function canEdit() {
    var task = self.tasks[Object.keys(self.tasks)[0]];
    return TaskService.isCurrentUserAuthorised(task) && TaskService.isCurrentUserAssigned(task);
  }

  /**
   *
   */
  function canValidate() {
    return self.tasks.edit;
  }

  /**
   *
   */
  function canSubmit() {
    var numEmptyPoints = 0;
    self.request.points.forEach(function (point) {
      if (Utils.isEmptyPoint(point)) {
        numEmptyPoints++;
      }
    });

    // Don't allow submit if all points are empty
    if (numEmptyPoints === self.request.points.length) {
      return false;
    }

    return self.tasks.submit;
  }

  /**
   *
   */
  function canSplit() {
    return self.hasErrors();
  }

  function validate(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    self.validating = 'started';
    AlertService.clear();

    $timeout(function () {
      ValidationService.validateRequest(self.request).then(function (request) {
        // Save the reference to the validated request
        self.request = request;
        self.rows = getRows();

        // Render the table to show the error highlights
        self.hot.render();

        if (!request.valid) {
          self.validating = 'error';
          var numErrors = getNumValidationErrors();
          AlertService.add('danger', 'Request failed validation with ' + numErrors + (numErrors === 1 ? ' error' : ' errors'));
          return;
        }

        $state.reload().then(function () {
          self.validating = 'success';
          AlertService.add('success', 'Request has been validated successfully');
        });
      },

      function (error) {
        console.log('error validating request: ' + error.statusText);
        self.validating = 'error';
      });
    });
  }

  /**
   *
   */
  function submit(event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    var task = self.tasks[Object.keys(self.tasks)[0]];

    if (!task) {
      console.log('warning: no submit task found');
      return;
    }

    AlertService.clear();
    self.submitting = 'started';

    RequestService.saveRequest(self.request).then(function () {
      console.log('saved request before submitting');

      // Complete the task associated with the request
      TaskService.completeTask(task.name, self.request.requestId).then(function () {
        console.log('completed task ' + task.name);

        var previousStatus = self.request.status;

        // Clear the cache so that the state reload also pulls a fresh request
        RequestService.clearCache();

        $state.reload().then(function () {
          self.submitting = 'success';

          // If the request is now FOR_CONFIGURATION, no need to go away from the request page
          if (self.request.status === 'FOR_CONFIGURATION') {
            AlertService.add('info', 'Request has been submitted successfully and is ready to be configured.');
          }

          if (self.request.status === 'CLOSED') {
            AlertService.add('info', 'Request has been submitted successfully and is now closed.');
          }

          // If the request is in any other state, show a page with information about what happens next
          else {
            $state.go('submitted', {id: self.request.requestId, previousStatus: previousStatus});
          }
        });
      },

      function (error) {
        console.log('error completing task: ' + error.statusText);
        self.submitting = 'error';
      });
    },

    function (error) {
      console.log('error completing task: ' + error.statusText);
      self.submitting = 'error';
    });
  }

  /**
   * Sends the "requestModified" signal when in the "submit" stage of the workflow in order to force the request
   * back to the "validate" stage.
   */
  function sendModificationSignal() {
    var signal = self.signals.requestModified;

    if (signal) {
      console.log('form modified whilst in submit state: sending signal');

      TaskService.sendSignal(signal).then(function () {
        // The "submit" task will have changed back to "edit".
        TaskService.getTasksForRequest(self.request).then(function (tasks) {
          self.tasks = tasks;
        });
      });
    }
  }

  /**
   *
   * @returns {boolean}
   */
  function hasErrors() {
    return getNumValidationErrors() > 0 || getNumApprovalRejections() > 0 || getNumConfigurationErrors() > 0 || getNumInsertionErrors() > 0;
  }

  /**
   *
   * @returns {number}
   */
  function getTotalErrors() {
    return getNumValidationErrors() + getNumApprovalRejections() + getNumConfigurationErrors() + getNumInsertionErrors();
  }

  /**
   *
   * @returns {number}
   */
  function getNumValidationErrors() {
    var n = 0;

    if (self.request.hasOwnProperty('points')) {
      self.request.points.forEach(function (point) {
        if (point.hasOwnProperty('errors')) {
          point.errors.forEach(function (error) {
            n += error.errors.length;
          });
        }
      });
    }

    return n;
  }

  /**
   *
   * @returns {number}
   */
  function getNumApprovalRejections() {
    var n = 0;

    self.rows.forEach(function (point) {
      if (point.properties.approvalResult && point.properties.approvalResult.approved === false) {
        n++;
      }
    });

    return n;
  }

  /**
   *
   * @returns {number}
   */
  function getNumConfigurationErrors() {
    if (self.request.properties.configurationResult && self.request.properties.configurationResult.errors) {
      return self.request.properties.configurationResult.errors.length;
    } else {
      return 0;
    }
  }

  /**
   *
   * @returns {number}
   */
  function getNumInsertionErrors() {
    if (self.request.properties.insertionResult && self.request.properties.insertionResult.errors) {
      return self.request.properties.insertionResult.errors.length;
    } else {
      return 0;
    }
  }

  /**
   * Return true if the given category is "invalid", i.e. there are points in
   * the current request that have errors that relate to the category.
   *
   * @param category
   */
  function isInvalidCategory(category) {
    var fieldIds = category.fields.map(function (field) {return field.id;});
    var invalid = false;

    self.request.points.forEach(function (point) {
      if (point.errors && point.errors.length > 0) {
        point.errors.forEach(function (error) {
          if (!error.category) {
            var property = error.property.split('.')[0];

            if (fieldIds.indexOf(property) !== -1) {
              invalid = true;
            }
          }

          else if (error.category === category.name || error.category === category.id) {
            invalid = true;
          }
        });
      }
    });

    return invalid;
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
      activateCategory(category);
    }
  }


  /**
   * Called before a change is made to the table.
   *
   * @param changes a 2D array containing information about each of the edited cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: 'alter', 'empty', 'edit', 'populateFromArray', 'loadData', 'autofill', 'paste'
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
   * @param changes a 2D array containing information about each of the edited cells [ [row, prop, oldVal, newVal], ... ]
   * @param source one of the strings: "alter", "empty", "edit", "populateFromArray", "loadData", "autofill", "paste"
   */
  function afterChange(changes, source) {
    // When the table is initially loaded, this callback is invoked with source === 'loadData'. In that case, we don't
    // want to do anything.
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
        RequestService.saveRequest(self.request).then(function () {
          // If we are in the "submit" stage of the workflow and the form is modified, then it will need to be
          // revalidated. This is done by sending the "requestModified" signal.
          if (self.tasks.submit) {
            self.sendModificationSignal();
          }

          self.request.valid = false;

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
   * Currently Handsontable does not support columns backed by complex objects, so for now it's necessary to manually save the object in the background.
   * See https://github.com/handsontable/handsontable/issues/2578.
   *
   * Also, sometimes after a modification, Handsontable does not properly save the new value to the underlying point. So we manually save the value in
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
  function afterCreateRow() {
    // Fix the point IDs
    for (var i = 0, len = self.request.points.length; i < len; i++) {
      self.request.points[i].lineNo = i + 1;
    }
  }

  /**
   *
   */
  function afterRemoveRow() {
    // Fix the point IDs
    for (var i = 0, len = self.request.points.length; i < len; i++) {
      self.request.points[i].lineNo = i + 1;
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
        pointIds.push(self.request.points[i].lineNo);
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
    }, function () {
      console.log('error saving request');
    });
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
    });
  }

  /**
   *
   */
  function showHelp() {
    $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/help-modal.html',
      controller: 'HelpModalController as ctrl'
    });
  }

  /**
   *
   */
  function showComments() {
    $modal.open({
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
    $modal.open({
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
   *
   */
  function deleteRequest() {
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/delete-modal.html',
      controller: 'DeleteModalController as ctrl',
      resolve: {
        request: function () {
          return self.request;
        }
      }
    });

    modalInstance.result.then(function () {
      RequestService.deleteRequest(request.requestId).then(function () {
        console.log('deleted request');
        AlertService.add('success', 'Request was deleted successfully.');
        $state.go('requests');
      },

      function (error) {
        console.log('delete failed: ' + error.statusText);
      });
    },

    function () {
      console.log('delete aborted');
    });
  }

  /**
   *
   */
  function cloneRequest() {
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/clone-modal.html',
      controller: 'CloneModalController as ctrl',
      resolve: {
        request: function () {
          return self.request;
        },
        schema: function () {
          return self.schema;
        }
      }
    });

    modalInstance.result.then(function () {

    },

    function () {
      console.log('clone aborted');
    });
  }

  /**
   * Calculate the required height for the table so that it fills the screen.
   */
  function calculateTableHeight() {
    var mainHeader = $('.main-header');
    var requestHeader = $('.request-header');
    var toolbar = $('.toolbar');
    var table = $('.table');
    //var log = $('.log');
    var footer = $('.footer');

    var height = $(window).height() - mainHeader.outerHeight() - requestHeader.outerHeight() - toolbar.outerHeight() - footer.outerHeight();

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
    /*jshint validthis:true */

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
    }

    if (cellProperties.editor === 'select2') {
      Handsontable.renderers.AutocompleteRenderer.apply(this, arguments);
    }

    if (typeof prop !== 'string') {
      return;
    }

    var point = self.request.points[row];
    if (Utils.isEmptyPoint(point)) {
      return;
    }

    var props = prop.split('.').slice(1, 3);
    //prop = prop.replace('properties.', '');

    // Check if we need to fill in a default value for this point.
    var field = Utils.getField(self.schema, props[0]);
    if (field) {
      setDefaultValue(point, field);
    }

    // Highlight errors in a cell by making the background red.
    for (var i in point.errors) {
      var error = point.errors[i];

      // If the property name isn't specified, then the error applies to the whole point.
      if (error.property === prop.replace('properties.', '') || error.property === props[0] || error.property === '') {
        td.className += ' alert-danger';
        break;
      }
      // Highlight an entire category if the property matches a category name.
      else {
        var category = Utils.getCategory(self.schema, error.category);

        if (category) {
          for (var j in category.fields) {
            var f = category.fields[j];

            if (f.id === field.id) {
              td.className += ' alert-danger';
              return;
            }
          }
        }
      }
    }


    //if (self.request.type === 'UPDATE' && point.dirty === true) {
    //  var changes = [];
    //  $(td).popover('destroy');
    //
    //  self.history.events.forEach(function (event) {
    //    event.changes.forEach(function (change) {
    //      if (change.path.indexOf(field.id) !== -1 && change.path.indexOf('[' + point.lineNo + ']') !== -1) {
    //        changes.push(change);
    //      }
    //    });
    //  });
    //
    //  if (changes.length > 0) {
    //    var latest = changes[changes.length - 1];
    //    var original, modified;
    //
    //    if (field.type === 'autocomplete') {
    //      original = field.model ? latest.original[field.model] : latest.original.value;
    //      modified = field.model ? latest.modified[field.model] : latest.modified.value;
    //    } else {
    //      original = latest.original;
    //      modified = latest.modified;
    //    }
    //
    //    var content = '<samp><table>';
    //    content    += '<tr><td style="background-color: #ffecec">&nbsp;- ' + original + '&nbsp;</td></tr>';
    //    content    += '<tr><td style="background-color: #dbffdb">&nbsp;+ ' + modified + '&nbsp;</td></tr></table></samp>';
    //
    //    td.style.background = '#fcf8e3';
    //    $(td).popover({ trigger: 'hover', placement: 'top', container: 'body', html: true, content: content/*, delay: { 'show': 0, 'hide': 100000 }*/ });
    //  }
    //}
  }

  /**
   * Inspect the given field and set the default value in the point if supplied. The default value can refer
   * to another property of the point via mustache-syntax, so interpolate that as well.
   *
   * @param point
   * @param field
   */
  function setDefaultValue(point, field) {
    var currentValue;

    if (field.type === 'autocomplete') {
      if (point.properties.hasOwnProperty(field.id) && point.properties[field.id]) {
        currentValue = field.model ? point.properties[field.id][field.model] : point.properties[field.id].value;
      }
    } else {
      currentValue = point.properties[field.id];
    }

    if (currentValue === undefined || currentValue === null || currentValue === '') {
      var regex = /^\{\{\s*[\w\.]+\s*}}/g;

      if (field.default && typeof field.default === 'string' && regex.test(field.default)) {
        var matches = field.default.match(regex).map(function(x) {
          return x.match(/[\w\.]+/)[0];
        });

        var props = matches[0].split('.');

        if (point.properties.hasOwnProperty(props[0])) {
          var outerProp = point.properties[props[0]];

          if (outerProp.hasOwnProperty(props[1])) {
            var value = outerProp[props[1]];
            var model = field.model ? field.model : 'value';

            console.log('setting default value ' + value + ' for ' + field.id + '.' + model + ' on point ' + point.lineNo);

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
   * Slightly hacky little function to make sure all the elements on the page are properly
   * initialised.
   */
  function afterRender() {

    // Initialise the popovers in the row headers
    $('.row-header').popover({trigger: 'hover', delay: {'show': 100, 'hide': 100}});

    // Initialise the help text popovers on the column headers
    $('.help-text').popover({trigger: 'hover', delay: {'show': 500, 'hide': 100}});

    if (hasCheckboxColumn()) {

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
  }

  /**
   *
   * @returns {*}
   */
  function getCheckboxHeaderState() {
    if (self.getSelectedPointIds().length === self.request.points.length) {
      return 'checked';
    } else if (self.getSelectedPointIds().length > 0) {
      return 'indeterminate';
    } else {
      return 'unchecked';
    }
  }
}
