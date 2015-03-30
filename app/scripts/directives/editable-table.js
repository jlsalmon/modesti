'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:editableTable
 * @description # editableTable
 */
var app = angular.module('modesti');

app.directive('editableTable', function() {

  return {
    templateUrl : 'views/templates/editable-table.html',
    restrict : 'A',
    controller : 'EditableTableController',
    scope: {
      request: '=request'
    },

    link : function(scope, element, attrs, controller) {
      controller.init();
    }
  };
});

app.controller('EditableTableController', function($scope, $location, $http, $routeParams, NgTableParams, RequestService) {

  this.init = function() {
  };

  $scope.table = {};
  $scope.searchText = {}
  
  $scope.$watch("searchText", function () {
    if(!jQuery.isEmptyObject($scope.searchText) && $scope.tableParams){
        $scope.tableParams.filter($scope.searchText)
    }
  }, true);

  $http.get('data/core-fields.json').then(function(response) {
    $scope.table.categories = response.data;
  });

  $scope.activateCategory = function(category) {
    console.log('activating category');
    var categories = $scope.table.categories;

    for ( var key in categories) {
      if (categories.hasOwnProperty(key)) {
        categories[key].active = false;
        console.log('category:' + categories[key]);
      }
    }

    category.active = true;
    console.log(category);
  };

  $scope.getActiveFields = function() {
    var categories = $scope.table.categories;
    var fields = [];

    for ( var key in categories) {
      if (categories.hasOwnProperty(key)) {
        var category = categories[key];

        if (category.active) {
          fields = category.fields;
        }
      }
    }

    return fields;
  };
  
  $scope.tableParams = new NgTableParams({
    page : 1, // show first page
    count : 10, // count per page
    sorting : {}
  }, {
    total : 0, // length of data
    filterDelay : 0,
    $scope: $scope, // see https://github.com/esvit/ng-table/issues/362
    
    getData : function($defer, params) {
      console.log('getData() called');
      var id = $routeParams.id;
      
      // If we already have a request, send it to the service for merging,
      // as we might have made unsaved changes.
      var unsavedRequest = $scope.request ? $scope.request : undefined;

      RequestService.getRequest(id, params, unsavedRequest).then(function(request) {
        $scope.request = request;
        console.log('got request (with ' + request.points.length + ' points)');
        
        // Set total for pagination
        params.total(request.points.length);

        // Slice the points into pages and resolve the promise
        $defer.resolve(request.points.slice((params.page() - 1) * params.count(), params.page() * params.count()));
      },

      function(error) {
        console.log('error getting request: ' + error);
        $location.path('/404');
      });
    }
  });

  $scope.checkboxes = {
    'checked' : false,
    items : {}
  };

  // watch for check all checkbox
  $scope.$watch('checkboxes.checked', function(value) {
    if (!$scope.request) {
      return;
    }

    angular.forEach($scope.request.points, function(point) {
      if (angular.isDefined(point.name)) {
        $scope.checkboxes.items[point.id] = value;
      }
    });
  });

  // watch for data checkboxes
  $scope.$watch('checkboxes.items', function(values) {
    if (!$scope.request) {
      return;
    }

    var checked = 0, unchecked = 0, total = $scope.request.points.length;
    angular.forEach($scope.request.points, function(point) {
      checked += ($scope.checkboxes.items[point.id]) || 0;
      unchecked += (!$scope.checkboxes.items[point.id]) || 0;
    });

    if ((unchecked == 0) || (checked == 0)) {
      $scope.checkboxes.checked = (checked == total);
    }

    // grayed checkbox
    angular.element(document.getElementById("select_all")).prop("indeterminate", (checked != 0 && unchecked != 0));
  }, true);

  /**
   * 
   */
  $scope.addRow = function() {
    console.log('adding new row');
    var request = $scope.request;

    var newRow = {
      'name' : '',
      'description' : '',
      'domain' : request.domain
    };

    request.points.push(newRow);

    RequestService.saveRequest(request).then(function() {
      console.log('added new row');
      
      // Reload the table data
      $scope.tableParams.reload();
      
      // Move to the last page
      var pages = $scope.tableParams.settings().$scope.pages;
      for (var i in pages) {
        if (pages[i].type == "last") {
          $scope.tableParams.page(pages[i].number);
        }
      }

    }, function(error) {
      console.log('error adding new row: ' + error);
    });
  };
  
  /**
   * 
   */
  $scope.duplicateSelectedRows = function() {
    console.log('duplicating rows');
    
    // TODO
  }

  /**
   * 
   */
  $scope.save = function() {
    var request = $scope.request;

    RequestService.saveRequest(request).then(function() {
      console.log('saved request');
    }, function(error) {
      console.log('error saving request: ' + error);
    });
  };

  /**
   * 
   */
  $scope.toggleFilter = function(params) {
    params.settings().$scope.show_filter = !params.settings().$scope.show_filter;
  };
});
