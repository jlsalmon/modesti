'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:PointsController
 * @description # PointsController
 */
angular.module('modesti').controller('PointsController', PointsController);

function PointsController(schemas, PointService) {
  var self = this;

  self.schemas = schemas;
  self.domains = ['TIM', 'CSAM', 'WINCC']; // TODO retrieve this dynamically
  self.points = [];
  self.query = '';

  self.useDomain = useDomain;
  self.search = search;

  // Load default domain
  useDomain(self.domains[1]);

  function useDomain(domain) {
    self.schemas.forEach(function (schema) {
      if (schema.id === domain) {
        self.schema = schema;
      }
    })
  }

  function search() {
    PointService.getPoints(self.query).then(function (points) {
      self.points = points;
      self.error = undefined;
    },

    function (error) {
      self.points = [];
      self.error = error;
    });
  }
}
