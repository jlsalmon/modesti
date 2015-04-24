'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:SearchController
 * @description # SearchController Controller of the modesti
 */
angular.module('modesti').controller('SearchController', SearchController);

function SearchController($location, $stateParams, Restangular) {
  var self = this;
  
  self.results = [];
  
  var q = $stateParams.q;
  console.log('searching for ' + q);

  // TODO refactor this into a service
  Restangular.all('requests/search/findAllByOrderByScoreDesc').getList({
    "q" : q
  }).then(function(requests) {
    console.log('got ' + requests.data.length + ' results');
    self.results = requests.data;
    self.q = q;
  });
};
