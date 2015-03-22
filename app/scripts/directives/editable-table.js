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
      type : 'text'
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

    // $scope.$watch("categories", function(newValue, oldValue) {
    // console.log('categories changed, refreshing table');
    // $scope.tableParams.reload();
    // }, true);
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
        html += '<option value="field.options[i]">field.options[i]</option>'
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

// app.directive('tableHeader', function() {
// return {
// restrict : 'A',
// replace : true,
// scope : {
// categories : '=categories'
// },
//
// link : function(scope, element, attrs) {
//
// var currentElement = element;
//
// scope.$watch("categories", function(newValue, oldValue) {
// // This gets called when data changes.
// console.log('change 2');
// var html = '';
//
// var categories = scope.categories;
//
// for ( var i in categories) {
// if (categories.hasOwnProperty(i)) {
//
// if (categories[i].active) {
// var fields = categories[i].fields;
//
// for ( var j in fields) {
// if (fields.hasOwnProperty(j)) {
// html += '<th>' + fields[j].name + '</th>';
// }
// }
// }
// }
// }
//
// /*
// * angular.forEach(scope.categories, function(category, index) {
// *
// * if (category.active) { angular.forEach(category.fields,
// * function(field, index) { html += '<th>' + field.name + '</th>';
// * }); } });
// */
//
// // var replacementElement = angular.element(html);
// // currentElement.replaceWith(replacementElement);
// // currentElement = replacementElement;
// element[0].innerHTML = html;
// console.log(html)
// // element.replaceWith(html);
//
// }, true);
//
// }
// }
// });

app.directive('tableRow', function() {
  return {
    restrict : 'A',
    replace : true,
    scope : {
      point : '=point',
      categories : '=categories',
      tableParams : '=params'
    },

    link : function(scope, element, attrs) {
      var currentElement = element;

      scope.$watch("categories", function(newValue, oldValue) {
        console.log('categories changed');
        var html = '';

        var categories = scope.categories;

        for ( var i in categories) {
          if (categories.hasOwnProperty(i)) {

            if (categories[i].active) {
              var fields = categories[i].fields;

              for ( var j in fields) {
                if (fields.hasOwnProperty(j)) {
                  var field = fields[j];

                  html += '<td data-title="\'' + field.name + '\'">' + scope.point.name + '</td>';
                }
              }
            }
          }
        }

        // angular.forEach(scope.categories, function(category, index) {
        //
        // if (category.active) {
        // angular.forEach(category.fields, function(field, index) {
        // html += '<td>' + scope.point[field.name] + '</td>';
        // });
        // }
        // });

        var replacementElement = angular.element(html);
        currentElement.html(replacementElement);
        currentElement = replacementElement;

        console.log('reloading table');
        scope.tableParams.reload();
      }, true);

      // console.log('initialising table');
      // scope.tableParams.reload();
    }
  }
});
