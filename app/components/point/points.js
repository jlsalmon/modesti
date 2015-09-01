'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:PointsController
 * @description # PointsController
 */
angular.module('modesti').controller('PointsController', PointsController);

function PointsController(PointService) {
  var self = this;

  PointService.getPoints().then(function (points) {
    self.points = points;
  });
}
