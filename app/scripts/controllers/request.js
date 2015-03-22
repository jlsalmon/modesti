'use strict';

/**
 * @ngdoc function
 * @name verity.controller:RequestController
 * @description # RequestController Controller of the verity
 */
var app = angular.module('verity');

app.controller('RequestController', function($scope, $location, $routeParams, NgTableParams, RequestService) {

  $scope.tableParams = new NgTableParams({
    page : 1, // show first page
    count : 10, // count per page
    sorting : {}
  }, {
    total : 0, // length of data
    filterDelay : 0,
    getData : function($defer, params) {
      var id = $routeParams.id;

      RequestService.getRequest(id, params, params.filter()).then(function(request) {
        $scope.request = request;
        $defer.resolve(request.points);
        console.log('got request');
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
    var request = $scope.request;

    var newRow = {
      'name' : '',
      'description' : '',
      'domain' : request.domain
    };

    var points = request.points;
    points.push(newRow);
    console.log('adding new row');

    request.save().then(function() {
      console.log('added new row');
    }, function(error) {
      console.log('error adding new row: ' + error);
    });
  };

  $scope.save = function() {
    var request = $scope.request;
    request.save().then(function() {
      console.log('saved request');
    }, function(error) {
      console.log('error saving request: ' + error);
    });
  };

  $scope.toggleFilter = function(params) {
    params.settings().$scope.show_filter = !params.settings().$scope.show_filter;
  };
});
