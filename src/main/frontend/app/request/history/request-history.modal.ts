export class RequestHistoryModalController {
  public static $inject:string[] = ['$uibModalInstance', 'request', 'history'];

  constructor(private $modalInstance:any, private request:any, private history:any) {}

  public ok() {
    this.$modalInstance.close();
  }
}
