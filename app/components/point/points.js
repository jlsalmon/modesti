'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:PointsController
 * @description # PointsController
 */
angular.module('modesti').controller('PointsController', PointsController);

function PointsController(PointService) {
  var self = this;

  self.points = [];
  self.query = '';

  self.search = search;

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
