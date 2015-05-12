'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:ModestiTableController
 * @description # ModestiTableController Controller of the modesti
 */
angular.module('modesti').controller('ModestiTableController', ModestiTableController);

function ModestiTableController($scope, $http, $stateParams, NgTableParams, RequestService) {
  var self = this;

  self.request = {};
  self.schema = {};
  self.task = {}
  self.tableForm = {};
  self.pointForms = {};
  self.searchText = {};

  self.tableParams = new NgTableParams({
    page : 1,
    count : 10,
    sorting : {id : 'asc'}
  }, {
    total : 0,
    filterDelay : 0,
    $scope: self, // see https://github.com/esvit/ng-table/issues/362
    getData : getTableData
  });

  self.checkboxes = {
    'checked' : false,
    items : {}
  };

  self.availableCategories = [];

  self.init = init;
  self.activateCategory = activateCategory;
  self.addNewCategory = addNewCategory;
  self.getAvailableCategories = getAvailableCategories;
  self.getActiveCategory = getActiveCategory;
  self.save = save;
  self.toggleFilter = toggleFilter;
  self.getSortingClass = getSortingClass;
  self.toggleSorting = toggleSorting;

  /**
   *
   * @param request
   * @param schema
   */
  function init(request, schema, task) {
    self.request = request;
    self.schema = schema;
    self.task = task;
    getAvailableCategories();
  }

  /**
   *
   * @param $defer
   * @param params
   */
  function getTableData($defer, params) {
    console.log('getting table data');
    var id = $stateParams.id;

    // If we already have a request, send it to the service for merging,
    // as we might have made unsaved changes.
    var unsavedRequest = self.request ? self.request : undefined;

    RequestService.getRequest(id, params, unsavedRequest).then(function(request) {
      self.request = request;
      console.log('got request (with ' + request.points.length + ' points)');

      // Set total for pagination
      params.total(request.points.length);

      // Slice the points into pages and resolve the promise
      $defer.resolve(request.points.slice((params.page() - 1) * params.count(), params.page() * params.count()));
    },

    function(error) {
      console.log('error getting request: ' + error);
    });
  }

  /**
   *
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
  }

  /**
   *
   * @param category
   */
  function addNewCategory(category) {
    console.log("adding category " + category);

    var schemaLink = self.request._links.schema.href;

    if(schemaLink.indexOf('?categories') > -1) {
      schemaLink += ',' + category;
    } else {
      schemaLink += '?categories=' + category;
    }

    // TODO refactor this into a service
    $http.get(schemaLink).then(function(response) {
      console.log('fetched new schema: ' + response.data.name);
      self.schema = response.data;
      self.request._links.schema.href = schemaLink;
      getAvailableCategories();
    },

    function(error) {
      console.log('error fetching schema: ' + error);
    });
  }

  /**
   *
   */
  function getAvailableCategories() {
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
   */
  function getActiveCategory() {
    for (var key in categories) {
      if (categories.hasOwnProperty(key)) {
        if (categories[key].active) {
          return categories[key];
        }
      }
    }
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
   *
   */
  function toggleFilter() {
    self.tableParams.settings().$scope.show_filter = !self.tableParams.settings().$scope.show_filter;
  }

  /**
   *
   * @param property
   */
  function getSortingClass(property) {
    if (self.tableParams.isSortBy(property, 'asc')) return 'sort-asc';
    if (self.tableParams.isSortBy(property, 'desc')) return 'sort-desc';
  }

  /**
   *
   * @param property
   */
  function toggleSorting(property) {
    self.tableParams.sorting(property, self.tableParams.isSortBy(property, 'asc') ? 'desc' : 'asc');
  }

  // TODO: remove these watches and use ng-change instead

  $scope.$watch("ctrl.searchText", function() {
    if (!jQuery.isEmptyObject(self.searchText) && self.tableParams) {
      self.tableParams.filter({properties: self.searchText});
    }
  }, true);

  // watch for check all checkbox
  $scope.$watch('ctrl.checkboxes.checked', function(value) {
    if (!self.request) {
      return;
    }

    angular.forEach(self.request.points, function(point) {
      if (angular.isDefined(point.id)) {
        self.checkboxes.items[point.id] = value;
      }
    });
  });

  // watch for data checkboxes
  $scope.$watch('ctrl.checkboxes.items', function(values) {
    if (!self.request) {
      return;
    }

    var checked = 0, unchecked = 0, total = self.request.points.length;
    angular.forEach(self.request.points, function(point) {
      checked += (self.checkboxes.items[point.id]) || 0;
      unchecked += (!self.checkboxes.items[point.id]) || 0;
    });

    if ((unchecked == 0) || (checked == 0)) {
      self.checkboxes.checked = (checked == total);
    }

    self.checkboxes.dirty = checked != 0 ? true : false;

    // greyed checkbox
    angular.element(document.getElementById("select_all")).prop("indeterminate", (checked != 0 && unchecked != 0));
  }, true);
}