import {Request} from '../request';
import {Change} from './change';

export class RequestHistoryModalController {
  public static $inject: string[] = ['$uibModalInstance', 'request', 'history'];

  constructor(private $modalInstance: any, private request: Request, private history: Change[]) {}

  public ok(): void {
    this.$modalInstance.close();
  }
}
