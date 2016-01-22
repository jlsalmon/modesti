'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:UpdatePointsModalController
 * @description # UpdatePointsModalController
 */
angular.module('modesti').controller('UpdatePointsModalController', UpdatePointsModalController);

function UpdatePointsModalController($http, $modalInstance, points, schema, AuthService) {
  var self = this;

  self.points = points;
  self.schema = schema;

  self.request = {
    type : 'UPDATE',
    domain : schema.id,
    description : '',
    creator : AuthService.getCurrentUser(),
    points: points
  };

  self.ok = ok;
  self.cancel = cancel;
  self.getSubsystems = getSubsystems;

  function ok() {
    $modalInstance.close(self.request);
  }

  function cancel() {
    $modalInstance.dismiss('cancel');
  }

  /**
   *
   */
  function getSubsystems(value) {
    return $http.get(BACKEND_BASE_URL + '/subsystems/search/find', {
      params : {
        query : value
      }
    }).then(function(response) {
      if (!response.data.hasOwnProperty('_embedded')) {
        return [];
      }

      return response.data._embedded.subsystems;
    });
  }
}
