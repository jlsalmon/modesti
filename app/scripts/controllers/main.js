'use strict';

/**
 * @ngdoc function
 * @name yoTestApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the yoTestApp
 */
var app = angular.module('yoTestApp');

app.controller('MainCtrl', ['$scope', '$timeout', '$http', 'NgTableParams', 'Greeting', function ($scope, $timeout, $http, NgTableParams, Greeting) {
    
	  $scope.greetings = Greeting.query();
	  
	  var data = [{name: 'Moroni', age: 50},
	                {name: 'Tiancum', age: 43},
	                {name: 'Jacob', age: 27},
	                {name: 'Nephi', age: 29},
	                {name: 'Enos', age: 34},
	                {name: 'Tiancum', age: 43},
	                {name: 'Jacob', age: 27},
	                {name: 'Nephi', age: 29},
	                {name: 'Enos', age: 34},
	                {name: 'Tiancum', age: 43},
	                {name: 'Jacob', age: 27},
	                {name: 'Nephi', age: 29},
	                {name: 'Enos', age: 34},
	                {name: 'Tiancum', age: 43},
	                {name: 'Jacob', age: 27},
	                {name: 'Nephi', age: 29},
	                {name: 'Enos', age: 34}];

	  
  $scope.tableParams = new NgTableParams({
    page : 1, // show first page
    count : 10
  // count per page
  }, {
    total : data.length, // length of data
    getData : function($defer, params) {
      $defer.resolve(data.slice((params.page() - 1) * params.count(), params.page() * params.count()));
    }
  });
  
  
  $scope.today = function() {
    $scope.dt = new Date();
  };
  $scope.today();

  $scope.clear = function () {
    $scope.dt = null;
  };

  // Disable weekend selection
  $scope.disabled = function(date, mode) {
    return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
  };

  $scope.toggleMin = function() {
    $scope.minDate = $scope.minDate ? null : new Date();
  };
  $scope.toggleMin();

  $scope.open = function($event) {
    $event.preventDefault();
    $event.stopPropagation();

    $scope.opened = true;
  };

  $scope.dateOptions = {
    formatYear: 'yy',
    startingDay: 1
  };

  $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
  $scope.format = $scope.formats[0];
  
  
  $scope.selected = undefined;
  
  $scope.getLocation = function(val) {
    return $http.get('http://maps.googleapis.com/maps/api/geocode/json', {
      params: {
        address: val,
        sensor: false
      }
    }).then(function(response){
      return response.data.results.map(function(item){
        return item.formatted_address;
      });
    });
  };
  
//  $scope.forms = {};
//  
//  $timeout(function() {
//    for (var form in $scope.forms) {
//      if ($scope.forms.hasOwnProperty(form)) {
//        $scope.forms[form].$show();
//      }
//  }
////    for (var i = 0; i < $scope.forms.length; i++) {
////      $scope.forms[i].$show();
////    }
////    $scope.forms.myform.$show();
////    $scope.forms.myform2.$show();
//  }, 1000);

  }]);

app.run(function(editableOptions) {
  editableOptions.theme = 'bs3';
  editableOptions.blur = 'ignore'; // don't hide on blur
});
