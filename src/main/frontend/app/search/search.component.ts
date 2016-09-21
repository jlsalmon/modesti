import {SearchService} from './search.service';
import {SchemaService} from '../schema/schema.service';
import {RequestService} from '../request/request.service';
import {AlertService} from '../alert/alert.service';
import {Table} from '../table/table';
import {TableFactory} from '../table/table-factory';
import {Schema} from '../schema/schema';
import {Point} from '../request/point/point';
import {Category} from '../schema/category/category';
import {IComponentOptions, IPromise} from 'angular';
import {IStateService} from 'angular-ui-router';
import {Field} from '../schema/field/field';
import {ColumnFactory} from '../table/column-factory';

export class SearchComponent implements IComponentOptions {
  public templateUrl: string = '/search/search.component.html';
  public controller: Function = SearchController;
  public bindings: any = {
    schemas: '='
  };
}

class SearchController {
  public static $inject: string[] = ['$uibModal', '$state', 'SearchService', 'SchemaService',
    'RequestService', 'AlertService'];

  public schema: Schema;
  public schemas: Schema[];
  public table: Table;
  // public points: Point[] = [];
  public filters: any[];
  public query: string;
  public page: any = {number: 0, size: 100};
  public sort: string;
  public loading: string;
  public error: string;
  public submitting: string;

  constructor(private $modal: any, private $state: IStateService, private searchService: SearchService,
              private schemaService: SchemaService, private requestService: RequestService,
              private alertService: AlertService) {

    this.useDomain(this.schemas[0].id);

    let settings: any = {
      getRows: this.search
    };

    this.table = TableFactory.createTable('ag-grid', this.schema, undefined, settings);
  }

  public useDomain(domain: string): void {
    this.schemas.forEach((schema: any) => {
      if (schema.id === domain) {
        this.schema = schema;

        // Initially expand the first category
        // FIXME: schema should be immutable. Put state on the column defs instead
        this.schema.categories.concat(this.schema.datasources).forEach((category: Category) => {
          if (this.schema.categories.indexOf(category) === 0) {
            category.isActive = true;
            category.isCollapsed = false;
          } else {
            category.isActive = false;
            category.isCollapsed = true;
          }
        });

        if (this.table) {
          this.table.refreshColumnDefs();
        }
        this.onFiltersChanged();
      }
    });
  }

  public onFiltersChanged(): void {
    if (this.table) {
      this.table.refreshData();
    }
  }

  public toggleFilter(field: Field): void {
    // FIXME: maintain filter state on the column defs, not the schema
    field.filter = {value: undefined, operation: 'equals'};
  }

  public search = (params: any): void => {
    this.loading = 'started';
    console.log('searching');

    this.parseQuery();

    let page: any = {number: params.startRow / 100, size: this.page.size};
    console.log('asking for ' + params.startRow + ' to ' + params.endRow + ' (page #' + page + ')');

    let sort: string = '';
    if (params.sortModel.length) {
      let sortProp: string = params.sortModel[0].colId.split('.')[1];
      let sortDir: string = params.sortModel[0].sort;
      sort = sortProp + ',' + sortDir;
    }

    this.searchService.getPoints(this.schema.id, this.query, page, sort).then((response: any) => {
      let points: Point[] = [];

      if (response.hasOwnProperty('_embedded')) {
        points = response._embedded.points;
      }

      console.log('fetched ' + points.length + ' points');


      this.page = response.page;
      // Backend pages 0-based, Bootstrap pagination 1-based
      this.page.number += 1;


      // if on or after the last page, work out the last row.
      let lastRow: number = -1;
      if (this.page.totalElements <= params.endRow) {
        lastRow = this.page.totalElements;
      }

      params.successCallback(points, lastRow);

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

  public parseQuery(params?: any): void {
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

    this.query = expressions.join(' and ');
    console.log('parsed query: ' + this.query);
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

  //public onPageChanged(): void {
  //  //this.search();
  //}

  //public activateCategory(category: any): void {
  //  console.log('activating category "' + category.id + '"');
  //  this.activeCategory = category;
  //  let columns: any[] = this.columnFactory.createColumnDefinitions(this.activeCategory.fields, this.schema, undefined);
  //  this.table.reload(columns);
  //  // $localStorage.lastActiveCategory[self.request.requestId] = category;
  //  // getColumns();
  //}

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
