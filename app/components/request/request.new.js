'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:NewRequestController
 * @description # NewRequestController
 */
angular.module('modesti').controller('NewRequestController', NewRequestController);

function NewRequestController($state, RequestService, SchemaService, AuthService) {
  var self = this;

  self.schemas = [];
  self.domainSpecificFields = [];
  self.submitting = undefined;

  self.getSchemas = getSchemas;
  self.onDomainChanged = onDomainChanged;
  self.queryFieldValues = queryFieldValues;
  self.submit = submit;

  self.request = {
    type : 'CREATE',
    description : '',
    creator : AuthService.getCurrentUser().username
  };

  getSchemas();

  /**
   *
   */
  function getSchemas() {
    SchemaService.getSchemas().then(function (schemas) {
      self.schemas = schemas;
    });
  }

  /**
   *
   */
  function onDomainChanged() {
    var domain = self.request.domain;

    self.schemas.forEach(function (schema) {
      if (schema.id === domain) {
        self.domainSpecificFields = schema.fields;
      }
    });
  }

  /**
   *
   * @param field
   * @param query
   * @returns {*}
   */
  function queryFieldValues(field, query) {
    return SchemaService.queryFieldValues(field, query).then(function (values) {
      self.fieldValues = values;
    });
  }

  /**
   *
   */
  function submit(form) {
    console.log(form);
    if (form.$invalid) {
      console.log('form invalid');
    }

    else {
      self.submitting = 'started';

      // Post form to server to create new request.
      RequestService.createRequest(self.request).then(function(location) {
        // Strip request ID from location.
        var id = location.substring(location.lastIndexOf('/') + 1);
        // Redirect to point entry page.
        $state.go('request', { id: id }).then(function () {
          self.submitting = 'success';
        });
      },

      function (error) {
        self.submitting = 'error';
        self.error = error.data.message;
      });
    }
  }
}
