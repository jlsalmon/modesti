'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:SearchController
 * @description # SearchController Controller of the modesti
 */
var app = angular.module('modesti');

app.controller('SearchController', function($scope, $location, $routeParams,Restangular) {
    var q = $routeParams.q;
    console.log('searching for ' + q);
    
    // TODO refactor this into a service
    Restangular.all('requests/search/findAllByOrderByScoreDesc').getList({"q": q}).then(function(requests) {
      console.log('got ' + requests.data.length + ' results');
      $scope.results = requests.data;
    });
});
