'use strict';

/**
 * @ngdoc function
 * @name modesti.config:configureRoutes
 *
 * @description Specifies the navigation flow between views.
 */
angular.module('modesti').config(configureRoutes);

function configureRoutes($stateProvider, $urlRouterProvider) {
  $urlRouterProvider.otherwise('/');

  $stateProvider.state('home', {
    url: '/',
    templateUrl: 'components/home/home.html',
    controller: 'HomeController as ctrl',
    resolve: {
      $title: function ($translate) {
        return 'HOME';
      }
    }

  }).state('requests', {
    url: '/requests',
    templateUrl: 'components/request/requests.html',
    controller: 'RequestsController as ctrl',
    resolve: {
      $title: function ($translate) {
        return 'REQUESTS';
      }
    }

  }).state('new', {
    url: '/requests/new',
    templateUrl: 'components/request/request.new.html',
    controller: 'NewRequestController as ctrl',
    resolve: {
      $title: function ($translate) {
        return $translate('NEW_REQUEST');
      }
    }

  }).state('upload', {
    url: '/requests/upload',
    templateUrl: 'components/request/request.upload.html',
    controller: 'UploadController as ctrl',
    resolve: {
      $title: function ($translate) {
        return $translate('UPLOAD_REQUEST');
      }
    }

  }).state('request', {
    url: '/requests/:id',
    templateUrl: 'components/request/request.html',
    controller: 'RequestController as ctrl',
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
      },

      $title: function (request, $translate) {
        return $translate('REQUEST', { id: request.id });
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
      },

      $title: function (request, $translate) {
        return $translate('REQUEST', { id: request.id });
      }

    }
  }).state('points', {
    url: '/points',
    templateUrl: 'components/point/points.html',
    controller: 'PointsController as ctrl',
    resolve: {

      schemas: function getSchemas(SchemaService) {
        return SchemaService.getSchemas();
      },

      $title: function ($stateParams, $translate) {
        return "Points";
      }
    }

  }).state('search', {
    url: '/search/:q',
    templateUrl: 'components/search/search.html',
    controller: 'SearchController as ctrl',
    resolve: {
      $title: function ($stateParams, $translate) {
        return $translate('SEARCHING_FOR', { q: $stateParams.q });
      }
    }

  }).state('users', {
    url: '/users/:id',
    templateUrl: 'components/users/user.html',
    controller: 'UserController as ctrl',
    resolve: {
      user: function ($stateParams, AuthService) {
        return AuthService.getUser($stateParams.id);
      },
      $title: function ($stateParams, $translate) {
        return $translate('USER', { id: $stateParams.id });
      }
    }

  }).state('about', {
    url: '/about',
    templateUrl: 'components/about/about.html',
    controller: 'AboutController as ctrl',
    resolve: {
      $title: function () { return 'ABOUT'; }
    }

  }).state('error', {
    url: '/error',
    templateUrl: 'components/error/error.html',
    controller: 'ErrorController as ctrl',
    resolve: {
      $title: function () { return 'ERROR'; }
    }

  }).state('404', {
    url: '/404',
    templateUrl: 'components/error/404.html',
    resolve: {
      $title: function () { return 'PAGE_NOT_FOUND'; }
    }
  });
}
