'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:NewRequestController
 * @description # NewRequestController Controller of the modesti
 */
angular.module('modesti').controller('NewRequestController', NewRequestController);

function NewRequestController($http, $location, RequestService, SchemaService, AuthService) {
  var self = this;

  self.schemas = [];
  self.submitting = undefined;

  self.getSchemas = getSchemas;
  self.getSubsystems = getSubsystems;
  self.submit = submit;

  self.request = {
    type : 'CREATE',
    description : '',
    creator : AuthService.getCurrentUser()
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
  function getSubsystems(value) {
    return $http.get(BACKEND_BASE_URL + '/subsystems/search/find', {
      params : {
        query : value
      }
    }).then(function(response) {
      if (!response.data.hasOwnProperty('_embedded')) {
        return [];
      }

      return response.data._embedded.subsystems;
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
        $location.path("/requests/" + id);
        self.submitting = 'success';
      },

      function (error) {
        self.submitting = 'error';
      });
    }
  }
}