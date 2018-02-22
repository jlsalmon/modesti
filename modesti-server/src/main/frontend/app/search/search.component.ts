import {SearchService} from './search.service';
import {SchemaService} from '../schema/schema.service';
import {RequestService} from '../request/request.service';
import {AlertService} from '../alert/alert.service';
import {Table} from '../table/table';
import {TableFactory} from '../table/table-factory';
import {Schema} from '../schema/schema';
import {Point} from '../request/point/point';
import {QueryParser} from './query-parser';
import {Filter} from '../table/filter';
import {IComponentOptions, IRootScopeService, IAngularEvent} from 'angular';
import {IStateService} from 'angular-ui-router';

import {TableService} from './table.service';

export class SearchComponent implements IComponentOptions {
  public templateUrl: string = '/search/search.component.html';
  public controller: Function = SearchController;
  public bindings: any = {
    schemas: '='
  };
}

export class SearchController {
  public static $inject: string[] = ['$rootScope', '$uibModal', '$state', 'SearchService',
                                     'SchemaService', 'RequestService', 'AlertService', 'TableService'];

  public schema: Schema;
  public schemas: Schema[];
  public table: Table;
  public filters: Map<string, Filter>;
  public query: string;
  public page: any = {number: 0, size: 100};
  public sort: string;
  public loading: string;
  public error: string;
  public submitting: string;

  constructor(private $rootScope: IRootScopeService, private $modal: any, private $state: IStateService,
              private searchService: SearchService, private schemaService: SchemaService,
              private requestService: RequestService, private alertService: AlertService,private tableService: TableService) {

    this.activateSchema(this.schemas[0]);

    let settings: any = {
      getRows: this.search
    };

    this.table = this.tableService.buildTable(this.schema, settings);

    $rootScope.$on('modesti:searchFiltersChanged', (event: IAngularEvent, filters: Map<string, Filter>) => {
      this.filters = filters;
      this.tableService.filters = this.filters;
      this.search();
    });
  }

  public activateSchema(schema: Schema): void {
    this.schema = schema;

    if (this.table) {
      this.resetFilters();
      this.table.schema = schema;
      this.table.refreshColumnDefs();
      this.table.refreshData();
    }

    this.$rootScope.$emit('modesti:searchDomainChanged', schema.id);
  }

  public resetFilters(): void {
    this.filters = new Map<string, Filter>();
    this.page = {number: 0, size: 100};
    this.table.gridOptions.api.setSortModel(null);
    this.sort = '';
  }

  public search = (params?: any): void => {
    this.loading = 'started';
    console.log('searching');

    let query: string = QueryParser.parse(this.filters);

    if (params) {
      this.page.number = params.startRow / 100;
      console.log('asking for ' + params.startRow + ' to ' + params.endRow + ' (page #' + this.page.number + ')');

      if (params.sortModel.length) {
        let sortProp: string;
        if (params.sortModel[0].colId.indexOf('.') !== -1) {
          sortProp = params.sortModel[0].colId.split('.')[1];
        } else {
          sortProp = params.sortModel[0].colId;
        }
        let sortDir: string = params.sortModel[0].sort;
        this.sort = sortProp + ',' + sortDir;
      }
    }

    this.searchService.getPoints(this.schema.id, this.schema.primary, query, this.page, this.sort).then((response: any) => {
      let points: Point[] = [];

      if (response.hasOwnProperty('_embedded')) {
        points = response._embedded.points;
      }

      console.log('fetched ' + points.length + ' points');


      this.page = response.page;
      // Backend pages 0-based, Bootstrap pagination 1-based
      this.page.number += 1;

      if (params) {
        // if on or after the last page, work out the last row.
        let lastRow: number = -1;
        if (this.page.totalElements <= params.endRow) {
          lastRow = this.page.totalElements;
        }

        this.table.updateSelections();
        params.successCallback(points, lastRow);
      } else {
        this.table.refreshData();
      }

      this.loading = 'success';
      this.error = undefined;
    },

    (error: any) => {
      this.loading = 'error';
      this.error = error;
    });
  };
}
