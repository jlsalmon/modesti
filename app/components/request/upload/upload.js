'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:UploadController
 * @description # UploadController Controller of the modesti
 */
angular.module('modesti').controller('UploadController', UploadController);

function UploadController($location, $cookies, FileUploader) {
  var self = this;

  self.uploader = new FileUploader({
    url : 'http://localhost:8080/requests/upload',
    withCredentials: true
  });

  self.uploader.filters.push({
    name : 'excelFilter',
    fn : function(item, options) {
      var re = /(?:\.([^.]+))?$/;
      var extension = re.exec(item.name)[1];
      return extension == 'xls' || extension == 'xlsx';
    }
  });

  self.uploader.onWhenAddingFileFailed = function(item, filter, options) {
    console.log('onWhenAddingFileFailed', item, filter, options);
  };

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
    } else {
      if (log)
        alert(log);
    }
  });

  self.uploader.onAfterAddingAll = function(addedFileItems) {
    console.log('onAfterAddingAll', addedFileItems);
  };
  self.uploader.onBeforeUploadItem = function(item) {
    console.log('onBeforeUploadItem', item);
  };
  self.uploader.onProgressItem = function(fileItem, progress) {
    console.log('onProgressItem', fileItem, progress);
  };
  self.uploader.onProgressAll = function(progress) {
    console.log('onProgressAll', progress);
  };
  self.uploader.onSuccessItem = function(fileItem, response, status, headers) {
    console.log('onSuccessItem', fileItem, response, status, headers);
    // Strip request ID from location.
    var id = headers.location.substring(headers.location.lastIndexOf('/') + 1);
    // Redirect to point entry page.
    $location.path("/requests/" + id);
  };
  self.uploader.onErrorItem = function(fileItem, response, status, headers) {
    console.log('onErrorItem', fileItem, response, status, headers);
  };
  self.uploader.onCancelItem = function(fileItem, response, status, headers) {
    console.log('onCancelItem', fileItem, response, status, headers);
  };
  self.uploader.onCompleteItem = function(fileItem, response, status, headers) {
    console.log('onCompleteItem', fileItem, response, status, headers);
  };
  self.uploader.onCompleteAll = function() {
    console.log('onCompleteAll');
  };

  console.log('uploader', self.uploader);
};
