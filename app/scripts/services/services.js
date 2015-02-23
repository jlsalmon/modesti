'use strict';

/**
 * @ngdoc service
 * @name yoTestApp.myService
 * @description
 * # myService
 * Service in the yoTestApp.
 */
var myServices = angular.module('myServices', ['ngResource']);

myServices.factory('Greeting', ['$resource',
  function($resource){
    return $resource('http://localhost:8080/greeting', {}, {
      query: {method:'GET', params:{}}
    });
  }]);