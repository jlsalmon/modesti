import {AuthService} from '../auth/auth.service';
import {Request} from './request';
import {Schema} from '../schema/schema';
import {Field} from '../schema/field/field';
import {Point} from './point/point';
import {User} from '../user/user';
import {IPromise, IDeferred, IHttpService, IRootScopeService, IQService} from 'angular';

export class RequestService {
  public static $inject: string[] = ['$http', '$rootScope', '$q', '$uibModal', 'Restangular', 'AuthService'];

  public cache: any = {};

  public constructor(private $http: IHttpService, private $rootScope: any, private $q: IQService,
                     private $modal: any, private restangular: any, private authService: AuthService) {}

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

    this.$http.get('/api/requests/' + id).then((response: any) => {
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

    this.$http.get('/api/requestHistories/' + id).then((response: any) => {
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
      request = request.deserialize(response.data);

      // Cache the newly saved request
      this.cache[request.requestId] = request;

      q.resolve(request);
      this.$rootScope.saving = 'success';
    },

    (error: any) => {
      console.log('error saving request: ' + error.statusText);
      q.reject(error);
      this.$rootScope.saving = 'error';
    });

    return q.promise;
  }

  public createRequest(request: Request): IPromise<String> {
    let q: IDeferred<String> = this.$q.defer();
    let requests: any = this.restangular.all('requests');

    this.$http.post('/api/requests', request).then((response: any) => {
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
    let clone: Request = new Request();
    clone.domain = request.domain;
    clone.type = request.type;
    clone.description = request.description;
    clone.creator = this.authService.getCurrentUser().username;
    clone.points = request.points.slice();
    clone.properties = {};

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

  public deleteRequest(id: string): IPromise<any> {
    let q: IDeferred<any> = this.$q.defer();

    this.$http.delete('/api/requests/' + id).then((response: any) => {
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

  public doAssignCreator(requestId: string, creator: string): IPromise<Request> {
    let q: IDeferred<Request> = this.$q.defer();

    let params: any = {
      action: 'CREATOR',
      creator: creator
    }

    this.$http.post('/api/requests/' + requestId, params).then((response: any) => {
      console.log('assigned request ' + requestId + ' to user ' + params.creator);
      q.resolve(response.data);
    },

    (error: any) => {
      console.log('error assigning request ' + requestId + ": " + error.data.message);
      q.reject(error);
    });

    return q.promise;
  }

  public assignCreator(request: Request): IPromise<Request> {
    let q: IDeferred<Request> = this.$q.defer();
    
    let modalInstance: any = this.$modal.open({
      animation: false,
      templateUrl: '/request/creator/assign-creator.modal.html',
      controller: 'AssignCreatorModalController as ctrl',
      resolve: {
        request: () => request
      }
    });

    modalInstance.result.then((creator: User) => {
      console.log('assigning creator to user ' + creator.username);

      this.doAssignCreator(request.requestId, creator.username).then((newRequest: Request) => {
        console.log('assigned request');
        request.creator = creator.username;
        q.resolve(newRequest);
      });
    });

    return q.promise;
  }

  
  /**
   * Called when the 'pointType' property is modified, the properties specific
   *   to the old category will be removed.
   * 
   * @param oldSource the old data source in the 'pointType' field
   * @param newSource the new data source in the 'pointType' field
   * @param request the request being modified
   * @param schema the schema for the request
   * @param row the row number being modified
   */
  public deleteOldPointTypeProperties(oldSource: string , newSource: string, request: Request, schema: Schema, row: number): IPromise<Request>  {       
    let point: Point = request.points[row];    
    let oldCategory = schema.getCategory(oldSource);
    let newCategory = schema.getCategory(newSource);
    
    let diffFields = oldCategory.fields.filter(function (obj) {
      return !newCategory.fields.some(function(obj2) {
        return obj.id == obj2.id;
      });
    });
    
    diffFields.forEach((field : Field) => {
      console.log("Removing value of property: " + field.id);
      point.setProperty(field.id, "");
    }

    return this.saveRequest(request);
  }

}
