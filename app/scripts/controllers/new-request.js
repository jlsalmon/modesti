'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:NewRequestController
 * @description # NewRequestController Controller of the modesti
 */
angular.module('modesti').controller('NewRequestController', NewRequestController);
  
function NewRequestController($http, $location, $filter, RequestService, Restangular) {
  var self = this;
  
  self.getSystems = getSystems;
  self.submit = submit;
  
  self.request = {
    type : 'create',
    description : ''
  };
  
  /**
   * 
   */
  function getSystems(value) {
    return $http.get('http://localhost:8080/subsystems/search/findByName', {
      params : {
        name : value
      }
    }).then(function(response) {
      return response.data._embedded.subsystems.map(function(item) {
        return item.name;
      });
    });
  }

  /**
   * 
   */
  function submit(form) {
    console.log(form);
    if (form.$invalid) {
      console.log('form invalid');
      return;
    }

    else {
      console.log('form valid');

      // Post form to server to create new request.
      RequestService.createRequest(self.request).then(function(location) {
        // Strip request ID from location.
        var id = location.substring(location.lastIndexOf('/') + 1);
        // Redirect to point entry page.
        $location.path("/requests/" + id);
      },

      function(error) {
        // what do do here?
      });
    }
  };
  
  
  // TODO: move everything below here to another page ------------------------------------------------------------------------
  
  self.schema = {};
  self.schemaString = '';
  self.saveSchema = saveSchema;
  
  var id = 'tim';
  
  Restangular.one('schemas', id).get().then(function(schema) {
    console.log('fetched schema');
    self.schema = schema.data;
    self.schemaString = JSON.stringify(schema.data, null, "    ");
  },

  function(error) {
    console.log('error fetching schema');
  });
  

  function saveSchema() {
    self.schema.categories = JSON.parse(self.schemaString).categories;
    
    self.schema.save().then(function(response) {
      console.log('saved schema');
      self.categories = response.data;
    }, function(error) {
      console.log('error saving schema: ' + error.data.message);
    });
  };
  
  
};