'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:SearchController
 * @description # SearchController Controller of the modesti
 */
angular.module('modesti').controller('SearchController', SearchController);

function SearchController($location, $stateParams, $http) {
  var self = this;

  self.requests = [];
  self.searching = 'started';

  self.search = search;
  self.editRequest = editRequest;

  search($stateParams.q);

  /**
   *
   */
  function search(query) {
    self.q = query;
    console.log('searching for ' + self.q);

    // TODO refactor this into a service
    $http.get(BACKEND_BASE_URL + '/requests/search/findAllByOrderByScoreDesc', {
      params: {
        'q': self.q
      }
    }).then(function (response) {

      if (response.data.hasOwnProperty('_embedded')) {
        self.requests = response.data._embedded.requests;
        console.log('got ' + self.requests.length + ' results');
      } else {
        self.requests = [];
        console.log('no results');
      }

      self.searching = 'success';
    });
  }

  /**
   *
   */
  function editRequest(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1);

    $location.path('/requests/' + id);
  }
}
