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
    comments: true,
    minSpareRows: 10,
    search: true,
    manualColumnResize: true,
    afterInit: afterInit,
    afterChange: afterChange
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
          });
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
          });
        }
      }

      self.columns.push(column);
    }
  }

  /**
   * AAAAAARRRGGGHHHHHHHHH
   * 
   * 16 is a magic number
   */
  function calculateTableHeight() {
    //Get window height and the wrapper height
    var neg = $('.main-header').outerHeight() + $('.main-footer').outerHeight();
    var window_height = $(window).height();

    //Set the height of the content based on the the height of the document.
    $(".content-wrapper").css('height', window_height - neg);

    var table = $('#table-wrapper');
    var footer = $('.main-footer');
    var offset = table.offset();
    
    console.log('.content-wrapper:' + $('.content-wrapper').height());
    var availableHeight = $(".content-wrapper").height() - $('#request-header').outerHeight(true) - $('.toolbar-wrapper').outerHeight(true) - $('.nav-tabs').outerHeight(true) - 16;
    
    console.log('window.height:' + $(window).height());
    console.log('offset.top:' + offset.top);
    console.log('.main-header:' + $('.main-header').outerHeight());
    console.log('#request-header:' + $('#request-header').outerHeight());
    console.log('.toolbar-wrapper:' + $('.toolbar-wrapper').outerHeight());
    console.log('.nav-tabs:' + $('.nav-tabs').outerHeight());
    console.log('footer.height:' + footer.outerHeight());
    console.log('neg:' + neg);
    console.log('calculated height:' + availableHeight);

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
    console.log('afterInit()');
    self.hot = this;

    calculateTableHeight();
  }
  
  function afterChange() {
    console.log('afterChange()');
  }

}