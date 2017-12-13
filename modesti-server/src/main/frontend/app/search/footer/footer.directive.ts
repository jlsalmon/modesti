import {Schema} from '../../schema/schema';
import {SchemaService} from '../../schema/schema.service';
import {RequestService} from '../../request/request.service';
import {AlertService} from '../../alert/alert.service';
import {SearchService} from '../search.service';
import {TableService} from '../table.service';
import {Table} from '../../table/table';
import {IPromise, IDeferred, IQService, IDirective, IDirectiveFactory, IHttpService, ICompileService, IScope, IRootScopeService} from 'angular';


export class SearchFooterDirective implements IDirective {
  public controller: Function = SearchFooterController;
  public controllerAs: string = '$ctrl';
  public scope: any = { };


  public bindToController: any = {
    schema: '=',
    table: '='
  }

  public constructor(private $compile: ICompileService, private $http: IHttpService, private $ocLazyLoad: any, private schemaService: SchemaService, private tableService: TableService) { }

  public static factory(): IDirectiveFactory {
    const directive: IDirectiveFactory = ($compile: ICompileService, $http: IHttpService, $ocLazyLoad: any, schemaService: SchemaService, tableService : TableService) =>
      new SearchFooterDirective($compile, $http, $ocLazyLoad, schemaService, tableService);
    directive.$inject = ['$compile', '$http', '$ocLazyLoad', 'SchemaService', 'TableService'];
    return directive;
  }

  public link: Function = (scope, element) => {

    this.schemaService.getSchemas().then((schemas: Schema[]) => { 
      if (schemas.length > 0) {
        scope.$ctrl.activeSchemaId = schemas[0].id;
      }

      schemas.forEach((schema: Schema) => {
        let schemaId = schema.id;

        this.$http.get('/api/plugins/' + schemaId + '/search-assets').then((response: any) => {
          let assets: string[] = response.data;
          console.log(assets);

          let directiveName : string;
          assets.forEach((asset: string) => {
            if (asset.endsWith("-search-controls.js")) {
              directiveName = asset.split('/')[asset.split('/').length - 1];
              directiveName = directiveName.substring(0, directiveName.length - 3);
            }
          }

          this.$ocLazyLoad.load(assets, {serie: true}).then(() => {
            let controlsName = directiveName;
            let template: string = '<div ' + controlsName + ' table="$ctrl.table" ng-show="$ctrl.isVisible(\'' + schemaId + '\')"></div>';
            element.append(this.$compile(template)(scope));
          };

        });
      });
    });
  }
}


class SearchFooterController {
  public static $inject: string[] = ['$rootScope', '$uibModal', '$scope', '$state', '$q', 'SearchService', 'RequestService', 'AlertService', 'TableService'];
  
  public activeSchemaId : string = "";
  public schema: Schema;
  public schemas: Schema[];
  public page: any = {number: 0, size: 100};

  public constructor(private $rootScope: IRootScope, private $modal: any, private $scope: IScope, private $state: IStateService, private $q: IQService,
                     private searchService: SearchService, private requestService: RequestService, private alertService: AlertService, 
                     private tableService: TableService) {
    $rootScope.$on('modesti:searchDomainChanged', (event, data) => {
      this.activeSchemaId = data;
    });
  }

  public isVisible(schemaId: string) : boolean {
    console.log("Cheking if the schema is visible: " + schemaId);
    return this.activeSchemaId !== undefined && this.activeSchemaId == schemaId;
  }

  public parentTest(): void {
    console.log("This is a test from the parent controller");
  } 

  public updatePoints(): void {
    this.tableService.updatePoints();
  }

  public deletePoints(): void {
    this.$scope.$parent.$ctrl.deletePoints();
  }

  public getTable(): Table {
    return this.$scope.$parent.$ctrl.table;
  }
}


