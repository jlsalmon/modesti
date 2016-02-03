'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:PointsController
 * @description # PointsController
 */
angular.module('modesti').controller('PointsController', PointsController);

function PointsController($modal, $state, schemas, PointService, SchemaService, RequestService, AlertService, Utils) {
  var self = this;

  self.schemas = schemas;
  self.domains = schemas.map(function (schema) { return schema.id; });
  self.points = [];

  self.filters = {}; // { 'pointDatatype': { /*operation: 'equals',*/ value: 'Boolean' } };

  self.page = {number: 0, size: 15};
  self.sort = 'pointId,desc';

  self.useDomain = useDomain;
  self.search = search;
  self.onPageChanged = onPageChanged;
  self.activateCategory = activateCategory;
  self.queryFieldValues = queryFieldValues;
  self.updatePoints = updatePoints;
  self.getOptionValue = getOptionValue;
  self.getOptionDisplayValue = getOptionDisplayValue;
  self.calculateTableHeight = calculateTableHeight;

  calculateTableHeight();

  // Load TIM domain by default
  self.domains.forEach(function (domain) {
    if (domain === 'TIM') {
      useDomain(domain);
    }
  });

  AlertService.add('warning', '<b><i class="icon fa fa-ban"></i>Warning!</b> This functionality is still in alpha stage. Use at your own risk.');

  self.activeCategory = self.schema.categories[0];
  search();

  /**
   *
   * @param domain
   */
  function useDomain(domain) {
    self.schemas.forEach(function (schema) {
      if (schema.id === domain) {
        self.schema = schema;
        self.activeCategory = self.schema.categories[0];
        self.filters = {};
        search();
      }
    });
  }

  /**
   *
   */
  function search() {
    self.loading = 'started';
    console.log('searching');

    parseQuery();

    PointService.getPoints(self.schema.id, self.query, self.page.number, self.page.size, self.sort).then(function (response) {
      if (response.hasOwnProperty('_embedded')) {
        self.points = response._embedded.points;
      } else {
        self.points = [];
      }

      console.log('fetched ' + self.points.length + ' points');

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

    Object.keys(self.filters).forEach(function(id) {
      var filter = self.filters[id];
      var field = Utils.getField(self.schema, id);

      //if (typeof filter.field === 'string') {
      //  filter.field = JSON.parse(filter.field);
      //}

      if (filter.value !== null && filter.value !== undefined && filter.value !== '') {

        var property;
        if (field.type === 'autocomplete') {
          var modelAttribute = field.model ? field.model : 'value';
          property = id + '.' + modelAttribute;
        } else {
          property = id;
        }

        var operation = parseOperation(filter.operation);
        var expression = property + ' ' + operation + ' "' + filter.value + '"';

        if (expressions.indexOf(expression) === -1) {
          expressions.push(expression);
        }
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
    } else {
      return ' == ';
    }
  }

  /**
   *
   */
  function updatePoints() {
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/point/modals/update-points-modal.html',
      controller: 'UpdatePointsModalController as ctrl',
      size: 'lg',
      resolve: {
        points: function () {
          return self.points;
        },
        schema: function () {
          return self.schema;
        }
      }
    });

    modalInstance.result.then(function (request) {
      console.log('creating update request');

      self.submitting = 'started';

      // Post form to server to create new request.
      RequestService.createRequest(request).then(function(location) {
          // Strip request ID from location.
          var id = location.substring(location.lastIndexOf('/') + 1);
          // Redirect to point entry page.
          $state.go('request', { id: id }).then(function () {
            self.submitting = 'success';

            AlertService.add('success', 'Update request #' + id + ' has been created.');
          });
        },

        function () {
          self.submitting = 'error';
        });
    });
  }

  /**
   *
   */
  function onPageChanged() {
    search();
  }

  /**
   *
   * @param category
   */
  function activateCategory(category) {
    console.log('activating category "' + category.id + '"');
    self.activeCategory = category;
    //$localStorage.lastActiveCategory[self.request.requestId] = category;
    //getColumns();
  }

  /**
   *
   * @param field
   * @param value
   * @returns {*}
   */
  function queryFieldValues(field, value) {
    return SchemaService.queryFieldValues(field, value);
  }

  /**
   *
   * @param option
   * @returns {*}
   */
  function getOptionValue(option) {
    return typeof option === 'object' ? option.value : option;
  }

  /**
   *
   * @param option
   * @returns {string}
   */
  function getOptionDisplayValue(option) {
    return typeof option === 'object' ? option.value + (option.description ? ': ' + option.description : '') : option;
  }

  /**
   * Calculate the required height for the table so that it fills the screen.
   */
  function calculateTableHeight() {
    var mainHeader = $('.main-header');
    var requestHeader = $('.request-header');
    var toolbar = $('.toolbar');
    var table = $('.table-wrapper');
    //var log = $('.log');
    var footer = $('.footer');

    var height = $(window).height() - mainHeader.outerHeight() - requestHeader.outerHeight() - toolbar.outerHeight() - footer.outerHeight();

    console.log($(window).height());
    console.log(mainHeader.height());
    console.log(requestHeader.height());
    console.log(toolbar.height());
    console.log(footer.height());

    table.height(height + 'px');
  }
}
