import {SearchService} from './search.service';
import {SchemaService} from '../schema/schema.service';
import {RequestService} from '../request/request.service';
import {AlertService} from '../alert/alert.service';
import {StatsService} from '../stats/stats-service';
import {Table} from '../table/table';
import {Schema} from '../schema/schema';
import {Field} from '../schema/field/field';
import {Point} from '../request/point/point';
import {QueryParser} from './query-parser';
import {Filter} from '../table/filter';
import {IComponentOptions, IRootScopeService, IAngularEvent} from 'angular';
import "lodash"

import {TableService} from './table.service';


export class SearchComponent implements IComponentOptions {
  public templateUrl: string = '/search/search.component.html';
  public controller: Function = SearchController;
  public bindings: any = {
    schemas: '='
  };
}

export class SearchController {
  public static $inject: string[] = ['$rootScope', '$uibModal', 'SearchService',
                                     'SchemaService', 'AlertService', 'TableService', 'StatsService'];

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
  public showSelectedPoints: boolean;

  constructor(private $rootScope: IRootScopeService, private $modal: any, 
              private searchService: SearchService, private schemaService: SchemaService,
              private alertService: AlertService,private tableService: TableService, private statsService: StatsService) {

    this.schemas.sort(function(s1: Schema, s2: Schema) {
      if (s1.id < s2.id) return -1;
      if (s1.id > s2.id) return 1;
      return 0;
    });

    this.activateSchema(this.schemas[0]);

    let settings: any = {
      getRows: this.getRows
    };

    this.table = this.tableService.buildTable(this.schema, settings);

    $rootScope.$on('modesti:searchFiltersChanged', (event: IAngularEvent, filters: Map<string, Filter>) => {
      this.filters = filters;
      this.tableService.filters = this.filters;
      this.search();
    });

    this.statsService.recordVisit('search');
  }

  public activateSchema(schema: Schema): void {
    this.schema = schema;

    if (this.table) {
      this.resetFilters();
      this.table.schema = schema;
      this.table.refreshColumnDefs();
      this.table.refreshData();
      this.table.clearSelections();
    }

    this.$rootScope.$emit('modesti:searchDomainChanged', schema.id);
  }

  public resetFilters(): void {
    this.filters = new Map<string, Filter>();
    this.page = {number: 0, size: 100};
    this.table.gridOptions.api.setSortModel(null);
    this.sort = '';
  }

  public getNumPointsSelected() : number {
    return this.table.getSelectedPoints().length;
  }

  public clearSelection() : void {
    this.showSelectedPoints = false;
    this.table.clearSelections();
  }

  public showSelection() : void {
    this.table.showSelectedRowsOnly(this.showSelectedPoints);
  }

  public selectAll() : void {
    if (this.page.totalElements > 200) {
      this.alertService.add('danger', 'Too many points in the filter. </br>Please create a filter containing <= 200 points.');
    } else {
      // Not all the points are loaded in the model, specify the lastRowIndex in call to selectAll
      if (this.page.totalElements > 0) {
        this.table.selectAll(this.page.totalElements - 1);
      }
    }
  }

  public getRows = (params?: any) : void => {
    if (this.showSelectedPoints) {
      let primaryField : Field = this.schema.getPrimaryField();
      let selectedPointIds : string [] = [];
      this.table.getSelectedPoints().forEach((point: Point) => {
        selectedPointIds.push(point.properties[primaryField.id]);
      });
      let filters: Map<string, Filter> = new Map();
      filters['_' + primaryField.id] = { field: primaryField, operation: "in", value: '[' + selectedPointIds.toString() + ']', isOpen: false };
      
      return this.search(params, filters);
    } else {
      return this.search(params);
    }
  }

  public search = (params?: any, applyFilters?: Map<string, Filter>): void => {
    this.loading = 'started';

    let searchFilters = applyFilters === undefined ? this.filters : applyFilters;
    let query: string = QueryParser.parse(searchFilters);

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
