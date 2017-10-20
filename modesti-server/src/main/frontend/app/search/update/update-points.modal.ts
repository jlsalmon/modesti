import {AuthService} from '../../auth/auth.service';
import {SchemaService} from '../../schema/schema.service';
import {Request} from '../../request/request';
import {Point} from '../../request/point/point';
import {Schema} from '../../schema/schema';
import {Field} from '../../schema/field/field';
import IPromise = angular.IPromise;
import IFormController = angular.IFormController;
import {RequestService} from "../../request/request.service";

export class UpdatePointsModalController {
  public static $inject: string[] = ['$uibModalInstance', 'points', 'schema', 'AuthService', 'SchemaService', '$state', 'RequestService'];

  public request: Request;
  public fieldValues: any[];
  public submitting: string = undefined;

  constructor(private $modalInstance: any, private points: Point[], private schema: Schema,
              private authService: AuthService, private schemaService: SchemaService, private $state: any,private requestService: RequestService) {
        this.request = new Request();
        this.request.type = 'CREATE';
        this.request.contractName = '';
        this.request.publisher = '';
        this.request.subscriber = '';
        this.request.creator = authService.getCurrentUser().username;
        this.request.assignee = this.request.creator;
        this.request.domain = schema.id;
        this.request.points = [];
        this.request.description = '';
        this.request.parentRequestId = null;
        this.request.status = 'IN_PROGRESS';
  }

    public ok(): void {
        this.$modalInstance.close(this.request);
    }

    public submit(form: IFormController): void {
        this.request.description = this.request.contractName;
        this.submitting = 'started';
        this.request.assignee = this.request.creator;
        // Post form to server to create new request.
        this.requestService.createRequest(this.request).then((location: string) => {
              // Strip request ID from location.
              let id: string = location.substring(location.lastIndexOf('/') + 1);
            });
        this.$modalInstance.dismiss('cancel');
    }

  public cancel(): void {
        this.$modalInstance.dismiss('cancel');
  }

  public queryFieldValues(field: Field, query: string): IPromise<any> {
        return this.schemaService.queryFieldValues(field, query, undefined).then((values: any[]) => {
          this.fieldValues = values;
        });
      }
}