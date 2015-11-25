'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:UploadController
 * @description # UploadController Controller of the modesti
 */
angular.module('modesti').controller('UploadController', UploadController);

function UploadController($location, FileUploader) {
  var self = this;

  self.upload = upload;
  self.edit = edit;

  self.uploader = new FileUploader({
    url : BACKEND_BASE_URL + '/requests/upload',
    withCredentials: true
  });

  self.uploader.filters.push({
    name : 'excelFilter',
    fn : function(item) {
      var re = /(?:\.([^.]+))?$/;
      var extension = re.exec(item.name)[1];
      return extension === 'xls' || extension === 'xlsx';
    }
  });

  /**
   *
   * @param item
   */
  function upload(item) {
    if (!item.description) {
      return;
    }

    item.formData[0] = {description: item.description};
    item.upload();
  }

  /**
   *
   * @param item
   */
  function edit(item) {
    $location.path(item.location);
  }

  self.uploader.onAfterAddingFile = function(fileItem) {
    console.log('onAfterAddingFile', fileItem);
    var input = $('.btn-file :file');
    var numFiles = input.get(0).files ? input.get(0).files.length : 1;
    var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
  };

  $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
    var input = $(this).parents('.input-group').find(':text');
    var log = numFiles > 1 ? numFiles + ' files selected' : label;

    if (input.length) {
      input.val(log);
    }
  });

  self.uploader.onSuccessItem = function(fileItem, response, status, headers) {
    console.log('onSuccessItem', fileItem, response, status, headers);
    // Strip request ID from location.
    var id = headers.location.substring(headers.location.lastIndexOf('/') + 1);
    // Redirect to point entry page.
    fileItem.location = '/requests/' + id;
  };

  self.uploader.onErrorItem = function(fileItem, response, status, headers) {
    console.log('onErrorItem', fileItem, response, status, headers);
    fileItem.errorMessage = response;
  };

  console.log('uploader', self.uploader);
}
