import {SearchService} from './search.service';
import {SchemaService} from '../schema/schema.service';
import {RequestService} from '../request/request.service';
import {AlertService} from '../alert/alert.service';
import {Table} from '../table/table';
import {TableFactory} from '../table/table-factory';
import {Schema} from '../schema/schema';
import {Point} from '../request/point/point';
import {Category} from '../schema/category/category';
import {Field} from '../schema/field/field';
import {ColumnFactory} from '../table/column-factory';
import {IComponentOptions, IPromise} from 'angular';
import {IStateService} from 'angular-ui-router';
import IRootScopeService = angular.IRootScopeService;

export class SearchComponent implements IComponentOptions {
  public templateUrl: string = '/search/search.component.html';
  public controller: Function = SearchController;
  public bindings: any = {
    schemas: '='
  };
}

class SearchController {
  public static $inject: string[] = ['$rootScope', '$uibModal', '$state', 'SearchService',
                                     'SchemaService', 'RequestService', 'AlertService'];

  public schema: Schema;
  public schemas: Schema[];
  public table: Table;
  public filters: any[];
  public query: string;
  public page: any = {number: 0, size: 100};
  public sort: string;
  public loading: string;
  public error: string;
  public submitting: string;

  constructor(private $rootScope: IRootScopeService, private $modal: any, private $state: IStateService,
              private searchService: SearchService, private schemaService: SchemaService,
              private requestService: RequestService, private alertService: AlertService) {

    this.activateSchema(this.schemas[0]);

    let settings: any = {
      getRows: this.search
    };

    this.table = TableFactory.createTable('ag-grid', this.schema, [], settings);

    $rootScope.$on('modesti:searchFiltersChanged', () => {
      this.search();
    });
  }

  public activateSchema(schema: Schema): void {
    this.schema = schema;

    if (this.table) {
      this.table.refreshColumnDefs();
      this.table.refreshData();
    }
  }

  public search = (params: any): void => {
    this.loading = 'started';
    console.log('searching');

    let query: string = this.parseQuery();

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

    this.searchService.getPoints(this.schema.id, query, this.page, this.sort).then((response: any) => {
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

      angular.forEach(response._links, (item: any) => {
        if (item.rel === 'next') {
          this.page.next = item.href;
        }

        if (item.rel === 'prev') {
          this.page.prev = item.href;
        }
      });

      this.loading = 'success';
      this.error = undefined;
    },

    (error: any) => {
      this.loading = 'error';
      this.error = error;
    });
  };

  public parseQuery(params?: any): string {
    let expressions: string[] = [];

    //for (let key in params.filterModel) {
    //  if (params.filterModel.hasOwnProperty(key)) {
    //    let filter: any = params.filterModel[key];
    //    let field: any = this.schema.getField(key.split('.')[1]);
    //
    //    if (filter.filter !== undefined && filter.filter !== '') {
    //
    //      let property: string;
    //      if (field.type === 'autocomplete') {
    //        let modelAttribute: string = field.model ? field.model : 'value';
    //        property = field.id + '.' + modelAttribute;
    //      } else {
    //        property = field.id;
    //      }
    //
    //      let operation: string = this.parseOperation(filter.type);
    //      let expression: string = property + ' ' + operation + ' "' + filter.filter + '"';
    //
    //      if (expressions.indexOf(expression) === -1) {
    //        expressions.push(expression);
    //      }
    //    }
    //  }
    //}

    this.schema.getAllFields().forEach((field: Field) => {

      if (field.filter && field.filter.value !== undefined && field.filter.value !== '') {

        let property: string;
        if (field.type === 'autocomplete') {
          let modelAttribute: string = field.model ? field.model : 'value';
          property = field.id + '.' + modelAttribute;
        } else {
          property = field.id;
        }

        let operation: string = this.parseOperation(field.filter.operation);
        let expression: string = property + ' ' + operation + ' "' + field.filter.value + '"';

        if (expressions.indexOf(expression) === -1) {
          expressions.push(expression);
        }
      }
    });

    let query: string = expressions.join(' and ');
    console.log('parsed query: ' + query);
    return query;
  }

  public parseOperation(operation: string): string {
    if (operation === 'equals') {
      return ' == ';
    } else {
      console.warn('not supported!');
      return ' == ';
    }
  }

  public updatePoints(): void {
    let modalInstance: any = this.$modal.open({
      animation: false,
      templateUrl: '/search/update/update-points.modal.html',
      controller: 'UpdatePointsModalController as ctrl',
      size: 'lg',
      resolve: {
        points: () => this.table.hot.getData(),
        schema: () => this.schema
      }
    });

    modalInstance.result.then((request: any) => {
      console.log('creating update request');

      this.submitting = 'started';

      // Post form to server to create new request.
      this.requestService.createRequest(request).then((location: string) => {
        // Strip request ID from location.
        let id: string = location.substring(location.lastIndexOf('/') + 1);
        // Redirect to point entry page.
        this.$state.go('request', {id: id}).then(() => {
          this.submitting = 'success';

          this.alertService.add('success', 'Update request #' + id + ' has been created.');
        });
      },

      () => {
        this.submitting = 'error';
      });
    });
  }

  public queryFieldValues(field: any, value: string): IPromise<any[]> {
    return this.schemaService.queryFieldValues(field, value, undefined);
  }

  public getOptionValue(option: any): string {
    return typeof option === 'object' ? option.value : option;
  }

  public getOptionDisplayValue(option: any): string {
    return typeof option === 'object' ? option.value + (option.description ? ': ' + option.description : '') : option;
  }
}
