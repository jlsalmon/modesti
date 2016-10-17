'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestController
 * @description # RequestController
 */
angular.module('modesti').controller('RequestController', RequestController);

function RequestController(request, children, schema, tasks, signals) {
  var self = this;

  self.request = request;
  self.children = children;
  self.schema = schema;
  self.tasks = tasks;
  self.signals = signals;
  //self.history = history;

  /** The handsontable instance */
  self.table = {};


  self.isInvalidCategory = isInvalidCategory;



  /**
   * Return true if the given category is "invalid", i.e. there are points in
   * the current request that have errors that relate to the category.
   *
   * @param category
   */
  function isInvalidCategory(category) {
    var fieldIds = category.fields.map(function (field) {return field.id;});
    var invalid = false;

    self.request.points.forEach(function (point) {
      if (point.errors && point.errors.length > 0) {
        point.errors.forEach(function (error) {
          if (!error.category) {
            var property = error.property.split('.')[0];

            if (fieldIds.indexOf(property) !== -1) {
              invalid = true;
            }
          }

          else if (error.category === category.name || error.category === category.id) {
            invalid = true;
          }
        });
      }
    });

    return invalid;
  }
}
