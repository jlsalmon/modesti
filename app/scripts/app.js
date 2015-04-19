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
    'ngTable',
    'restangular'
  ]);


app.config(function($routeProvider, $locationProvider) {
  // Configure routes
  $routeProvider
  .when('/new', {
    templateUrl: 'views/new-request.html',
    controller: 'NewRequestController as ctrl'
  })
  .when('/requests', {
    templateUrl: 'views/user-requests.html',
    controller: 'UserRequestsController as ctrl'
  })
  .when('/requests/:id', {
    templateUrl: 'views/request.html',
    controller: 'RequestController as ctrl',
    resolve: {
      
      // TODO refactor this out
      request: function getRequest($route, RequestService) {
        var id = $route.current.params.id;
        return RequestService.getRequest(id);
      },
      
      schema:  function getSchema($route, $q, $http) {
        console.log('fetching schema');
        var q = $q.defer();
        var id = $route.current.params.id;

        // TODO refactor this into a service
        $http.get('http://localhost:8080/requests/' + id + '/schema').then(function(response) {
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
  .when('/about', {
    templateUrl: 'views/about.html',
    controller: 'AboutCtrl'
  })
  .when('/search/:q', {
    templateUrl: 'views/search.html',
    controller: 'SearchController as ctrl'
  })
  .when('/404', {
    templateUrl: 'views/errors/404.html',
    //controller: 'PageNotFoundController'
  })
  .otherwise({
    redirectTo: '/'
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