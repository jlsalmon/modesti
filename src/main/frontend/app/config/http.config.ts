export class HttpConfig {

  public static configure($httpProvider:any) {

    // Needed so that Spring Security sends us back a WWW-Authenticate header,
    // which will prevent the browser from showing a basic auth popup
    $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

    // Needed to make sure that the JSESSIONCOOKIE is sent with every request
    $httpProvider.defaults.withCredentials = true;

    $httpProvider.interceptors.push(['$q', '$injector', ($q, $injector) => {
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
          if (response && (response.status === 0 || response.status === -1)) {
            // Backend not connected
            console.log('error: backend not connected');
            $injector.get('$state').transitionTo('error', {}, {location: false});
          }
          if (response && response.status === 404) {
            console.log('error: page not found');
            $injector.get('$state').transitionTo('404', {}, {location: false});
          }
          if (response && response.status >= 500) {
            console.log('error: ' + response.statusText);
          }
          return $q.reject(response);
        }
      };
    }]);
  }
}
