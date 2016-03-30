'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:UpdatePointsModalController
 * @description # UpdatePointsModalController
 */
angular.module('modesti').controller('UpdatePointsModalController', UpdatePointsModalController);

function UpdatePointsModalController($modalInstance, points, schema, AuthService, SchemaService) {
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
  self.queryFieldValues = queryFieldValues;

  function ok() {
    $modalInstance.close(self.request);
  }

  function cancel() {
    $modalInstance.dismiss('cancel');
  }

  function queryFieldValues(field, query) {
    return SchemaService.queryFieldValues(field, query).then(function (values) {
      self.fieldValues = values;
    });
  }
}
