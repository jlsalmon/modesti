import {RequestService} from './request.service';
import {SchemaService} from '../schema/schema.service';
import {AuthService} from '../auth/auth.service';
import {Request} from './request';
import {Schema} from '../schema/schema';
import {Field} from '../schema/field/field';
import IComponentOptions = angular.IComponentOptions;
import IFormController = angular.IFormController;
import IPromise = angular.IPromise;

export class NewRequestComponent implements IComponentOptions {
  public templateUrl: string = '/request/new-request.component.html';
  public controller: Function = NewRequestController;
}

class NewRequestController {
  public static $inject: string[] = ['$state', 'RequestService', 'SchemaService', 'AuthService'];

  public schemas: Schema[] = [];
  public request: Request;
  public domainSpecificFields: Field[] = [];
  public fieldValues: any[] = [];
  public submitting: string = undefined;
  public error: string;

  public constructor(private $state: any, private requestService: RequestService,
                     private schemaService: SchemaService, private authService: AuthService) {}

  public $onInit(): void {
    this.schemaService.getSchemas().then((schemas: Schema[]) => {
      this.schemas = schemas;

      this.request = new Request();
      this.request.type = 'CREATE';
      this.request.description = '';
      this.request.creator = this.authService.getCurrentUser().username;
    });
  }

  public onDomainChanged(): void {
    let domain: string = this.request.domain;

    this.schemas.forEach((schema: Schema) => {
      if (schema.id === domain) {
        this.domainSpecificFields = schema.fields;
      }
    });
  }

  public queryFieldValues(field: Field, query: string): IPromise<void> {
    return this.schemaService.queryFieldValues(field, query, undefined).then((values: any[]) => {
      this.fieldValues = values;
    });
  }

  public submit(form: IFormController): void {
    if (form.$invalid) {
      console.log('form invalid');
      return;
    }

    this.submitting = 'started';

    // Post form to server to create new request.
    this.requestService.createRequest(this.request).then((location: string) => {
      // Strip request ID from location.
      let id: string = location.substring(location.lastIndexOf('/') + 1);
      // Redirect to point entry page.
      this.$state.go('request', { id: id }).then(() => {
        this.submitting = 'success';
      });
    },

    (error: any) => {
      this.submitting = 'error';
      if (error.data && error.data.message) {
        this.error = error.data.message;
      } else {
        this.error = error.statusText;
      }
    });
  }

}
