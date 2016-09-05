import {AuthService} from '../../auth/auth.service';
import {SchemaService} from '../../schema/schema.service';
import {Request} from '../../request/request';
import {Point} from '../../request/point/point';
import {Schema} from '../../schema/schema';
import {Field} from '../../schema/field';
import IPromise = angular.IPromise;

export class UpdatePointsModalController {
  public static $inject: string[] = ['$uibModalInstance', 'points', 'schema', 'AuthService', 'SchemaService'];

  public request: Request;
  public fieldValues: any[];

  constructor(private $modalInstance: any, private points: Point[], private schema: Schema,
              private authService: AuthService, private schemaService: SchemaService) {
    this.request = new Request('UPDATE', '', authService.getCurrentUser().username);
    this.request.domain = schema.id;
    this.request.points = points;
  }

  public ok(): void {
    this.$modalInstance.close(this.request);
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
