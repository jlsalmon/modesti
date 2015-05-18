'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:inputField
 * @description
 * # inputField
 */
angular.module('modesti').directive('inputField', inputField);

function inputField($http, $compile, $templateCache) {



  var template = '<div ng-switch="::schema.type">\
      <div ng-switch-when="text">\
      <input ng-model="::model"\
    type="text"\
  class="form-control"\
    name="{{::schema.id}}"\
    ng-minlength="{{::schema.minLength}}"\
    ng-maxlength="{{::schema.maxLength}}"\
    ng-readonly="{{::editable}}">\
        </input>\
        </div>\
        <div ng-switch-when="select">\
        <select ng-model="::model"\
    class="form-control"\
      name="{{::self.schema.id}}"\
      ng-options="option for option in ::options track by option">\
          </select>\
          </div>\
          <div ng-switch-when="typeahead">\
    <div class="form-group has-feedback">\
      <input type="text" class="form-control" name="{{::schema.id}}" />\
      </div>\
          </div>\
          </div>';
  var compiled = $compile(template);


  return {
    restrict : 'A',
    controller : 'InputFieldController as ctrl',
    scope : {
      schema : '=schema',
      model  : '=model',
      editable : '@editable'
    },

    //templateUrl : 'components/table/input/input-field.html',

    //compile : function(element, attrs) {
    //
    //
    //},

    link : function(scope, element, attrs, controller) {
      //// TODO refactor this into a service
      //console.log('link');
      //if (scope.schema.options) {
      //  $http.get(scope.schema.options, {cache: true}).then(function (response) {
      //    if (!response.data.hasOwnProperty('_embedded')) return [];
      //
      //    scope.options = [];
      //    response.data._embedded[scope.schema.returnPropertyName].map(function (item) {
      //      scope.options.push(item[scope.schema.model]);
      //    });
      //  });
      //}

      //controller.init(scope, element);

      compiled(scope, function(clonedElement, scope) {
        element.append(clonedElement);
      });
    }
  };
}
