'use strict';

/**
 * @ngdoc directive
 * @name verity.directive:editableTable
 * @description # editableTable
 */
var app = angular.module('verity');

app.directive('editableTable', function(RequestService) {

  return {
    templateUrl : 'views/templates/editable-table.html',
    restrict : 'A',
    controller : 'EditableTableController',

    link : function(scope, element, attrs, controller) {
      controller.init();
    }

  };
});

app.controller('EditableTableController', function($scope) {

  this.init = function() {
  };

  $scope.table = {};

  $scope.table.categories = [ {
    name : 'Basic details',
    active : true,
    fields : [ {
      name : 'name',
      type : 'text'
    }, {
      name : 'description',
      type : 'text'
    }, {
      name : 'domain',
      type : 'select',
      options: ['TIM', 'PVSS', 'CSAM']
    }, ]
  }, {
    name : 'Location',
    active : false,
    fields : [ {
      name : 'building',
      type : 'typeahead',
      url: 'data/systems.json'
    }, ]
  } ];

  $scope.activateCategory = function(category) {
    console.log('activating category');
    var categories = $scope.table.categories;

    for ( var key in categories) {
      if (categories.hasOwnProperty(key)) {
        categories[key].active = false;
        console.log('category:' + categories[key]);
      }
    }

    category.active = true;
    console.log(category);
  };

  $scope.getActiveFields = function() {
    var categories = $scope.table.categories;
    var fields = [];

    for ( var key in categories) {
      if (categories.hasOwnProperty(key)) {
        var category = categories[key];

        if (category.active) {
          fields = category.fields;
        }
      }
    }

    return fields;
  };
});

app.directive('inputField', function($compile) {

  var getInput = function(field) {
    if (field.type == 'text') {
      return '<input type="text" ng-model="point[field.name]" class="form-control" />'
    }
    else if (field.type == 'select') {
      var html = '<select class="form-control">';
      
      for (var i in field.options) {
        var option = field.options[i];
        html += '<option value="' + option + '">' + option + '</option>'
      }
      
      html += '</select>'
      return html;
    }
  }

  return {
    restrict : 'A',
    replace : true,
    scope : {
      point : '=point',
      field : '=field'
    },

    link : function(scope, element, attrs) {
      element.html(getInput(scope.field)).show();

      $compile(element.contents())(scope);
    }
  }
});