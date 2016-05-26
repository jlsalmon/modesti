'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:NewRequestController
 * @description # NewRequestController
 */
angular.module('modesti').controller('NewRequestController', NewRequestController);

function NewRequestController($state, schemas, RequestService, SchemaService, AuthService) {
  var self = this;

  self.schemas = schemas;
  self.domainSpecificFields = [];
  self.submitting = undefined;

  self.onDomainChanged = onDomainChanged;
  self.queryFieldValues = queryFieldValues;
  self.submit = submit;

  self.request = {
    type : 'CREATE',
    description : '',
    creator : AuthService.getCurrentUser().username
  };

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
        if (error.data && error.data.message) {
          self.error = error.data.message;
        } else {
          self.error = error.statusText;
        }
      });
    }
  }
}
