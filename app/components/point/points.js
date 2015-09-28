'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:PointsController
 * @description # PointsController
 */
angular.module('modesti').controller('PointsController', PointsController);

function PointsController($scope, schemas, PointService) {
  var self = this;

  self.schemas = schemas;
  self.domains = schemas.map(function (schema) { return schema.id; });
  self.points = [];

  self.filters = [
    { property: 'pointDatatype', operation: 'equals', value: 'Boolean' }
  ];

  self.page = {number: 0, size: 15};
  self.sort = 'pointId,desc';

  self.useDomain = useDomain;
  self.addFilter = addFilter;
  self.deleteFilter = deleteFilter;
  self.search = search;
  self.onPageChanged = onPageChanged;

  // Load default domain
  useDomain(self.domains[1]);
  parseQuery();
  search();

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
   */
  function addFilter() {
    self.filters.push({ property: 'pointDatatype', operation: 'equals', value: 'Boolean' });
  }

  /**
   *
   * @param filter
   */
  function deleteFilter(filter) {
    self.filters.splice(self.filters.indexOf(filter), 1);
  }

  /**
   *
   * @param filters
   * @param page
   * @param size
   * @param sort
   */
  function search() {
    self.loading = 'started';
    console.log('searching');

    PointService.getPoints(self.query, self.page.number, self.page.size, self.sort).then(function (response) {
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
   * @returns {string}
   */
  function parseQuery() {
    var expressions = [];

    self.filters.forEach(function (filter) {
      var expression = filter.property + parseOperation(filter.operation) + filter.value;

      if (expressions.indexOf(expression) === -1) {
        expressions.push(expression);
      }
    });

    self.query = expressions.join(' and ');
    console.log('parsed query: ' + self.query);
  }

  /**
   *
   * @param operation
   * @returns {string}
   */
  function parseOperation(operation) {
    if (operation === 'equals') {
      return ' == ';
    }
  }

  /**
   *
   */
  function onPageChanged() {
    search();
  }


  $scope.$watch('ctrl.filters', function () {
    parseQuery();
  }, true);
}
