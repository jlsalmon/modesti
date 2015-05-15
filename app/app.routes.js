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

      children : function getChildren($q, request, RequestService) {
        var childRequestIds = request.childRequestIds;
        var promises = [];

        angular.forEach(childRequestIds, function(childRequestId) {
          promises.push(RequestService.getRequest(childRequestId));
        });

        return $q.all(promises);
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

      tasks : function getTasks($q, $http, request) {
        console.log('fetching tasks');
        var q = $q.defer();
        var promises = [];

        // TODO refactor this into a service
        angular.forEach(request._links.tasks, function(link) {
          var href = link.href ? link.href : link;
          var promise = $http.get(href);
          promises.push(promise);
        });

        $q.all(promises).then(function(responses) {
          console.log('fetched ' + responses.length + ' task(s)');
          var tasks = {};

          angular.forEach(responses, function(response) {
            tasks[response.data.name] = response.data;
          });

          q.resolve(tasks);
        },

        function(error) {
          console.log('error fetching tasks: ' + error);
          q.reject(error);
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
      }
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