'use strict';

/**
 * @ngdoc directive
 * @name modesti.directive:inputField
 * @description
 * # inputField
 */
angular.module('modesti').directive('inputField', inputField);

function inputField() {
  return {
    restrict : 'A',
    controller : 'InputFieldController as ctrl',
    scope : {
      schema : '=schema',
      model  : '=model'
    },

    link : function(scope, element, attrs, controller) {
      controller.init(scope, element);
    }
  };
}

// TODO move this controller to a separate file
angular.module('modesti').controller('InputFieldController', InputFieldController);
    
function InputFieldController($compile, $http, $filter) {
  var self = this;

  self.autocomplete = autocomplete;

  self.init = function(scope, element) {
    element.html(getInput(scope.schema, scope.model)).show();
    $compile(element.contents())(scope);
  };
  
  /**
   * 
   */
  function getInput(schema, model) {
    if (schema.type == 'text') {
      return getTextInput(schema, model);
    }
    
    else if (schema.type == 'select') {
      return getSelectInput(schema, model);
    }
    
    else if (schema.type == 'typeahead') {
      return getTypeaheadInput(schema, model);
    }
  }
  
  /**
   * 
   */
  function getTextInput(schema, model) {
    if (schema.model) {
      // Field is an object, so we should bind the model property specified in
      // the schema
      return '<input type="text" ng-model="model[schema.model]" class="form-control" />'
    } else {
      // Field is a simple string, so we should just bind the model directly
      return '<input type="text" ng-model="model" class="form-control" />'
    }
  }
  
  /**
   * 
   */
  function getSelectInput(schema, model) {
    self.options = [];

    if( typeof schema.options === 'string' ) {
      // Options given as a URL
      var html = '<select ng-model="model[schema.model]" ng-options="option for option in ctrl.options track by option" class="form-control" />';

      // TODO refactor this into a service
      $http.get(schema.options).then(function(response) {
        if (!response.data.hasOwnProperty('_embedded')) return [];
        
        response.data._embedded[schema.returnPropertyName].map(function(item) {
          self.options.push(item[schema.model]);
        });
        
        
      });
    }
    
    else {
      // Options given as inline array
      var html = '<select ng-model="model[schema.model]" ng-options="option for option in schema.options track by option" class="form-control" />';
    }

    return html;
  }
  
  /**
   * 
   */
  function getTypeaheadInput(schema, model) {
    var template = '\
      <div class="form-group has-feedback"> \
        <input type="text" class="form-control" ng-model="model" placeholder="' + schema.placeholder + '"  \
               typeahead="item as item.' + schema.model + ' for item in ctrl.autocomplete(schema, $viewValue)" \
               typeahead-editable="false" \
               typeahead-loading="loading" \
               typeahead-template-url="item-template-' + schema.id + '.html"> \
      </div>' // <i ng-show="loading" class="fa fa-fw fa-spin fa-refresh form-control-feedback"></i> \
               
    var itemTemplate = '\
      <script type="text/ng-template" id="item-template-' + schema.id + '.html"> \
        <a><span bind-html-unsafe="match.label | typeaheadHighlight:query"></span>';
    
    if (schema.template) {
      itemTemplate += schema.template;
    }
    
    itemTemplate += '</a></script>';
    return template + itemTemplate;
  }
  
  /**
   * 
   */
  function autocomplete(schema, value) {
    var params = {}
    for (var i in schema.params) {
      params[schema.params[i]] = value;
    }
    
    // TODO refactor this into a service
    return $http.get(schema.url, {
      params : params
    }).then(function(response) {
      
      if (!response.data.hasOwnProperty('_embedded')) {
        var empty = {};
        empty[schema.model] = 'No results';
        return [empty];
      }

      return response.data._embedded[schema.returnPropertyName].map(function(item){
        return item;
      });
    });
  }
};