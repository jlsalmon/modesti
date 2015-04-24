'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:LayoutController
 * @description # LayoutController Controller of the modesti
 */
angular.module('modesti').controller('LayoutController', LayoutController);
    
function LayoutController($location) {
  var self = this;
  
  self.isActivePage = isActivePage;
  self.search = search;
  
  /**
   * 
   */
  function isActivePage(page) {
    return $location.path().lastIndexOf(page, 0) === 0;
  };
  
  /**
   * 
   */
  function search(q) {
    $location.path('/search/' + q);
  }
};
