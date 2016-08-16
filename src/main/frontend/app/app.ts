import 'angular';
import 'angular-cookies';
import 'angular-animate';
import 'angular-sanitize';
import 'angular-ui-bootstrap';
import 'angular-ui-router';
import 'angular-ui-router-title';
import 'angular-file-upload';
import 'angular-http-auth';
import 'angular-spinner';
import 'angular-filter';
import 'spin.js';
//import 'angular-bootstrap-select';
import 'bootstrap-sass';
import 'ui-select';
import 'select2';
import 'ngstorage';
import 'restangular';
import 'oclazyload';

// TODO: import these properly, remove <script> tags from index.html
//import 'numbro';
//import 'moment';
//import 'pikaday';
//import 'zeroclipboard';
//import 'handsontable';
//declare var Handsontable: any;
import 'ngHandsontable/dist/ngHandsontable';
import 'handsontable-select2-editor';

import {MainComponent} from './main.component.ts';
import {HomeComponent} from './home/home.component.ts';
import {RequestListComponent} from './request/request-list.component.ts';
import {NewRequestComponent} from './request/new-request.component.ts';
import {UploadRequestComponent} from './request/upload-request.component.ts';
import {RequestComponent} from './request/request.component.ts';
import {RequestHeaderComponent} from './request/header/header.component.ts';
import {RequestToolbarComponent} from './request/toolbar/toolbar.component.ts';
import {RequestTableComponent} from './request/table/table.component.ts';
import {RequestFooterDirective} from './request/footer/footer.directive.ts';
import {CloneRequestModalController} from './request/clone/clone-request.modal.ts';
import {AssignRequestModalController} from './request/assign/assign-request.modal.ts';
import {DeleteRequestModalController} from './request/delete/delete-request.modal.ts';
import {RequestHistoryModalController} from './request/history/request-history.modal.ts';
import {RequestCommentsModalController} from './request/comments/request-comments.modal.ts';
import {ShowIfDirective} from './request/directives/show-if.directive.ts';
import {EnableIfDirective} from './request/directives/enable-if.directive.ts';
import {UserComponent} from './user/user.component.ts';
import {SearchComponent} from './search/search.component.ts';
import {AuthService} from './auth/auth.service.ts';
import {RequestService} from './request/request.service.ts';
import {SchemaService} from './schema/schema.service.ts';
import {TaskService} from './task/task.service.ts';
import {HistoryService} from './request/history/history.service.ts';
import {AlertService} from './alert/alert.service.ts';
import {SearchService} from './search/search.service.ts';
import {TableService} from './request/table/table.service.ts';
import {ValidationService} from './request/validation/validation.service.ts';
import {Utils} from './utils/utils.ts';
import {LoginModalController} from './auth/login.modal.ts';
import {UpdatePointsModalController} from './search/update/update-points.modal.ts';
import {RestangularConfig} from './config/restangular.config.ts';
import {RouterConfig} from './config/router.config.ts';
import {HttpConfig} from './config/http.config.ts';

var app = angular.module('modesti', [
  'ng',
  'ngCookies',
  'ngAnimate',
  'ngSanitize',
  'ngStorage',
  'ngHandsontable',
  'ui.bootstrap',
  'ui.router',
  'ui.router.title',
  'ui.select',
  'restangular',
  'angularFileUpload',
  'http-auth-interceptor',
  'angularSpinner',
  'angular.filter',
  //'angular-bootstrap-select',
  'oc.lazyLoad'
]);

// TODO: split this up into modules
app.component('main', new MainComponent());
app.component('home', new HomeComponent());
app.component('requestList', new RequestListComponent());
app.component('newRequest', new NewRequestComponent());
app.component('uploadRequest', new UploadRequestComponent());
app.component('request', new RequestComponent());
app.component('requestHeader', new RequestHeaderComponent());
app.component('requestToolbar', new RequestToolbarComponent());
app.component('requestTable', new RequestTableComponent());
app.directive('requestFooter', RequestFooterDirective.factory());
app.directive('showIf', ShowIfDirective.factory());
app.directive('enableIf', EnableIfDirective.factory());
app.component('user', new UserComponent());
app.component('search', new SearchComponent());

app.service('AuthService', AuthService);
app.service('RequestService', RequestService);
app.service('SchemaService', SchemaService);
app.service('TaskService', TaskService);
app.service('HistoryService', HistoryService);
app.service('AlertService', AlertService);
app.service('SearchService', SearchService);
app.service('TableService', TableService);
app.service('ValidationService', ValidationService);
app.service('Utils', Utils);

app.controller('LoginModalController', LoginModalController);
app.controller('CloneRequestModalController', CloneRequestModalController);
app.controller('AssignRequestModalController', AssignRequestModalController);
app.controller('DeleteRequestModalController', DeleteRequestModalController);
app.controller('RequestHistoryModalController', RequestHistoryModalController);
app.controller('RequestCommentsModalController', RequestCommentsModalController);
app.controller('UpdatePointsModalController', UpdatePointsModalController);


app.config(['RestangularProvider', (RestangularProvider:any) => RestangularConfig.configure(RestangularProvider)]);
app.config(['$httpProvider', ($httpProvider:any) => HttpConfig.configure($httpProvider)]);
app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider', ($stateProvider: any, $urlRouterProvider: any, $locationProvider: any) => {
  RouterConfig.configure($stateProvider, $urlRouterProvider, $locationProvider);
}]);
app.config(['$animateProvider', ($animateProvider: any) => {
  // Disable animation for fa-spin
  $animateProvider.classNameFilter(/^((?!(fa-spin)).)*$/);
}]);

angular.bootstrap(document, ['modesti'], {
  strictDi: true
});