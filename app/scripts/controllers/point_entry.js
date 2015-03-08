'use strict';

/**
 * @ngdoc function
 * @name verity.controller:PointEntryController
 * @description # PointEntryController Controller of the verity
 */
var app = angular.module('verity');

app.controller('PointEntryController', function($scope, NgTableParams, RequestService) {

  $scope.tableParams = new NgTableParams({
    page : 1, // show first page
    count : 10, // count per page
    sorting : {
      name : 'asc'
    }
  }, {
    total : 0, // length of data
    getData : function($defer, params) {
      RequestService.getData($defer, params, $scope.filter).then(function(request) {
        $scope.request = request;
        $defer.resolve(request.points);
        console.log('got request');
      });
    }
  });

  $scope.$watch("filter.$", function() {
    $scope.tableParams.reload();
  });

  $scope.selected = undefined;

  $scope.checkboxes = {
    'checked' : false,
    items : {}
  };

  // watch for check all checkbox
  $scope.$watch('checkboxes.checked', function(value) {
    if (!$scope.request) {
      return;
    }

    angular.forEach($scope.request.points, function(item) {
      if (angular.isDefined(item.name)) {
        $scope.checkboxes.items[item.name] = value;
      }
    });
  });

  // watch for data checkboxes
  $scope.$watch('checkboxes.items', function(values) {
    if (!$scope.request) {
      return;
    }
    
    var checked = 0, unchecked = 0, total = $scope.request.points.length;
    angular.forEach($scope.request.points, function(item) {
      checked += ($scope.checkboxes.items[item.name]) || 0;
      unchecked += (!$scope.checkboxes.items[item.name]) || 0;
    });
    
    if ((unchecked == 0) || (checked == 0)) {
      $scope.checkboxes.checked = (checked == total);
    }
    
    // grayed checkbox
    angular.element(document.getElementById("select_all")).prop("indeterminate", (checked != 0 && unchecked != 0));
  }, true);

  $scope.getLocation = function(val) {
    return $http.get('http://maps.googleapis.com/maps/api/geocode/json', {
      params : {
        address : val,
        sensor : false
      }
    }).then(function(response) {
      return response.data.results.map(function(item) {
        return item.formatted_address;
      });
    });
  };

  $scope.addRow = function() {
    var newThing = {
      'name' : 'DEFAUT_GENERAL',
      'description' : 'No-so-cool description',
      'domain' : 'TIM'
    };

    var request = $scope.request;
    var points = request.points;
    points.push(newThing);
    console.log('adding new row');

    request.save();
  };

  $scope.save = function() {
    var request = $scope.request;
    request.save();
    console.log('saved request');
  }
});
