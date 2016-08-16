export class RouterConfig {

  public static configure($stateProvider:any, $urlRouterProvider:any, $locationProvider:any) {
    $locationProvider.html5Mode(true);

    $urlRouterProvider.otherwise('/');
    $stateProvider
        .state('home',          { url: '/',                component: 'home'})
        .state('requestList',   { url: '/requests',        component: 'requestList'})
        .state('newRequest',    { url: '/requests/new',    component: 'newRequest'})
        .state('uploadRequest', { url: '/requests/upload', component: 'uploadRequest' })
        .state('request',       { url: '/requests/:id',    component: 'request',
          resolve: {
            request: ['$stateParams', 'RequestService', ($stateParams:any, RequestService:any) => {
              return RequestService.getRequest($stateParams.id);
            }],
            children: ['request', 'RequestService', (request:any, RequestService:any) => {
              return RequestService.getChildRequests(request);
            }],
            schema: ['request', 'SchemaService', (request:any, SchemaService:any) => {
              return SchemaService.getSchema(request);
            }],
            tasks: ['request', 'TaskService', (request:any, TaskService:any) => {
              return TaskService.getTasksForRequest(request);
            }],
            signals: ['request', 'TaskService', (request:any, TaskService:any) => {
              return TaskService.getSignalsForRequest(request);
            }],
            history: ['request', 'RequestService', (request:any, RequestService:any) => {
              return RequestService.getRequestHistory(request.requestId);
            }]
          }})
        .state('search', { url: '/search',    component: 'search'})
        .state('user',   { url: '/users/:id', component: 'user'})
        .state('about',  { url: '/about',     component: 'about' })
        .state('error',  { url: '/error',     component: 'error' })
        .state('404',    { url: '/404',       templateUrl: '/error/404.html'   })

        // TODO: remove this state, an alert should be enough
        .state('submitted',   { url: '/requests/:id/submitted', component: 'request',
          params: { previousStatus: null },
          data: {
            pageTitle: 'Request submitted'
          },
          resolve: {
            request: ['$stateParams', 'RequestService', ($stateParams:any, RequestService:any) => {
              return RequestService.getRequest($stateParams.id);
            }],
          }})
  }
}
