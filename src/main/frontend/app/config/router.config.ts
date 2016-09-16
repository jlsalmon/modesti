/**
 * Define the routes (pages) that make up the application.
 */
export class RouterConfig {

  public static configure($stateProvider: any, $urlRouterProvider: any, $locationProvider: any): void {
    $locationProvider.html5Mode(true);

    $urlRouterProvider.otherwise('/');
    $stateProvider
        .state('home',          { url: '/',                component: 'home',          title: 'Home' })
        .state('requestList',   { url: '/requests',        component: 'requestList',   title: 'Requests' })
        .state('newRequest',    { url: '/requests/new',    component: 'newRequest',    title: 'New Request' })
        .state('uploadRequest', { url: '/requests/upload', component: 'uploadRequest', title: 'Upload Request' })
        .state('request',       { url: '/requests/:id',    component: 'request',       title: 'Request #{{id}}',
          resolve: {
            request: ['$stateParams', 'RequestService', ($stateParams: any, requestService: any) => {
              return requestService.getRequest($stateParams.id);
            }],
            children: ['request', 'RequestService', (request: any, requestService: any) => {
              return requestService.getChildRequests(request);
            }],
            schema: ['request', 'SchemaService', (request: any, schemaService: any) => {
              return schemaService.getSchema(request);
            }],
            tasks: ['request', 'TaskService', (request: any, taskService: any) => {
              return taskService.getTasksForRequest(request);
            }],
            signals: ['request', 'TaskService', (request: any, taskService: any) => {
              return taskService.getSignalsForRequest(request);
            }],
            history: ['request', 'RequestService', (request: any, requestService: any) => {
              return requestService.getRequestHistory(request.requestId);
            }]
          }})
        .state('search', { url: '/search',    component: 'search',            title: 'Search',
          resolve: {
            schemas: ['SchemaService', (schemaService: any) => {
              return schemaService.getSchemas();
            }]
          }})
        .state('user',   { url: '/users/:id', component: 'user',              title: '{{id}}' })
        .state('about',  { url: '/about',     component: 'about',             title: 'About' })
        .state('error',  { url: '/error',     component: 'error',             title: 'Error' })
        .state('404',    { url: '/404',       templateUrl: '/error/404.html', title: 'Not Found' })

        // TODO: remove this state, an alert should be enough
        .state('submitted',   { url: '/requests/:id/submitted', component: 'request',
          params: { previousStatus: undefined },
          title: 'Request submitted',
          resolve: {
            request: ['$stateParams', 'RequestService', ($stateParams: any, requestService: any) => {
              return requestService.getRequest($stateParams.id);
            }]
          }});
  }
}
