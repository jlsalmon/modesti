

export class ExportRequestModalController {
  public static $inject: string[] = ['$uibModalInstance', 'points'];

  public exportVisibleColumnsOnly: boolean = false;

  constructor(private $modalInstance: any, private points: number) {}

  public ok(): void {
    console.log("Closing modal with value: ", this.exportVisibleColumnsOnly);
    this.$modalInstance.close(this.exportVisibleColumnsOnly);
  }

  public cancel(): void {
    this.$modalInstance.dismiss();
  }
}
