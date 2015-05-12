'use strict';

/**
 * @ngdoc function
 * @name modesti.config:configureRoutes
 *
 * @description Specifies the navigation flow between views.
 */
angular.module('modesti').config(configureRoutes);

function configureRoutes($stateProvider, $urlRouterProvider) {
  $urlRouterProvider.otherwise('/requests');

  $stateProvider.state('requests', {
    url : '/requests',
    templateUrl : 'components/request/requests.html',
    controller : 'UserRequestsController as ctrl'

  }).state('requests/new', {
    url : '/requests/new',
    templateUrl : 'components/request/new-request.html',
    controller : 'NewRequestController as ctrl'

  }).state('requests/upload', {
    url : '/requests/upload',
    templateUrl : 'components/request/upload/upload.html',
    controller : 'UploadController as ctrl'

  }).state('request', {
    url : '/requests/:id',
    templateUrl : 'components/request/request.html',
    controller : 'RequestController as ctrl',
    resolve : {

      // TODO refactor this out
      request : function getRequest($stateParams, RequestService) {
        var id = $stateParams.id;
        return RequestService.getRequest(id);
      },

      schema : function getSchema($q, $http, request) {
        console.log('fetching schema');
        var q = $q.defer();

        // TODO refactor this into a service
        $http.get(request._links.schema.href).then(function(response) {
          console.log('fetched schema: ' + response.data.name);
          q.resolve(response.data);
        },

        function(error) {
          console.log('error fetching schema: ' + error);
          q.reject();
        });

        return q.promise;
      },
      
      task : function getTask($q, $http, request) {
        console.log('fetching task');
        var q = $q.defer();
        
        // TODO refactor this into a service
        $http.get(request._links.task.href).then(function(response) {
          console.log('fetched task: ' + response.data.name);
          q.resolve(response.data);
        },

        function(error) {
          console.log('error fetching task: ' + error);
          q.reject();
        });

        return q.promise;
      }
    }

  }).state('tasks', {
    url : '/tasks',
    templateUrl : 'components/tasks/tasks.html',
    controller : 'TasksController as ctrl'

  }).state('task', {
    url : '/tasks/:id',
    templateUrl : 'components/tasks/task.html',
    controller : 'TaskController as ctrl',
    resolve : {

      // TODO refactor this out
      task : function getTask($stateParams, TaskService) {
        var id = $stateParams.id;
        return TaskService.getTask(id);
      },
    }

  }).state('about', {
    url : '/about',
    templateUrl : 'components/about/about.html',
    controller : 'AboutController as ctrl'

  }).state('search', {
    url : '/search/:q',
    templateUrl : 'components/search/search.html',
    controller : 'SearchController as ctrl'

  }).state('404', {
    url : '/404',
    templateUrl : 'components/errors/404.html',
  });
}