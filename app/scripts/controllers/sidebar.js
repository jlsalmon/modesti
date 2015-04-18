'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:SidebarController
 * @description # SidebarController Controller of the modesti
 */
angular.module('modesti').controller('SidebarController', SidebarController);
    
function SidebarController($location) {
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
