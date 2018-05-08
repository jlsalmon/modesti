import {RequestService} from '../request.service';
import {AlertService} from '../../alert/alert.service';
import {Request} from '../request';
import {Schema} from '../../schema/schema';
import {SchemaService} from '../../schema/schema.service';
import {IStateService} from 'angular-ui-router';
import {Field} from '../../schema/field/field';
import {IPromise, IRootScopeService} from 'angular';

export class CloneRequestModalController {
  public static $inject: string[] = ['$uibModalInstance', '$rootScope', '$state', 'request', 'schema', 'RequestService', 'AlertService', 'SchemaService'];

  public cloning: string = undefined;
  public showFieldsOnClone: boolean;
  public fieldValues : any[] = [];   
  
  constructor(private $modalInstance: any, private $rootScope: IRootScopeService, private $state: IStateService, public request: Request,
              private schema: Schema, private requestService: RequestService, private alertService: AlertService, private schemaService: SchemaService) {}

  public $onInit(): void {
    this.showFieldsOnClone = this.schema.configuration !== null && this.schema.configuration.showFieldsOnClone === true;
  }

  public clone(): void { 
    this.cloning = 'started';

    this.requestService.cloneRequest(this.request, this.schema).then((location: any) => {
      // Strip request ID from location
      let id: string = location.substring(location.lastIndexOf('/') + 1);
      console.log('cloned request ' + this.request.requestId + ' to new request ' + id);

      this.$state.go('request', {id: id}).then(() => {
        this.cloning = 'success';
        this.$modalInstance.close();
        this.alertService.clear();
        this.alertService.add('success', 'Request was cloned successfully with id ' + id);
      });
    },

    (error: any) => {
      console.log('clone failed: ' + error.statusText);
      this.cloning = 'error';
    });
  }

  public cancel(): void {
    this.$modalInstance.dismiss();
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
}
