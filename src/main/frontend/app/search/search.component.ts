import {SearchService} from './search.service';
import {SchemaService} from '../schema/schema.service';
import {RequestService} from '../request/request.service';
import {AlertService} from '../alert/alert.service';
import {Utils} from '../utils/utils';

export class SearchComponent implements ng.IComponentOptions {
  public templateUrl: string = '/search/search.component.html';
  public controller: Function = SearchController;
}

class SearchController {
  public static $inject: string[] = ['$uibModal', '$state', 'SearchService', 'SchemaService',
                                     'RequestService', 'AlertService', 'Utils'];

  public schema: any;
  public schemas: any[];
  public domains: any[];
  public points: any[];
  public filters: any[]; // { 'pointDatatype': { /*operation: 'equals',*/ value: 'Boolean' } };
  public query: string;
  public page: any = {number: 0, size: 50};
  public sort: string;
  public activeCategory: any;
  public loading: string;
  public error: string;
  public submitting: string;

  constructor(private $modal: any, private $state: any, private searchService: SearchService,
              private schemaService: SchemaService, private requestService: RequestService,
              private alertService: AlertService, private utils: Utils) {}

  public $onInit(): void {
    this.schemaService.getSchemas().then((schemas: any[]) => {
      this.schemas = schemas;
      this.domains = schemas.map((schema: any) => { return schema.id; });

      this.calculateTableHeight();

      if (this.domains.length > 0) {
        this.useDomain(this.domains[0]);
      }

      if (this.schema) {
        this.activeCategory = this.schema.categories[0];
        this.search();
      }
    });
  }

  public useDomain(domain: string): void {
    this.schemas.forEach((schema: any) => {
      if (schema.id === domain) {
        this.schema = schema;
        this.activeCategory = this.schema.categories[0];
        this.filters = [this.activeCategory.fields[0]];
        this.search();
      }
    });
  }

  public getAllFields(): any[] {
    let fields: any[] = [];
    this.schema.categories.concat(this.schema.datasources).forEach((category: any) => {
      category.fields.forEach((field: any) => {
        field.category = category.name;
        fields.push(field);
      });
    });
    return fields;
  }

  public onFilterRemoved(): void {
    this.search();
  }

  public search(): void {
    this.loading = 'started';
    console.log('searching');

    this.parseQuery();

    this.searchService.getPoints(this.schema.id, this.query, this.page.number, this.page.size, this.sort)
      .then((response: any) => {
      if (response.hasOwnProperty('_embedded')) {
        this.points = response._embedded.points;
      } else {
        this.points = [];
      }

      console.log('fetched ' + this.points.length + ' points');

      this.page = response.page;
      // Backend pages 0-based, Bootstrap pagination 1-based
      this.page.number += 1;

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
      this.points = [];
      this.loading = 'error';
      this.error = error;
    });
  }

  public parseQuery(): void {
    let expressions: string[] = [];

    this.filters.forEach((filter: any) => {
      let field: any = this.utils.getField(this.schema, filter.id);

      // if (typeof filter.field === 'string') {
      //  filter.field = JSON.parse(filter.field);
      // }

      if (filter.value !== null && filter.value !== undefined && filter.value !== '') {

        let property: string;
        if (field.type === 'autocomplete') {
          let modelAttribute: string = field.model ? field.model : 'value';
          property = filter.id + '.' + modelAttribute;
        } else {
          property = filter.id;
        }

        let operation: string = this.parseOperation(filter.operation);
        let expression: string = property + ' ' + operation + ' "' + filter.value + '"';

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
        points: () => this.points,
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
        this.$state.go('request', { id: id }).then(() => {
          this.submitting = 'success';

          this.alertService.add('success', 'Update request #' + id + ' has been created.');
        });
      },

      () => {
        this.submitting = 'error';
      });
    });
  }

  public onPageChanged(): void {
    this.search();
  }

  public activateCategory(category: any): void {
    console.log('activating category "' + category.id + '"');
    this.activeCategory = category;
    // $localStorage.lastActiveCategory[self.request.requestId] = category;
    // getColumns();
  }

  public queryFieldValues(field: any, value: string): any[] {
    return this.schemaService.queryFieldValues(field, value, undefined);
  }

  public getOptionValue(option: any): string {
    return typeof option === 'object' ? option.value : option;
  }

  public getOptionDisplayValue(option: any): string {
    return typeof option === 'object' ? option.value + (option.description ? ': ' + option.description : '') : option;
  }

  /**
   * Calculate the required height for the table so that it fills the screen.
   */
  public calculateTableHeight(): void {
    let mainHeader: any = $('.main-header');
    let requestHeader: any = $('.request-header');
    let toolbar: any = $('.toolbar');
    let table: any = $('.table-wrapper');
    // var log = $('.log');
    let footer: any = $('.footer');

    let height: any = $(window).height() - mainHeader.outerHeight() - requestHeader.outerHeight()
                 - toolbar.outerHeight() - footer.outerHeight();

    console.log($(window).height());
    console.log(mainHeader.height());
    console.log(requestHeader.height());
    console.log(toolbar.height());
    console.log(footer.height());

    table.height(height + 'px');
  }
}
