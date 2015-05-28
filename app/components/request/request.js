'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController($http, request, children, schema, tasks) {
  const self = this;

  self.request = request;
  self.children = children;
  self.schema = schema;
  self.tasks = tasks;
  self.currentActiveTab = 0;

  /**
   * The handsontable instance
   */
  self.hot = {};

  /**
   * Settings object for handsontable
   */
  self.settings = {
    colHeaders: true,
    rowHeaders: true,
    contextMenu: true,
    stretchH: 'all',
    columnSorting: true,
    fixedColumnsLeft: 1,
    comments: true,
    minSpareRows: 10,
    search: true,
    manualColumnResize: true,
    afterInit: afterInit
  };

  self.columns = [];

  self.activateTab = activateTab;
  self.activateCategory = activateCategory;
  self.resetSorting = resetSorting;
  self.afterInit = afterInit;

  /**
   * Activate a particular tab
   */
  function activateTab(tab) {
    self.currentActiveTab = tab;
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

    self.columns.length = 0;

    //self.columns.push({data: 'id', title: '#', readOnly: true, width: 30, className: "htCenter"});

    for (var i = 0; i < self.activeCategory.fields.length; i++) {
      let field = self.activeCategory.fields[i];

      var column = {
        data: 'properties.' + field.id,
        title: field.name
      };

      if (field.type == 'typeahead') {
        column.type = 'autocomplete';
        column.strict = true;
        column.allowInvalid = true;

        if (field.model) {
          column.data = 'properties.' + field.id + '.' + field.model;
        }

        column.source = function(query, process) {

          if (field.minLength && query.length < field.minLength) {
            return;
          }

          var params = {};
          for (var i in field.params) {
            params[field.params[i]] = query;
          }

          // TODO refactor this into a service
          $http.get(field.url, {params : params}).then(function(response) {
            if (!response.data.hasOwnProperty('_embedded')) {
              return [];
            }

            var items = response.data._embedded[field.returnPropertyName].map(function(item) {
              return item[field.model];
            });

            process(items);
          })
        }
      }

      if (field.type == 'select') {
        column.type = 'dropdown';
        column.strict = true;
        column.allowInvalid = true;

        column.source = function(query, process) {

          // TODO refactor this into a service
          $http.get(field.options).then(function(response) {
            if (!response.data.hasOwnProperty('_embedded')) {
              return [];
            }

            var items = response.data._embedded[field.returnPropertyName].map(function(item) {
              return item[field.model];
            });

            process(items);
          })
        }
      }

      self.columns.push(column);
    }
  }

  /**
   *
   */
  function calculateTableHeight() {
    var table = $('#table-wrapper');
    var footer = $('.main-footer');
    var offset;

    offset = table.offset();
    var availableHeight = $(window).outerHeight() - offset.top - (footer.outerHeight() * 2);

    table.height(availableHeight + 'px');
  }

  /**
   *
   */
  function resetSorting() {
    // Hack to clear sorting
    self.hot.updateSettings({
      columnSorting: false
    });
    self.hot.updateSettings({
      columnSorting: true
    });
  }

  /**
   *
   */
  function afterInit() {
    self.hot = this;

    calculateTableHeight();
  }

}