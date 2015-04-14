'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:NewRequestController
 * @description # NewRequestController Controller of the modesti
 */
angular.module('modesti').controller('NewRequestController', NewRequestController);
  
function NewRequestController($http, $location, $filter, RequestService) {
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
    return $http.get('data/systems.json', {
      params : {}
    }).then(function(response) {
      return $filter('filter')(response.data, value);
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
};