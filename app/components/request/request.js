'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController Controller of the modesti
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController($http, $timeout, request, children, schema, tasks, RequestService, ColumnService) {
  var self = this;

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
    pasteMode: 'shift_down',
    outsideClickDeselects: false,
    manualColumnResize: true,
    manualRowMove: true,
    afterInit: afterInit,
    afterChange: afterChange
  };

  /**
   *
   * @type {Array}
   */
  self.columns = [];

  /**
   * Stores the available extra categories that can potentially be added to the request.
   *
   * @type {Array}
   */
  self.availableExtraCategories = [];

  self.activateTab = activateTab;
  self.activateCategory = activateCategory;
  self.addNewCategory = addNewCategory;
  self.getAvailableExtraCategories = getAvailableExtraCategories;
  self.save = save;
  self.search = search;
  self.resetSorting = resetSorting;
  self.afterInit = afterInit;

  /**
   *
   */
  function afterInit() {
    console.log('afterInit()');
    self.hot = this;

    calculateTableHeight();

    // Retrieve the list of available extra categories
    getAvailableExtraCategories();

    $timeout(function() {
      // Activate the first category
      activateCategory(self.schema.categories[0]);
    })

  }

  /**
   *
   */
  function afterChange() {
    console.log('afterChange()');
  }

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

    // Remove existing columns
    self.columns.length = 0;

    //self.columns.push({data: 'id', title: '#', readOnly: true, width: 30, className: "htCenter"});

    for (var i = 0; i < self.activeCategory.fields.length; i++) {
      var field = self.activeCategory.fields[i];
      
      // Build the right type of column based on the schema
      var column = ColumnService.getColumn(field);

      self.columns.push(column);
    }

    // Checkbox column
    //self.columns.push({data: 'approved', type: 'checkbox'})
  }

  /**
   *
   */
  function getAvailableExtraCategories() {
    // TODO refactor this into a service
    $http.get('http://localhost:8080/domains/' + self.request.domain).then(function(response) {
      self.availableCategories = [];

      response.data.datasources.map(function(category) {

        // Only add the category if we aren't already using it
        if ($.grep(self.schema.categories, function(item){ return item.name == category.name; }).length == 0) {
          self.availableCategories.push(category.name);
        }
      });
    });
  }

  /**
   *
   * @param categoryName
   */
  function addNewCategory(categoryName) {
    console.log("adding category " + categoryName);

    var schemaLink = self.request._links.schema.href;

    if(schemaLink.indexOf('?categories') > -1) {
      schemaLink += ',' + categoryName;
    } else {
      schemaLink += '?categories=' + categoryName;
    }

    // TODO refactor this into a service
    $http.get(schemaLink).then(function(response) {
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

      function(error) {
        console.log('error fetching schema: ' + error);
      });
  }
  
  function search(query) {
    
    var result = self.hot.search.query(query);
    //self.hot.loadData(result);
  }

  /**
   *
   */
  function save() {
    var request = self.request;

    RequestService.saveRequest(request).then(function() {
      console.log('saved request');
    }, function(error) {
      console.log('error saving request');
    });
  }

  /**
   * AAAAAARRRGGGHHHHHHHHH
   *
   * 16 is a magic number
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

    var height = $(window).height() - mainHeader.outerHeight() - requestHeader.outerHeight() - toolbar.outerHeight() - footer.outerHeight();

    table.height(height + 'px');
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
}