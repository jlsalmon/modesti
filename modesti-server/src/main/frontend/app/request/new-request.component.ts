import {RequestService} from './request.service';
import {SchemaService} from '../schema/schema.service';
import {AuthService} from '../auth/auth.service';
import {StatsService} from '../stats/stats-service';
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
  public static $inject: string[] = ['$state', 'RequestService', 'SchemaService', 'AuthService', 'StatsService'];

  public schemas: Schema[] = [];
  public request: Request;
  public domainSpecificFields: Field[] = [];
  public fieldValues: any[] = [];
  public submitting: string = undefined;
  public error: string;

  public constructor(private $state: any, private requestService: RequestService,
                     private schemaService: SchemaService, private authService: AuthService,
                     private statsService: StatsService) {}

  public $onInit(): void {
    this.schemaService.getSchemas().then((schemas: Schema[]) => {
      schemas.forEach((schema: Schema) => {
        if (schema.configuration === null || schema.configuration.createFromUi === true) {
          this.schemas.push(schema);
        }
      });

      this.request = new Request();
      this.request.type = 'CREATE';
      this.request.description = '';
      this.request.creator = this.authService.getCurrentUser().username;
      this.statsService.recordVisit('new-request');
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
      if (values.length == 1) {
        // Auto select the only value
        this.request.properties[field.id] = values[0];
      }
    });
  }

  public submit(form: IFormController): void {
    if (form.$invalid) {
      console.log('form invalid');
      return;
    }

    this.submitting = 'started';
    this.request.assignee = this.request.creator;

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
