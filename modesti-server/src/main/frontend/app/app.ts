import 'angular';
import 'angular-cookies';
import 'angular-animate';
import 'angular-sanitize';
import 'angular-ui-bootstrap';
import 'angular-ui-router';
import 'angular-file-upload';
import 'angular-http-auth';
import 'angular-spinner';
import 'angular-filter';
import 'angular-xeditable';
import 'angular-bind-html-compile';
import 'jquery';
import 'spin.js';
import 'bootstrap-sass';
import 'ui-select';
import 'ngstorage';
import 'restangular';
import 'oclazyload';
import 'angular-cache';
import 'ng-middle-click';
// TODO: import these properly, remove <script> tags from index.html
// import 'numbro';
// import 'moment';
// import 'pikaday';
// import 'zeroclipboard';
// import 'handsontable';
// import 'ngHandsontable/dist/ngHandsontable';
// import 'handsontable-select2-editor';

import {MainComponent} from './main.component.ts';
import {HomeComponent} from './home/home.component.ts';
import {LogoComponent} from './home/logo.component.ts';
import {RequestListComponent} from './request/request-list.component.ts';
import {NewRequestComponent} from './request/new-request.component.ts';
import {UploadRequestComponent} from './request/upload-request.component.ts';
import {RequestComponent} from './request/request.component.ts';
import {RequestHeaderComponent} from './request/header/header.component.ts';
import {RequestToolbarComponent} from './request/toolbar/toolbar.component.ts';
import {RequestTableComponent} from './request/table/request-table.component.ts';
import {SearchControlsComponent} from './search/footer/search.controls.component.ts';
import {RequestFooterDirective} from './request/footer/footer.directive.ts';
import {ColumnSelectorComponent} from './table/column-selector/column-selector.component.ts';
import {FilterBuilderComponent} from './table/filter-builder/filter-builder.component';
import {CloneRequestModalController} from './request/clone/clone-request.modal.ts';
import {AssignRequestModalController} from './request/assign/assign-request.modal.ts';
import {AssignCreatorModalController} from './request/creator/assign-creator.modal.ts';
import {DeleteRequestModalController} from './request/delete/delete-request.modal.ts';
import {ExportRequestModalController} from './export/export-request.modal.ts';
import {RequestHistoryModalController} from './request/history/request-history.modal.ts';
import {RequestCommentsModalController} from './request/comments/request-comments.modal.ts';
import {ShowIfDirective} from './request/directives/show-if.directive.ts';
import {EnableIfDirective} from './request/directives/enable-if.directive.ts';
import {UserComponent} from './user/user.component.ts';
import {SearchComponent} from './search/search.component.ts';
import {SearchFooterDirective} from './search/footer/footer.directive.ts';
import {AuthService} from './auth/auth.service.ts';
import {ExportService} from './export/export.service.ts';
import {RequestService} from './request/request.service.ts';
import {SchemaService} from './schema/schema.service.ts';
import {TaskService} from './task/task.service.ts';
import {HistoryService} from './request/history/history.service.ts';
import {AlertService} from './alert/alert.service.ts';
import {SearchService} from './search/search.service.ts';
import {TableService} from './search/table.service';
import {ValidationService} from './request/validation/validation.service.ts';
import {CacheService} from './cache/cache.service.ts';
import {ServerInfoService} from './server/server.info.ts';
import {SelectedPointsService} from './search/selected-points.service.ts';
import {LoginModalController} from './auth/login.modal.ts';
import {UpdatePointsModalController} from './search/update/update-points.modal.ts';
import {DeletePointsModalController} from './search/delete/delete-points.modal.ts';
import {RestangularConfig} from './config/restangular.config.ts';
import {RouterConfig} from './config/router.config.ts';
import {HttpConfig} from './config/http.config.ts';
import {ConfirmationModalController} from './util/confirmation.modal.controller.ts';


let app: any = angular.module('modesti', [
  'ng',
  'ngCookies',
  'ngAnimate',
  'ngSanitize',
  'ngStorage',
  'angular-bind-html-compile',
  'ui.bootstrap',
  'ui.router',
  'ui.select',
  'restangular',
  'angularFileUpload',
  'http-auth-interceptor',
  'angularSpinner',
  'angular.filter',
  'oc.lazyLoad',
  'xeditable',
  'agGrid',
  'angular-cache',
  'ng-middle-click'
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
app.component('searchControls', new SearchControlsComponent());
app.directive('requestFooter', RequestFooterDirective.factory());
app.directive('searchFooter', SearchFooterDirective.factory());
app.component('columnSelector', new ColumnSelectorComponent());
app.component('filterBuilder', new FilterBuilderComponent());
app.directive('showIf', ShowIfDirective.factory());
app.directive('enableIf', EnableIfDirective.factory());
app.component('user', new UserComponent());
app.component('search', new SearchComponent());
app.component('logo', new LogoComponent());

app.service('AuthService', AuthService);
app.service('ExportService', ExportService);
app.service('RequestService', RequestService);
app.service('SchemaService', SchemaService);
app.service('TaskService', TaskService);
app.service('HistoryService', HistoryService);
app.service('AlertService', AlertService);
app.service('SearchService', SearchService);
app.service('TableService', TableService);
app.service('ValidationService', ValidationService);
app.service('CacheService', CacheService);
app.service('ServerInfoService', ServerInfoService);
app.service('SelectedPointsService', SelectedPointsService);

app.controller('LoginModalController', LoginModalController);
app.controller('CloneRequestModalController', CloneRequestModalController);
app.controller('AssignRequestModalController', AssignRequestModalController);
app.controller('AssignCreatorModalController', AssignCreatorModalController);
app.controller('DeleteRequestModalController', DeleteRequestModalController);
app.controller('ExportRequestModalController', ExportRequestModalController);
app.controller('RequestHistoryModalController', RequestHistoryModalController);
app.controller('RequestCommentsModalController', RequestCommentsModalController);
app.controller('UpdatePointsModalController', UpdatePointsModalController);
app.controller('DeletePointsModalController', DeletePointsModalController);
app.controller('ConfirmationModalController', ConfirmationModalController);


app.config(['RestangularProvider', (restangularProvider: any) => RestangularConfig.configure(restangularProvider)]);
app.config(['$httpProvider', ($httpProvider: any) => HttpConfig.configure($httpProvider)]);
app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider',
  ($stateProvider: any, $urlRouterProvider: any, $locationProvider: any) => {
  RouterConfig.configure($stateProvider, $urlRouterProvider, $locationProvider);
}]);

// Disable animation for fa-spin
app.config(['$animateProvider', ($animateProvider: any) => {
  $animateProvider.classNameFilter(/^((?!(fa-spin)).)*$/);
}]);

// Enable dynamic page titles
app.run(['$rootScope', '$timeout', '$transitions', '$interpolate',
  ($rootScope: any, $timeout: any, $transitions: any, $interpolate: any) => {

  $transitions.onSuccess({to: '*'}, ($state: any) => {
    let title: string = getTitleValue($state.router.globals);
    $timeout(() => $rootScope.$title = title);
  });

  function getTitleValue(globals: any): string {
    if (globals.$current.title) {
      return $interpolate(globals.$current.title)(globals.params);
    }

    // TODO: allow "resolving" a title (or use angular-ui-router-title when
    // TODO: it supports ui-router 1.0)
  }
}]);

angular.bootstrap(document, ['modesti'], {
  strictDi: true
});

