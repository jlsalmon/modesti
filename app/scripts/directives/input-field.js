'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:inputField
 * @description
 * # inputField
 */
var app = angular.module('modesti');

app.directive('inputField', function() {

  return {
    restrict : 'A',
    controller : 'InputFieldController',
    scope : {
      field : '=field',
      point : '=point'
    },

    link : function(scope, element, attrs, controller) {
      controller.init(scope, element);
    }
  };
});

app.controller('InputFieldController', function($scope, $compile, $http, $filter) {

  this.init = function(scope, element) {
    element.html(getInput(scope.field, scope.point)).show();
    $compile(element.contents())(scope);
  };
  
  $scope.autocomplete = function(url, value) {
    return $http.get(url, {
      params : {
        address : value,
        sensor : false
      }
    }).then(function(response) {
      return $filter('filter')(response.data, value);
    });
  };
  
  var getInput = function(field, point) {
    if (field.type == 'text') {
      return '<input type="text" ng-model="point[field.id]" class="form-control" />'
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
    
    else if (field.type == 'typeahead') {
      var template = '\
        <div class="form-group has-feedback"> \
          <input type="text" ng-model="point[field.id]" placeholder="' + field.placeholder + '"  \
                 typeahead="item for item in autocomplete(field.url, $viewValue)" \
                 typeahead-loading="loading" class="form-control"> \
          <i ng-show="loading" class="fa fa-fw fa-spin fa-refresh form-control-feedback"></i> \
        </div>'
        
      return template;
    }
  };
});