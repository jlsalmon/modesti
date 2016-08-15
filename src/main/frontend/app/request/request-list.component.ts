import {RequestService} from './request.service';
import {AuthService} from '../auth/auth.service';
import {SchemaService} from '../schema/schema.service';

export class RequestListComponent implements ng.IComponentOptions {
  public templateUrl:string = '/request/request-list.component.html';
  public controller:Function = RequestListController;
}

class RequestListController {
  public static $inject:string[] = ['$http', '$location', '$scope', 'RequestService', 'AuthService', 'SchemaService'];

  public requests = [];
  public statuses = [];
  public schemas = [];
  public users = [];
  public types = ['CREATE', 'UPDATE', 'DELETE'];
  public filter = {};
  public sort = 'createdAt,desc';
  public loading = undefined;
  public page:any = {};
  
  public constructor(private $http:any, private $location:any, private $scope:any,
                     private requestService:any, private authService:any, private schemaService:any) {
    this.users.push(authService.getCurrentUser());

    this.resetFilter();
    this.getRequests(1, 15, this.sort, this.filter);
    this.getRequestMetrics();
    this.getSchemas();

    $scope.$watch(() => { return this.filter; }, this.onCriteriaChanged, true);
    $scope.$watch(() => { return this.sort; }, this.onCriteriaChanged, true);
  }

  public isUserAuthenticated() {
    return this.authService.isCurrentUserAuthenticated();
  }

  public getCurrentUsername() {
    return this.authService.getCurrentUser().username;
  }

  public resetFilter() {
    console.log('filter reset');
    this.filter = {
      description: '',
      status: '',
      domain: '',
      creator: '',
      assignee: '',
      type: ''
    };
  }

  public getRequests(page, size, sort, filter) {
    this.loading = 'started';

    this.requestService.getRequests(page, size, sort, filter).then((response) => {
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

  public deleteRequest(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1);

    this.requestService.deleteRequest(id).then(() => {
      console.log('deleted request ' + id);
      this.requests.splice(this.requests.indexOf(request), 1);
    },

    () => {
      // something went wrong deleting the request
    });
  }

  public editRequest(request) {
    var href = request._links.self.href;
    var id = href.substring(href.lastIndexOf('/') + 1).replace('{?projection}', '');

    this.$location.path('/requests/' + id);
  }

  /**
   * Retrieve some metrics about requests. Currently contains only the number of requests of each status.
   */
  public getRequestMetrics() {
    this.requestService.getRequestMetrics().then((statuses) => {
      this.statuses = statuses;
    });
  }

  /**
   * Get the number of requests of a given status
   *
   * @param status
   */
  public getRequestCount(status) {
    for (var key in this.statuses) {
      if (this.statuses.hasOwnProperty(key)) {
        var s = this.statuses[key];

        if (s.hasOwnProperty('status') && s.status === status) {
          return s.count;
        }
      }
    }

    return 0;
  }

  public getSchemas() {
    this.schemaService.getSchemas().then((schemas) => {
      this.schemas = schemas;
    });
  }

  // TODO: consolidate this functionality with assign-request.modal.ts
  public queryUsers(query) {
    return this.$http.get('/api/users/search', {
      params : {
        query : this.parseQuery(query)
      }
    }).then((response) => {
      if (!response.data.hasOwnProperty('_embedded')) {
        return [];
      }

      this.users = response.data._embedded.users;
    });
  }

  // TODO: consolidate this functionality with assign-request.modal.ts
  public parseQuery(query) {
    var q = '';

    if (query.length !== 0) {
      q += '(username == ' + query;
      q += ' or firstName == ' + query;
      q += ' or lastName == ' + query + ')';
    }

    console.log('parsed query: ' + query);
    return q;
  }

  public hasCustomProperties(request) {
    var has = false;
    this.schemas.forEach((schema) => {
      if (schema.id === request.domain && schema.fields) {
        has = true;
      }
    });

    return has;
  }

  public formatCustomProperty(request, key) {
    var value = '';
    var field:any = {};

    this.schemas.forEach((schema) => {
      if (schema.id === request.domain && schema.fields) {

        schema.fields.forEach((f) => {
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

  public onPageChanged() {
    this.getRequests(this.page.number, this.page.size, this.sort, this.filter);
  }

  public onCriteriaChanged = () => {
    if (!this.page) {
      return;
    }

    console.log('filter: ' + JSON.stringify(this.filter));
    this.getRequests(0, this.page.size, this.sort, this.filter);
  }
}
