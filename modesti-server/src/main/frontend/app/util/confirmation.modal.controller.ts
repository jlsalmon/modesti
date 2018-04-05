export class ConfirmationModalController {
  public static $inject: string[] = ['$uibModalInstance'];

  public closeButtonText: string = "Cancel";
  public actionButtonText: string = "Ok";
  public headerText: string = "Proceed?";
  public bodyDesc: string = "Perform this action?";
  public showTextBox: boolean = false;
  public userInput: string = "";

  constructor(private $modalInstance: any) { 
    if ($modalInstance.headerText !== undefined) {
      this.headerText = $modalInstance.headerText;
    }

    if ($modalInstance.bodyDesc !== undefined) {
      this.bodyDesc = $modalInstance.bodyDesc;
    }

    if ($modalInstance.showTextBox !== undefined) {
      this.showTextBox = $modalInstance.showTextBox;
    }
  }

  public ok(): void {
    this.$modalInstance.close(this.userInput);
  }

  public close(): void {
    this.$modalInstance.dismiss('cancel');
  }
}
