'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:RequestCorrectionControlsController
 * @description # RequestCorrectionControlsController Controller of the modesti
 */
angular.module('modesti').controller('RequestCorrectionControlsController', RequestCorrectionControlsController);

function RequestCorrectionControlsController($scope, $controller, Restangular) {
  // Most of the functionality is shared with the request creation controls, so we just extend here
  angular.extend(this, $controller('RequestCreationControlsController', {$scope: $scope}));
  
  var self = this;
  
  // Override the validate() function
  self.validate = function () {
    // Get the task associated with the request
    var taskLink = self.parent.request._links.task.href;
    
    Restangular.one(taskLink).get().then(function(task) {
      console.log('got task ' + task.id);
    }, 
    
    function(error) {
      console.log('error getting task: ' + error);
    });
  }
  
}