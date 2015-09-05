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
  self.domains = schemas.map(function (schema) { return schema.id; });
  self.points = [];
  self.query = 'pointDatatype == Boolean';

  self.useDomain = useDomain;
  self.search = search;
  self.onPageChanged = onPageChanged;

  // Load default domain
  useDomain(self.domains[1]);

  /**
   *
   * @param domain
   */
  function useDomain(domain) {
    self.schemas.forEach(function (schema) {
      if (schema.id === domain) {
        self.schema = schema;
      }
    });
  }

  /**
   *
   * @param query
   * @param page
   * @param size
   * @param sort
   */
  function search(query, page, size, sort) {
    self.loading = 'started';

    if (!query) {
      return;
    }

    PointService.getPoints(query, page, size, sort).then(function (response) {
      if (response.hasOwnProperty('_embedded')) {
        self.points = response._embedded.refPoints;
      } else {
        self.points = [];
      }

      self.page = response.page;
      // Backend pages 0-based, Bootstrap pagination 1-based
      self.page.number += 1;

      angular.forEach(response._links, function (item) {
        if (item.rel === 'next') {
          self.page.next = item.href;
        }

        if (item.rel === 'prev') {
          self.page.prev = item.href;
        }
      });

      self.loading = 'success';
      self.error = undefined;
    },

    function (error) {
      self.points = [];
      self.loading = 'error';
      self.error = error;
    });
  }

  /**
   *
   */
  function onPageChanged() {
    search(self.query, self.page.number, self.page.size, "pointId,desc");
  }
}
