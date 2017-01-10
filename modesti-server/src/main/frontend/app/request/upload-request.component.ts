import IComponentOptions = angular.IComponentOptions;
import ILocationService = angular.ILocationService;

export class UploadRequestComponent implements IComponentOptions {
  public templateUrl: string = '/request/upload-request.component.html';
  public controller: Function = UploadRequestController;
}

class UploadRequestController {
  public static $inject: string[] = ['$location', 'FileUploader'];

  public uploader: any;

  public constructor(private $location: ILocationService, private fileUploader: any) {
    this.uploader = new fileUploader({
      url : '/api/requests/upload',
      withCredentials: true
    });

    this.uploader.filters.push({
      name : 'excelFilter',
      fn : (item: any) => {
        let re: RegExp = /(?:\.([^.]+))?$/;
        let extension: string = re.exec(item.name)[1];
        return extension === 'xls' || extension === 'xlsx';
      }
    });

    this.uploader.onAfterAddingFile = this.onAfterAddingFile;
    this.uploader.onSuccessItem = this.onSuccessItem;
    this.uploader.onErrorItem = this.onErrorItem;

    $('.btn-file :file').on('fileselect', (event: Event, numFiles: number, label: string) => {
      let input: JQuery = $(this).parents('.input-group').find(':text');
      let log: string = numFiles > 1 ? numFiles + ' files selected' : label;

      if (input.length) {
        input.val(log);
      }
    });
  }

  public upload(item: any): void {
    if (!item.description) {
      return;
    }

    item.formData[0] = {description: item.description};
    item.upload();
  }

  public edit(item: any): void {
    this.$location.path(item.location);
  }

  public onAfterAddingFile(fileItem: any): void {
    console.log('onAfterAddingFile', fileItem);
    let input: JQuery = $('.btn-file :file');
    let numFiles: number = input.get(0).files ? input.get(0).files.length : 1;
    let label: string = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
  }

  public onSuccessItem(fileItem: any, response: any, status: any, headers: any): void {
    console.log('onSuccessItem', fileItem, response, status, headers);
    // Strip request ID from location.
    let id: string = headers.location.substring(headers.location.lastIndexOf('/') + 1);
    // Redirect to point entry page.
    fileItem.location = '/requests/' + id;
    fileItem.warnings = response;
  }

  public onErrorItem(fileItem: any, response: any, status: any, headers: any): void {
    console.log('onErrorItem', fileItem, response, status, headers);
    fileItem.errorMessage = response;
  }
}
