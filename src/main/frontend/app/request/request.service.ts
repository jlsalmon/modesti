import {AuthService} from '../auth/auth.service';

export class RequestService {
  public static $inject:string[] = ['$http', '$rootScope', '$q', 'Restangular', 'AuthService'];
  
  public cache:any = {};
  
  public constructor(private $http:any, private $rootScope:any, private $q:any, private Restangular:any, 
                     private authService:AuthService) {}

  public getRequests(page, size, sort, filter) {
    var q = this.$q.defer();
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
    }).then((response) => {
      q.resolve(response.data);
    },

    (error) => {
      console.log('error: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public parseQuery(filter) {
    var expressions = [];

    for (var property in filter) {
      if (typeof filter[property] === 'string' && filter[property] !== '') {
        expressions.push(property.toString() + '=="' + filter[property] + '"');
      }

      else if (filter[property] instanceof Array && filter[property].length > 0) {
        expressions.push(property.toString() + '=in=' + '("' + filter[property].join('","') + '")');
      }

      else if (typeof filter[property] === 'object') {
        for (var subProperty in filter[property]) {

          if (typeof filter[property][subProperty] === 'string' && filter[property][subProperty] !== '') {
            expressions.push(property.toString() + '.' + subProperty.toString() + '=="' + filter[property][subProperty] + '"');
          }

          else if (filter[property][subProperty] instanceof Array && filter[property][subProperty].length > 0) {
            expressions.push(property.toString() + '.' + subProperty.toString() + '=in=' + '("' + filter[property][subProperty].join('","') + '")');
          }
        }
      }
    }

    var query = expressions.join('; ');

    console.log('parsed query: ' + query);
    return query;
  }

  public getRequest(id) {
    var q = this.$q.defer();
    console.log('fetching request ' + id);

    this.Restangular.one('requests', id).get().then((response) => {
      var request = response.data;

      // Make a copy for sorting/filtering
      request = this.Restangular.copy(request);

      q.resolve(request);
    },

    (error) => {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public getRequestHistory(id) {
    var q = this.$q.defer();

    console.log('fetching history for request ' + id);

    this.Restangular.one('requestHistories', id).get().then((response) => {
      var history = response.data;
      q.resolve(history);
    },

    (error) => {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public getChildRequests(request) {
    var childRequestIds = request.childRequestIds;
    var promises = [];

    angular.forEach(childRequestIds, (childRequestId) => {
      promises.push(this.getRequest(childRequestId));
    });

    return this.$q.all(promises);
  }

  public saveRequest(request) {
    this.$rootScope.saving = 'started';
    var q = this.$q.defer();

    this.$http.put('/api/requests/' + request.requestId, request).then((response) =>{
      console.log('saved request');

      // Cache the newly saved request
      this.cache[request.requestId] = response.data;

      q.resolve(this.cache[request.requestId]);
      this.$rootScope.saving = 'success';

    }, (error) => {
      console.log('error saving request: ' + error.data.message);
      q.reject(error);
      this.$rootScope.saving = 'error';
    });

    return q.promise;
  }

  public createRequest(request) {
    var q = this.$q.defer();
    var requests = this.Restangular.all('requests');

    requests.post(request).then((response) => {
      var location = response.headers('Location');
      console.log('created request: ' + location);
      q.resolve(location);
    },

    (error) => {
      console.log(error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public cloneRequest(request, schema) {
    var clone = {
      domain: request.domain,
      type : request.type,
      description : request.description,
      creator : this.authService.getCurrentUser().username,
      //subsystem: request.subsystem,
      points: request.points.slice(),
      properties: {}
    };

    // Clone request-level properties that are defined in the schema
    if (schema.fields) {
      schema.fields.forEach((field) => {
        if (request.properties.hasOwnProperty(field.id)) {
          clone.properties[field.id] = request.properties[field.id];
        }
      });
    }

    clone.points.forEach((point) => {
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

  public deleteRequest(id) {
    var q = this.$q.defer();

    this.Restangular.one('requests', id).remove().then((response) => {
      console.log('deleted request: ' + response);
      q.resolve(response);
    },

    (error) => {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public isCurrentUserOwner(request) {
    var user = this.authService.getCurrentUser();
    if (!user) {
      return false;
    }

    return user && user.username === request.creator;
  }

  public getRequestMetrics() {
    var q = this.$q.defer();

    this.$http.get('/api/metrics').then((response) => {
      q.resolve(response.data);
    },

    (error) => {
      console.log(error.status + ' ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }

  public clearCache() {
    this.cache = {};
  }
}
