'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:AssignmentModalController
 * @description # AssignmentModalController
 */
angular.module('modesti').controller('AssignmentModalController', AssignmentModalController);

function AssignmentModalController($modalInstance, $http, task) {
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

  function refreshUsers(query) {
    return $http.get(BACKEND_BASE_URL + '/users/search', {
      params : {
        query : parseQuery(query)
      }
    }).then(function(response) {
      if (!response.data.hasOwnProperty('_embedded')) {
        return [];
      }

      self.users = response.data._embedded.users;
    });
  }

  function parseQuery(query) {
    var q = 'authorities.authority =in= (' + task.candidateGroups.join() + ')';

    if (query.length !== 0) {
      q += ' and (username == ' + query;
      q += ' or firstName == ' + query;
      q += ' or lastName == ' + query + ')';
    }

    console.log('parsed query: ' + query);
    return q;
  }
}
