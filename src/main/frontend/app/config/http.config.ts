export class HttpConfig {

  public static configure($httpProvider: any): void {

    // Needed so that Spring Security does not send a WWW-Authenticate header,
    // which will prevent the browser from showing a basic auth popup
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

    // Needed to make sure that the JSESSIONCOOKIE is sent with every request
    $httpProvider.defaults.withCredentials = true;

    $httpProvider.interceptors.push(['$q', '$injector', ($q: any, $injector: any) => {
      return {
        request : (config: any) => {
          return config || $q.when(config);
        },
        requestError : (request: any) => {
          return $q.reject(request);
        },
        response : (response: any) => {
          return response || $q.when(response);
        },
        responseError : (response: any) => {
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
