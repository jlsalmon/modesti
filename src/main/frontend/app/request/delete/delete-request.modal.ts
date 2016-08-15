export class DeleteRequestModalController {
  public static $inject:string[] = ['$uibModalInstance', 'request'];

  constructor(private $modalInstance:any, private request:any) {}

  public ok() {
    this.$modalInstance.close();
  }

  public cancel() {
    this.$modalInstance.dismiss();
  }
}
