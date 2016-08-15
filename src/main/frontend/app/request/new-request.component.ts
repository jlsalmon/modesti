import {RequestService} from './request.service';
import {SchemaService} from '../schema/schema.service';
import {AuthService} from '../auth/auth.service';

export class NewRequestComponent implements ng.IComponentOptions {
  public templateUrl:string = '/request/new-request.component.html';
  public controller:Function = NewRequestController;
}

class NewRequestController {
  public static $inject:string[] = ['$state', 'RequestService', 'SchemaService', 'AuthService'];

  public schemas:any = [];
  public request:any;
  public domainSpecificFields = [];
  public fieldValues = [];
  public submitting:string = undefined;
  public error;

  public constructor(private $state:any,
                     private requestService:RequestService, private schemaService:SchemaService, private authService:AuthService) {
    this.request = {
      type : 'CREATE',
      description : '',
      creator : authService.getCurrentUser().username
    };
  }

  public $onInit() {
    this.schemaService.getSchemas().then((schemas) => this.schemas = schemas);
  }

  public onDomainChanged() {
    var domain = this.request.domain;

    this.schemas.forEach((schema) => {
      if (schema.id === domain) {
        this.domainSpecificFields = schema.fields;
      }
    });
  }

  public queryFieldValues(field, query) {
    return this.schemaService.queryFieldValues(field, query, null).then((values) => {
      this.fieldValues = values;
    });
  }

  public submit(form) {
    if (form.$invalid) {
      console.log('form invalid');
      return;
    }

    this.submitting = 'started';

    // Post form to server to create new request.
    this.requestService.createRequest(this.request).then((location) => {
      // Strip request ID from location.
      var id = location.substring(location.lastIndexOf('/') + 1);
      // Redirect to point entry page.
      this.$state.go('request', { id: id }).then(() => {
        this.submitting = 'success';
      });
    },

    (error) => {
      this.submitting = 'error';
      if (error.data && error.data.message) {
        this.error = error.data.message;
      } else {
        this.error = error.statusText;
      }
    });
  }

}
