import {AuthService} from '../../auth/auth.service';
import {SchemaService} from '../../schema/schema.service';

export class UpdatePointsModalController {
  public static $inject:string[] = ['$uibModalInstance', 'points', 'schema', 'AuthService', 'SchemaService'];

  public points:any[];
  public schema:any;
  public request:Object;
  public fieldValues:any;

  constructor(private $modalInstance:any, private points:any, private schema:any,
              private authService:AuthService, private schemaService:SchemaService) {
    this.request = {
      type : 'UPDATE',
      domain : schema.id,
      description : '',
      creator : authService.getCurrentUser().username,
      points: points
    };
  }

  public ok() {
    this.$modalInstance.close(this.request);
  }

  public cancel() {
    this.$modalInstance.dismiss('cancel');
  }

  public queryFieldValues(field, query) {
    return this.schemaService.queryFieldValues(field, query).then((values) => {
      this.fieldValues = values;
    });
  }
}
