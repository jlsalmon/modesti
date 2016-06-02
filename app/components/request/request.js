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
  self.navigateToField = navigateToField;


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

  /**
   * Navigate somewhere to focus on a particular field.
   *
   * @param categoryName
   * @param fieldId
   */
  function navigateToField(categoryName, fieldId) {

    // Find the category which contains the field.
    var category;

    if (fieldId.indexOf('.') !== -1) {
      fieldId = fieldId.split('.')[0];
    }

    self.schema.categories.concat(self.schema.datasources).forEach(function (cat) {
      if (cat.name === categoryName || cat.id === categoryName) {
        cat.fields.forEach(function (field) {
          if (field.id === fieldId || cat.name === fieldId || cat.id === fieldId) {
            category = cat;
          }
        });
      }
    });

    if (category) {
      activateCategory(category);
    }
  }
}
