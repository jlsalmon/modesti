import {AuthService} from '../auth/auth.service';
import {Request} from './request';
import {Schema} from '../schema/schema';
import {Field} from '../schema/field/field';
import {Point} from './point/point';
import {User} from '../user/user';
import IPromise = angular.IPromise;
import IDeferred = angular.IDeferred;

export class RequestService {
  public static $inject: string[] = ['$http', '$rootScope', '$q', 'Restangular', 'AuthService'];

  public cache: any = {};

  public constructor(private $http: any, private $rootScope: any, private $q: any, private restangular: any,
                     private authService: AuthService) {}

  public getRequests(page: number, size: number, sort: string, filter: string): IPromise<Request[]> {
    let q: IDeferred<Request[]> = this.$q.defer();
    page = page || 0;
    size = size || 15;
    sort = sort || 'createdAt,desc';

    this.$http.get('/api/requests/search', {
      params: {
        query: this.parseQuery(filter),
        page: page - 1,
        size: size,
        sort: sort
      }
    }).then((response: any) => {
      q.resolve(response.data);
    },

    (error: any) => {
      console.log('error: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public parseQuery(filter: any): string {
    let expressions: string[] = [];

    for (let property in filter) {
      if (typeof filter[property] === 'string' && filter[property] !== '') {
        expressions.push(property.toString() + '=="' + filter[property] + '"');
      } else if (filter[property] instanceof Array && filter[property].length > 0) {
        expressions.push(property.toString() + '=in=' + '("' + filter[property].join('","') + '")');
      } else if (typeof filter[property] === 'object') {
        for (let subProperty in filter[property]) {

          if (typeof filter[property][subProperty] === 'string' && filter[property][subProperty] !== '') {
            expressions.push(property.toString() + '.' + subProperty.toString()
              + '=="' + filter[property][subProperty] + '"');
          } else if (filter[property][subProperty] instanceof Array && filter[property][subProperty].length > 0) {
            expressions.push(property.toString() + '.' + subProperty.toString()
              + '=in=' + '("' + filter[property][subProperty].join('","') + '")');
          }
        }
      }
    }

    let query: string = expressions.join('; ');

    console.log('parsed query: ' + query);
    return query;
  }

  public getRequest(id: string): IPromise<Request> {
    let q: IDeferred<Request> = this.$q.defer();
    console.log('fetching request ' + id);

    this.restangular.one('requests', id).get().then((response: any) => {
      let request: Request = new Request().deserialize(response.data);

      // Make a copy for sorting/filtering
      // request = this.restangular.copy(request);

      q.resolve(request);
    },

    (error: any) => {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public getRequestHistory(id: string): IPromise<any> {
    let q: IDeferred<any> = this.$q.defer();

    console.log('fetching history for request ' + id);

    this.restangular.one('requestHistories', id).get().then((response: any) => {
      let history: any[] = response.data;
      q.resolve(history);
    },

    (error: any) => {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public getChildRequests(request: Request): IPromise<Request[]> {
    let childRequestIds: string[] = request.childRequestIds;
    let promises: IPromise<Request>[] = [];

    angular.forEach(childRequestIds, (childRequestId: string) => {
      promises.push(this.getRequest(childRequestId));
    });

    return this.$q.all(promises);
  }

  public saveRequest(request: Request): IPromise<Request> {
    this.$rootScope.saving = 'started';
    let q: IDeferred<Request> = this.$q.defer();

    this.$http.put('/api/requests/' + request.requestId, request).then((response: any) => {
      console.log('saved request');

      // Cache the newly saved request
      this.cache[request.requestId] = new Request().deserialize(response.data);

      q.resolve(this.cache[request.requestId]);
      this.$rootScope.saving = 'success';

    }, (error: any) => {
      console.log('error saving request: ' + error.statusText);
      q.reject(error);
      this.$rootScope.saving = 'error';
    });

    return q.promise;
  }

  public createRequest(request: Request): IPromise<String> {
    let q: IDeferred<String> = this.$q.defer();
    let requests: any = this.restangular.all('requests');

    requests.post(request).then((response: any) => {
      let location: string = response.headers('Location');
      console.log('created request: ' + location);
      q.resolve(location);
    },

    (error: any) => {
      console.log(error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public cloneRequest(request: Request, schema: Schema): IPromise<String> {
    let clone: Request = {
      domain: request.domain,
      type : request.type,
      description : request.description,
      creator : this.authService.getCurrentUser().username,
      points: request.points.slice(),
      properties: {}
    };

    // Clone request-level properties that are defined in the schema
    if (schema.fields) {
      schema.fields.forEach((field: Field) => {
        if (request.properties.hasOwnProperty(field.id)) {
          clone.properties[field.id] = request.properties[field.id];
        }
      });
    }

    clone.points.forEach((point: Point) => {
      point.dirty = true;
      point.selected = false;
      point.errors = [];

      // TODO: delete properties that are not in the schema
      delete point.valid;
      delete point.properties.approvalResult;
      delete point.properties.addressingResult;
      delete point.properties.cablingResult;
      delete point.properties.testResult;

      if (request.type === 'CREATE') {
        delete point.properties.pointId;
      }
    });

    return this.createRequest(clone);
  }

  public deleteRequest(id: string): IPromise<void> {
    let q: IDeferred<void> = this.$q.defer();

    this.restangular.one('requests', id).remove().then((response: any) => {
      console.log('deleted request: ' + response);
      q.resolve(response);
    },

    (error: any) => {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public isCurrentUserOwner(request: Request): boolean {
    let user: User = this.authService.getCurrentUser();
    if (!user) {
      return false;
    }

    return user && user.username === request.creator;
  }

  public getRequestMetrics(): IPromise<any[]> {
    let q: IDeferred<any[]> = this.$q.defer();

    this.$http.get('/api/metrics').then((response: any) => {
      q.resolve(response.data);
    },

    (error: any) => {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public clearCache(): void {
    this.cache = {};
  }
}
