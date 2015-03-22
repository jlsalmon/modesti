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

  $scope.table.categories = [
      {
        name : 'Basic details',
        active : true,
        fields : [
            {
              name : 'name'
            }, {
              name : 'description'
            }, {
              name : 'domain'
            },
        ]
      }, {
        name : 'Location',
        active: false,
        fields : [
            {
              name : 'building'
            },
        ]
      }
  ];

  $scope.activateCategory = function(category) {
    console.log('activating category');
    var categories = $scope.table.categories;
    
    for (var key in categories) {
      if (categories.hasOwnProperty(key)) {
        categories[key].active = false;
        console.log('category:' + categories[key]);
      }
    }
    
    category.active = true;
    console.log(category);
  };
});

app.directive('tableHeader', function() {
  return {
    restrict : 'A',
    replace: true,
    scope : {
      categories : '=categories'
    },

    link : function(scope, element, attrs) {

      var currentElement = element;
      
      scope.$watch("categories", function(newValue, oldValue) {
        // This gets called when data changes.
        console.log('change 2');
        var html = '';
        
        var categories = scope.categories;
        
        for (var i in categories) {
          if (categories.hasOwnProperty(i)) {
            
            if (categories[i].active) {
              var fields = categories[i].fields;
              
              for (var j in fields) {
                if (fields.hasOwnProperty(j)) {
                  html += '<th>' + fields[j].name + '</th>';
                }
              }
            }
          }
        }
        
/*        angular.forEach(scope.categories, function(category, index) {

          if (category.active) {
            angular.forEach(category.fields, function(field, index) {
              html += '<th>' + field.name + '</th>';
            });
          }
        });*/

        var replacementElement = angular.element(html);
        currentElement.replaceWith(replacementElement);
        currentElement = replacementElement;
        
        console.log(html)
        //element.replaceWith(html);
        
      }, true);

    }
  }
});

app.directive('tableRow', function() {
  return {
    restrict : 'A',
    scope : {
      point : '=point',
      categories : '=categories'
    },

    link : function(scope, element, attrs) {
      var html = '';

      angular.forEach(scope.categories, function(category, index) {

        if (category.active) {
          angular.forEach(category.fields, function(field, index) {
            html += '<td>' + scope.point[field.name] + '</td>';
          });
        }
      });

      element.replaceWith(html)
    }
  }
});
