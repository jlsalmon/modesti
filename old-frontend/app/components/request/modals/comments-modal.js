'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CommentsModalController
 * @description # CommentsModalController
 */
angular.module('modesti').controller('CommentsModalController', CommentsModalController);

function CommentsModalController($modalInstance, request, RequestService, AuthService) {
  var self = this;

  self.request = request;
  self.text = '';

  self.addComment = addComment;
  self.ok = ok;

  /**
   *
   */
  function addComment() {
    if (self.text.length) {
      var comment = {
        text: self.text,
        user: AuthService.getCurrentUser().username,
        timestamp: Date.now()
      };

      self.request.comments.push(comment);

      // Save the request
      RequestService.saveRequest(self.request).then(function() {
        self.text = '';
      });
    }
  }

  function ok() {
    $modalInstance.close();
  }
}
