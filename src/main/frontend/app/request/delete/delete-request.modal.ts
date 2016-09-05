import {Request} from '../request';

export class DeleteRequestModalController {
  public static $inject: string[] = ['$uibModalInstance', 'request'];

  constructor(private $modalInstance: any, private request: Request) {}

  public ok(): void {
    this.$modalInstance.close();
  }

  public cancel(): void {
    this.$modalInstance.dismiss();
  }
}
