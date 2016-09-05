import {RequestService} from '../request.service';
import {AlertService} from '../../alert/alert.service';
import {Request} from '../request';
import {Schema} from '../../schema/schema';
import {IStateService} from 'angular-ui-router';

export class CloneRequestModalController {
  public static $inject: string[] = ['$uibModalInstance', '$state', 'request', 'schema',
    'RequestService', 'AlertService'];

  public cloning: string = undefined;

  constructor(private $modalInstance: any, private $state: IStateService, private request: Request,
              private schema: Schema, private requestService: RequestService, private alertService: AlertService) {}

  public clone(): void {
    this.cloning = 'started';

    this.requestService.cloneRequest(this.request, this.schema).then((location: any) => {
      // Strip request ID from location
      let id: string = location.substring(location.lastIndexOf('/') + 1);
      console.log('cloned request ' + this.request.requestId + ' to new request ' + id);

      this.$state.go('request', {id: id}).then(() => {
        this.cloning = 'success';
        this.$modalInstance.close();
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
}
