import {AuthService} from '../../auth/auth.service';
import {Request} from '../../request/request';
import {Point} from '../../request/point/point';
import {Schema} from '../../schema/schema';
import {SchemaService} from '../../schema/schema.service';
import {Field} from '../../schema/field/field';
import IPromise = angular.IPromise;

export class DeletePointsModalController {
  public static $inject: string[] = ['$uibModalInstance', 'points', 'schema', 'AuthService', 'SchemaService'];

  public request: Request;
  public showFieldsOnDelete: boolean;
  public fieldValues : any[] = [];   

  constructor(private $modalInstance: any, private points: Point[], private schema: Schema,
              private authService: AuthService, private schemaService: SchemaService) {
    this.request = new Request();
    this.request.type = 'DELETE';
    this.request.description = '';
    this.request.creator = authService.getCurrentUser().username;
    this.request.assignee = this.request.creator;
    this.request.domain = schema.id;
    this.request.points = points;
  }

  public $onInit(): void {
    this.showFieldsOnDelete = this.schema.configuration !== null && this.schema.configuration.showFieldsOnDelete === true;
  }

  public ok(): void {
    this.$modalInstance.close(this.request);
  }

  public cancel(): void {
    this.$modalInstance.dismiss('cancel');
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
