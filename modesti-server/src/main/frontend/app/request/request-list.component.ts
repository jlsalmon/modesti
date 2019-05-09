import {RequestService} from './request.service';
import {AuthService} from '../auth/auth.service';
import {SchemaService} from '../schema/schema.service';
import {Request} from './request';
import {Schema} from '../schema/schema';
import {User} from '../user/user';
import {Field} from '../schema/field/field';
import { CacheService } from '../cache/cache.service';
import IComponentOptions = angular.IComponentOptions;
import IHttpService = angular.IHttpService;
import ILocationService = angular.ILocationService;
import IScope = angular.IScope;
import IPromise = angular.IPromise;


export class RequestListComponent implements IComponentOptions {
  public templateUrl: string = '/request/request-list.component.html';
  public controller: Function = RequestListController;
}

class RequestListController {
  public static $inject: string[] = ['$http', '$location', '$scope', 'RequestService', 'AuthService', 
    'SchemaService', 'CacheService', '$window'];

  public requests: Request[] = [];
  public statuses: string[] = [];
  public schemas: Schema[] = [];
  public users: User[] = [];
  public types: string[] = ['CREATE', 'UPDATE', 'DELETE'];
  public filter: any = {};
  public sort: string = 'createdAt,desc';
  public loading: string = undefined;
  public page: any = {};
  public hideClosedRequests: boolean = true;

  public constructor(private $http: IHttpService, private $location: ILocationService, private $scope: IScope,
    private requestService: RequestService, private authService: AuthService,
    private schemaService: SchemaService, private cacheService: CacheService,
    private $window: any) {
    this.users.push(authService.getCurrentUser());

    this.resetFilter();
    this.loadCachedValues();
    this.getRequests(1, 15, this.sort, this.filter);
    this.getRequestMetrics();
    this.getSchemas();

    $scope.$watch(() => { return this.filter; }, this.onCriteriaChanged, true);
    $scope.$watch(() => { return this.sort; }, this.onCriteriaChanged, true);
  }

  public isUserAuthenticated(): boolean {
    return this.authService.isCurrentUserAuthenticated();
  }

  public getCurrentUsername(): string {
    return this.authService.getCurrentUser().username;
  }

  public resetFilter(): void {
    this.filter = {
      description: '',
      requestId: '',
      status: '',
      domain: '',
      creator: '',
      assignee: '',
      type: ''
    };
  }

  public getRequests(page: any, size: number, sort: string, filter: string): void {
    this.loading = 'started';

    this.requestService.getRequests(page, size, sort, filter).then((response: any) => {
      if (response.hasOwnProperty('_embedded')) {
        this.requests = response._embedded.requests;
      } else {
        this.requests = [];
      }

      this.page = response.page;
      // Backend pages 0-based, Bootstrap pagination 1-based
      this.page.number += 1;

      if (response._links.hasOwnProperty('next')) {
        this.page.next = response._links.next.href;
      }

      if (response._links.hasOwnProperty('prev')) {
        this.page.prev = response._links.prev.href;
      }

      this.loading = 'success';
    },

      () => {
        this.loading = 'error';
      });
  }

  public deleteRequest(request: Request): void {
    let href: string = request._links.self.href;
    let id: string = href.substring(href.lastIndexOf('/') + 1);

    this.requestService.deleteRequest(id).then(() => {
      console.log('deleted request ' + id);
      this.requests.splice(this.requests.indexOf(request), 1);
    });
  }

  public editRequest(request: Request, openInNewTab: boolean=false): void {
    let href: string = request._links.self.href;
    let id: string = href.substring(href.lastIndexOf('/') + 1).replace('{?projection}', '');

    if (openInNewTab) {
      this.$window.open('/requests/' + id, '_blank');
    } else {
      this.$location.path('/requests/' + id);
    }
  }

  /**
   * Retrieve some metrics about requests. Currently contains only the number
   * of requests of each status.
   */
  public getRequestMetrics(): void {
    this.requestService.getRequestMetrics().then((statuses: string[]) => {
      this.statuses = statuses;
    });
  }

  /**
   * Get the number of requests of a given status
   *
   * @param status
   */
  public getRequestCount(status: string): number {
    for (let key in this.statuses) {
      if (this.statuses.hasOwnProperty(key)) {
        let s: any = this.statuses[key];

        if (s.hasOwnProperty('status') && s.status === status) {
          return s.count;
        }
      }
    }

    return 0;
  }

  public getSchemas(): void {
    this.schemaService.getSchemas().then((schemas: Schema[]) => {
      this.schemas = schemas;
    });
  }

  // TODO: consolidate this functionality with assign-request.modal.ts
  public queryUsers(query: string): IPromise<User[]> {
    return this.$http.get('/api/users/search', {
      params : {
        query : this.parseQuery(query)
      }
    }).then((response: any) => {
      if (!response.data.hasOwnProperty('_embedded')) {
        return [];
      }

      this.users = response.data._embedded.users;
    });
  }

  // TODO: consolidate this functionality with assign-request.modal.ts
  public parseQuery(query: string): string {
    let q: string = '';

    if (query.length !== 0) {
      q += '(username == ' + query;
      q += ' or firstName == ' + query;
      q += ' or lastName == ' + query + ')';
    }

    console.log('parsed query: ' + query);
    return q;
  }

  public hasCustomProperties(request: Request): boolean {
    let has: boolean = false;
    this.schemas.forEach((schema: Schema) => {
      if (schema.id === request.domain && schema.fields) {
        has = true;
      }
    });

    return has;
  }

  public formatCustomProperty(request: Request, key: string): any {
    let value: string = '';
    let field: Field;

    this.schemas.forEach((schema: Schema) => {
      if (schema.id === request.domain && schema.fields) {

        schema.fields.forEach((f: Field) => {
          if (request.properties.hasOwnProperty(f.id) && key === f.id) {
            field = f;

            if (field.type === 'autocomplete') {
              value = field.model ? request.properties[field.id][field.model] : request.properties[field.id].value;
            } else {
              value = request.properties[field.id];
            }
          }
        });
      }
    });

    return {value: value, field: field};
  }

  public onPageChanged(): void {
    this.getRequests(this.page.number, this.page.size, this.sort, this.filter);
  }

  public onCriteriaChanged = () => {
    if (!this.page) {
      return;
    }

    if (this.filter || this.sort) {
      this.saveValuesToCache();
    }

    console.log('filter: ' + JSON.stringify(this.filter));
    this.getRequests(0, this.page.size, this.sort, this.filter);
  }

  private loadCachedValues(): void {
    this.sort   = this.cacheService.filtersCache.get('sort')    || this.sort;
    this.filter = this.cacheService.filtersCache.get('request') || this.filter;
  }

  private saveValuesToCache(): void {
    this.cacheService.filtersCache.put('request', this.filter);
    this.cacheService.filtersCache.put('sort',    this.sort);
  }
}
