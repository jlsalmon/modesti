'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:DelegationModalController
 * @description # DelegationModalController
 */
angular.module('modesti').controller('DelegationModalController', DelegationModalController);

function DelegationModalController($modalInstance, $http, task) {
  var self = this;

  self.task = task;
  self.users = [];

  self.ok = ok;
  self.cancel = cancel;
  self.refreshUsers = refreshUsers;

  function ok() {
    $modalInstance.close(self.assignee);
  }

  function cancel() {
    $modalInstance.dismiss('cancel');
  }

  /**
   * TODO: which authority to assign to? I guess all...
   */
  function refreshUsers(query) {
    return $http.get(BACKEND_BASE_URL + '/users/search/find', {
      params : {
        query : query,
        authority: task.candidateGroups[1]
      }
    }).then(function(response) {
      if (!response.data.hasOwnProperty('_embedded')) {
        return [];
      }

      self.users = response.data._embedded.users;
    });
  }
}
