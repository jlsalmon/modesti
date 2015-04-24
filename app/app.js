'use strict';

/**
 * @ngdoc overview
 * @name modesti
 * @description
 * # modesti
 *
 * Main module of the application.
 */
var app = angular
  .module('modesti', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'ui.bootstrap',
    'ui.router',
    'ngTable',
    'restangular'
  ]);


app.config(function($stateProvider, $urlRouterProvider) {
  // Configure routes
  $stateProvider
  .state('requests', {
    url: '/requests',
    templateUrl: 'components/request/user-requests.html',
    controller: 'UserRequestsController as ctrl'
  })
  .state('requests/new', {
    url: '/requests/new',
    templateUrl: 'components/request/new-request.html',
    controller: 'NewRequestController as ctrl'
  })
  .state('request', {
    url: '/requests/:id',
    templateUrl: 'components/request/request.html',
    controller: 'RequestController as ctrl',
    resolve: {
      
      // TODO refactor this out
      request: function getRequest($stateParams, RequestService) {
        var id = $stateParams.id;
        return RequestService.getRequest(id);
      },
      
      schema:  function getSchema($q, $http, request) {
        console.log('fetching schema');
        var q = $q.defer();
        //var id = $stateParams.id;

        // TODO refactor this into a service
        $http.get(request._links.schema.href).then(function(response) {
          console.log('fetched schema: ' + response.data.name);
          q.resolve(response.data);
          //self.schema = response.data;
        },
        
        function(error) {
          console.log('error fetching schema: ' + error);
          q.reject();
        });
        
        return q.promise;
      }
    }
  })
  .state('about', {
    url: '/about',
    templateUrl: 'components/about/about.html',
    controller: 'AboutCtrl'
  })
  .state('search', {
    url: '/search/:q',
    templateUrl: 'components/search/search.html',
    controller: 'SearchController as ctrl'
  })
  .state('404', {
    url: '/404',
    templateUrl: 'components/errors/404.html',
    //controller: 'PageNotFoundController'
  });
});

app.config(function(RestangularProvider) {
  // Set the base URL
  RestangularProvider.setBaseUrl('http://localhost:8080/');
  
  // Enable access to the response headers
  RestangularProvider.setFullResponse(true);

  // Add a response interceptor
  RestangularProvider.addResponseInterceptor(function(data, operation, what, url, response, deferred) {
    
    var extractedData;

    if (operation === "getList") {
      if (data.hasOwnProperty('_embedded')) {
        extractedData = data._embedded.requests;
      } else {
        extractedData = [];
      }
    } else {
      extractedData = data;
    }
    return extractedData;
  });

  // Set the self link
  RestangularProvider.setRestangularFields({
    selfLink : "_links.self.href"
  });
});

app.factory('errorInterceptor', [ '$q', '$rootScope', function($q, $rootScope) {
  return {
    request : function(config) {
      return config || $q.when(config);
    },
    requestError : function(request) {
      return $q.reject(request);
    },
    response : function(response) {
      return response || $q.when(response);
    },
    responseError : function(response) {
      if (response && response.status === 0) {
        // Backend not connected
        console.log('error: backend not connected');
        $rootScope.$broadcast("server.error.connection", response.status);
      }
      if (response && response.status === 404) {
        console.log('error: page not found');
      }
      if (response && response.status >= 500) {
        console.log('error: ' + response.statusText);
      }
      return $q.reject(response);
    }
  };
} ]);

app.config([ '$httpProvider', function($httpProvider) {
  $httpProvider.interceptors.push('errorInterceptor');
} ]);