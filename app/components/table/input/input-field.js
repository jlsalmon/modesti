'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:InputFieldController
 * @description # InputFieldController Controller of the modesti
 */
angular.module('modesti').controller('InputFieldController', InputFieldController);
    
function InputFieldController($compile, $http, $filter) {
  var self = this;

  self.autocomplete = autocomplete;

  /**
   * 
   */
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
    var html = '<input type="text" name="{{schema.id}}" class="form-control" ';
    if (schema.model) {
      // Field is an object, so we should bind the model property specified in
      // the schema
      html += 'ng-model="model[schema.model]" ';
    } else {
      // Field is a simple string, so we should just bind the model directly
      html += 'ng-model="model" '
    }
    return html += (schema.required ? 'required' : '') + ' />'
  }
  
  /**
   * 
   */
  function getSelectInput(schema, model) {
    self.options = [];
    var html = '<select ng-model="model[schema.model]" name="{{schema.id}}" class="form-control" ';

    if( typeof schema.options === 'string' ) {
      // Options given as a URL
      html += 'ng-options="option for option in ctrl.options track by option" ';

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
      html += 'ng-options="option for option in schema.options track by option" ';
    }

    return html += (schema.required ? 'required' : '') + ' />'
  }
  
  /**
   * 
   */
  function getTypeaheadInput(schema, model) {
    var template = '\
      <div class="form-group has-feedback"> \
        <input type="text" class="form-control" name="{{schema.id}}" ng-model="model" \
               placeholder="' + schema.placeholder + '"  \
               typeahead="item as item.' + schema.model + ' for item in ctrl.autocomplete(schema, $viewValue)" \
               typeahead-editable="false" \
               typeahead-loading="loading" \
               typeahead-template-url="item-template-' + schema.id + '.html" \
               typeahead-min-length="{{schema.minLength}}" \
               ' + (schema.required ? 'required' : '') + '> \
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