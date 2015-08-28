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
    url: '/requests',
    templateUrl: 'components/request/requests.html',
    controller: 'RequestsController as ctrl',
    data: {
      pageTitle: 'REQUESTS'
    }

  }).state('new', {
    url: '/requests/new',
    templateUrl: 'components/request/request.new.html',
    controller: 'NewRequestController as ctrl',
    data: {
      pageTitle: 'NEW_REQUEST'
    }

  }).state('upload', {
    url: '/requests/upload',
    templateUrl: 'components/request/request.upload.html',
    controller: 'UploadController as ctrl',
    data: {
      pageTitle: 'UPLOAD_REQUEST'
    }

  }).state('request', {
    url: '/requests/:id',
    templateUrl: 'components/request/request.html',
    controller: 'RequestController as ctrl',
    data: {
      pageTitle: 'EDIT_REQUEST' // TODO localise this and add request id
    },
    resolve: {

      request: function getRequest($stateParams, RequestService) {
        return RequestService.getRequest($stateParams.id);
      },

      children: function getChildren(request, RequestService) {
        return RequestService.getChildRequests(request);
      },

      schema: function getSchema(request, SchemaService) {
        return SchemaService.getSchema(request);
      },

      tasks: function getTasks(request, TaskService) {
        return TaskService.getTasksForRequest(request);
      },

      signals: function getSignals(request, TaskService) {
        return TaskService.getSignalsForRequest(request);
      }

    }
  }).state('submitted', {
    url: '/requests/:id/submitted',
    templateUrl: 'components/request/request.submitted.html',
    controller: 'RequestSubmittedController as ctrl',
    params: { previousStatus: null },
    data: {
      pageTitle: 'REQUEST_SUBMITTED' // TODO localise this and add request id
    },
    resolve: {

      request: function getRequest($stateParams, RequestService) {
        return RequestService.getRequest($stateParams.id);
      }

    }
  }).state('search', {
    url: '/search/:q',
    templateUrl: 'components/search/search.html',
    controller: 'SearchController as ctrl',
    data: {
      pageTitle: 'SEARCH' // TODO localise this and add search term
    }

  }).state('users', {
    url: '/users/:id',
    templateUrl: 'components/users/user.html',
    controller: 'UserController as ctrl',
    data: {
      pageTitle: 'USERS' // TODO localise this and add user id
    }

  }).state('404', {
    url: '/404',
    templateUrl: 'components/errors/404.html',
    data: {
      pageTitle: 'PAGE_NOT_FOUND' // TODO localise this
    }
  });
}