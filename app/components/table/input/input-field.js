'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:InputFieldController
 * @description # InputFieldController Controller of the modesti
 */
angular.module('modesti').controller('InputFieldController', InputFieldController);

function InputFieldController($compile, $http, $templateCache) {
  var self = this;

  self.schema = {};
  self.model = {};
  self.editable = false;

  self.autocomplete = autocomplete;

  /**
   *
   */
  self.init = function(scope, element) {
    self.schema = scope.schema;
    self.model = scope.model;
    self.editable = scope.editable;

    element.html(getInput()).show();
    $compile(element.contents())(scope);
  };

  /**
   *
   */
  function getInput() {
    if (self.schema.type == 'text') {
      return getTextInput();
    }

    else if (self.schema.type == 'select') {
      return getSelectInput();
    }

    else if (self.schema.type == 'typeahead') {
      return getTypeaheadInput();
    }
  }

  /**
   *
   */
  function getTextInput() {
    var html = '<input type="text" name="{{self.schema.id}}" class="form-control" ';
    if (self.schema.model) {
      // Field is an object, so we should bind the model property specified in
      // the schema
      html += 'ng-model="model[self.schema.model]" ';
    } else {
      // Field is a simple string, so we should just bind the model directly
      html += 'ng-model="model" '
    }

    html += self.schema.minLength ? 'ng-minlength="{{self.schema.minLength}}" ': '';
    html += self.schema.maxLength ? 'ng-maxlength="{{self.schema.maxLength}}" ': '';
    html += self.editable ? '': 'ng-readonly="true" '
    return html + (self.schema.required ? 'required' : '') + ' />'
  }

  /**
   *
   */
  function getSelectInput() {
    self.options = [];
    var html = '<select ng-model="model" name="{{self.schema.id}}" class="form-control" ';

    if( typeof self.schema.options === 'string' ) {
      // Options given as a URL
      html += 'ng-options="option for option in ctrl.options track by option" ';

      // TODO refactor this into a service
      $http.get(self.schema.options, {cache: true}).then(function(response) {
        if (!response.data.hasOwnProperty('_embedded')) return [];

        response.data._embedded[self.schema.returnPropertyName].map(function(item) {
          self.options.push(item[self.schema.model]);
        });
      });
    }

    else {
      // Options given as inline array
      html += 'ng-options="option for option in self.schema.options track by option" ';
    }

    html += self.editable ? '': 'ng-disabled="true" '
    return html += (self.schema.required ? 'required' : '') + ' />'
  }

  /**
   *
   */
  function getTypeaheadInput() {
    var template = '\
      <div class="form-group has-feedback"> \
        <input type="text" class="form-control" name="{{self.schema.id}}" ng-model="model" \
               placeholder="' + self.schema.placeholder + '"  \
               typeahead="item as item.' + self.schema.model + ' for item in ctrl.autocomplete(ctrl.schema, $viewValue)" \
               typeahead-editable="false" \
               typeahead-loading="loading" \
               typeahead-template-url="item-template-' + self.schema.id + '.html" \
               typeahead-min-length="{{self.schema.minLength}}" \
               ' + (self.schema.minLength ? 'ng-minlength="{{self.schema.minLength}}" ': '') +' \
               ' + (self.schema.maxLength ? 'ng-maxlength="{{self.schema.maxLength}}" ': '') +' \
               ' + (self.editable ? '': 'ng-readonly="true" ') + ' \
               ' + (self.schema.required ? 'required' : '') + '> \
      </div>'; // <i ng-show="loading" class="fa fa-fw fa-spin fa-refresh form-control-feedback"></i> \

    var itemTemplate = '\
      <script type="text/ng-template" id="item-template-' + self.schema.id + '.html"> \
        <a><span bind-html-unsafe="match.label | typeaheadHighlight:query"></span>';

    if (self.schema.template) {
      itemTemplate += self.schema.template;
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