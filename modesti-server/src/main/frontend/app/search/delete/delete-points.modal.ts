import {AuthService} from '../../auth/auth.service';
import {Request} from '../../request/request';
import {Point} from '../../request/point/point';
import {Schema} from '../../schema/schema';
import IPromise = angular.IPromise;

export class DeletePointsModalController {
  public static $inject: string[] = ['$uibModalInstance', 'points', 'schema', 'AuthService'];

  public request: Request;

  constructor(private $modalInstance: any, private points: Point[], private schema: Schema,
              private authService: AuthService) {
    this.request = new Request();
    this.request.type = 'DELETE';
    this.request.description = '';
    this.request.creator = authService.getCurrentUser().username;
    this.request.assignee = this.request.creator;
    this.request.domain = schema.id;
    this.request.points = points;
  }

  public ok(): void {
    this.$modalInstance.close(this.request);
  }

  public cancel(): void {
    this.$modalInstance.dismiss('cancel');
  }
}
