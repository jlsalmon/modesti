export class UploadRequestComponent implements ng.IComponentOptions {
  public templateUrl:string = '/request/upload-request.component.html';
  public controller:Function = UploadRequestController;
}

class UploadRequestController {
  public static $inject:string[] = ['$location', 'FileUploader'];

  public uploader:any;

  public constructor(private $location:any, private FileUploader:any) {
    this.uploader = new FileUploader({
      url : '/api/requests/upload',
      withCredentials: true
    });

    this.uploader.filters.push({
      name : 'excelFilter',
      fn : function(item) {
        var re = /(?:\.([^.]+))?$/;
        var extension = re.exec(item.name)[1];
        return extension === 'xls' || extension === 'xlsx';
      }
    });

    this.uploader.onAfterAddingFile = this.onAfterAddingFile;
    this.uploader.onSuccessItem = this.onSuccessItem;
    this.uploader.onErrorItem = this.onErrorItem;

    $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
      var input = $(this).parents('.input-group').find(':text');
      var log = numFiles > 1 ? numFiles + ' files selected' : label;

      if (input.length) {
        input.val(log);
      }
    });
  }

  public upload(item) {
    if (!item.description) {
      return;
    }

    item.formData[0] = {description: item.description};
    item.upload();
  }

  public edit(item) {
    this.$location.path(item.location);
  }

  public onAfterAddingFile(fileItem) {
    console.log('onAfterAddingFile', fileItem);
    var input = $('.btn-file :file');
    var numFiles = input.get(0)['files'] ? input.get(0)['files'].length : 1;
    var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
  }

  public onSuccessItem (fileItem, response, status, headers) {
    console.log('onSuccessItem', fileItem, response, status, headers);
    // Strip request ID from location.
    var id = headers.location.substring(headers.location.lastIndexOf('/') + 1);
    // Redirect to point entry page.
    fileItem.location = '/requests/' + id;
    fileItem.warnings = response;
  }

  public onErrorItem(fileItem, response, status, headers) {
    console.log('onErrorItem', fileItem, response, status, headers);
    fileItem.errorMessage = response;
  }
}
