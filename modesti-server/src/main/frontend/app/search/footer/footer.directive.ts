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

  public constructor(private $compile: ICompileService, private $http: IHttpService, private $ocLazyLoad: any, 
	private schemaService: SchemaService, private tableService: TableService) { }

  public static factory(): IDirectiveFactory {
    const directive: IDirectiveFactory = ($compile: ICompileService, $http: IHttpService, $ocLazyLoad: any, schemaService: SchemaService, tableService : TableService, defaultSearchControls: DefaultSearchControls) =>
      new SearchFooterDirective($compile, $http, $ocLazyLoad, schemaService, tableService);
    directive.$inject = ['$compile', '$http', '$ocLazyLoad', 'SchemaService', 'TableService'];
    return directive;
  }

  public link: Function = (scope, element) => {

    this.schemaService.getSchemas().then((schemas: Schema[]) => { 
      if (schemas.length == 0) {
        return;
      }

      let loadedSchemas : string[] = [];
      schemas.forEach((schema: Schema) => {
        let schemaId = schema.id;
        loadedSchemas.push(schemaId);

        this.$http.get('/api/plugins/' + schemaId + '/search-assets').then((response: any) => {
          let assets: string[] = response.data;
          if (assets.length == 0) {
            this.loadDefaultSearchButtons(schemaId, element, scope);
          } else {
            this.loadPluginSearchButtons(schemaId, assets, element, scope);
          }

          if (schemaId == schemas[schemas.length-1].id) {
            // When the search buttons for all the plugins are loaded, activates the first plugin (alphabetically sorted)
            loadedSchemas.sort();
            scope.$ctrl.activeSchemaId = loadedSchemas[0];
          }
        });
      });
    });
  }


  public loadDefaultSearchButtons(schemaId: string, element: any, scope: any) : void {
    let template: string = 
      '<div class="search-controls container-fluid" table="$ctrl.table" ng-show="$ctrl.isVisible(\'' + schemaId + '\')"><search-controls></search-controls></div>';
    element.append(this.$compile(template)(scope));
  }


  public loadPluginSearchButtons(schemaId: string, assets : string[], element: any, scope: any) : void {
    let directiveName : string;
    assets.forEach((asset: string) => {
      if (asset.endsWith("-search-controls.js")) {
        directiveName = asset.split('/')[asset.split('/').length - 1];
        directiveName = directiveName.substring(0, directiveName.length - 3);
      }
    });

    this.$ocLazyLoad.load(assets, {serie: true}).then(() => {
      let controlsName = directiveName;
      let template: string = '<div ' + controlsName + ' table="$ctrl.table" ng-show="$ctrl.isVisible(\'' + schemaId + '\')"></div>';
      element.append(this.$compile(template)(scope));
    });
  }
}

class SearchFooterController {
  public static $inject: string[] = ['$rootScope', 'TableService'];
  
  public activeSchemaId : string = "";
  public schema: Schema;
  public schemas: Schema[];
  public page: any = {number: 0, size: 100};
 
  public constructor(private $rootScope: IRootScope, private tableService: TableService) {
    $rootScope.$on('modesti:searchDomainChanged', (event, data) => {
      this.activeSchemaId = data;
    });
  }

  public isVisible(schemaId: string) : boolean {
    return this.activeSchemaId !== undefined && this.activeSchemaId == schemaId;
  }

  public updatePoints(message: string=''): void {
    this.tableService.updatePoints(message);
  }

  public deletePoints(): void {
    this.tableService.deletePoints();
  }

  public getTable(): Table {
    return this.tableService.table;
  }
}


