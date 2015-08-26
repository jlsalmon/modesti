'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:DelegationModalController
 * @description # DelegationModalController
 */
angular.module('modesti').controller('DelegationModalController', DelegationModalController);

function DelegationModalController($modalInstance, $http, request) {
  var self = this;

  self.request = request;

  self.ok = ok;
  self.cancel = cancel;
  self.getUsers = getUsers;

  function ok() {
    $modalInstance.close(self.assignee);
  }

  function cancel() {
    $modalInstance.dismiss('cancel');
  }

  /**
   *
   */
  function getUsers(query) {
    return $http.get(BACKEND_BASE_URL + '/persons/search/findByIdOrName', {
      params : {
        id : query,
        name: query
      }
    }).then(function(response) {
      if (!response.data.hasOwnProperty('_embedded')) {
        return [];
      }

      return response.data._embedded.persons;
    });
  }
}
