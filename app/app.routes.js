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

  }).state('new', {
    url : '/requests/new',
    templateUrl : 'components/request/request.new.html',
    controller : 'NewRequestController as ctrl'

  }).state('upload', {
    url : '/requests/upload',
    templateUrl : 'components/request/request.upload.html',
    controller : 'UploadController as ctrl'

  }).state('request', {
    url : '/requests/:id',
    templateUrl : 'components/request/request.html',
    controller : 'RequestController as ctrl',
    resolve : {

      request : function getRequest($stateParams, RequestService) {
        return RequestService.getRequest($stateParams.id);
      },

      children : function getChildren(request, RequestService) {
        return RequestService.getChildRequests(request);
      },

      schema : function getSchema(request, SchemaService) {
        return SchemaService.getSchema(request);
      },

      tasks : function getTasks(request, TaskService) {
        return TaskService.getTasksForRequest(request);
      }
    }

  }).state('request.errors', {
    url : '/requests/:id/errors',
    templateUrl : 'components/request/request.errors.html',
    controller : 'RequestErrorsController as ctrl'

  }).state('request.activity', {
    url : '/requests/:id/activity',
    templateUrl : 'components/request/request.activity.html',
    controller : 'RequestActivityController as ctrl'

  }).state('request.comments', {
    url : '/requests/:id/comments',
    templateUrl : 'components/request/request.comments.html',
    controller : 'RequestCommentsController as ctrl'

  }).state('search', {
    url : '/search/:q',
    templateUrl : 'components/search/search.html',
    controller : 'SearchController as ctrl'

  }).state('404', {
    url : '/404',
    templateUrl : 'components/errors/404.html'
  });
}