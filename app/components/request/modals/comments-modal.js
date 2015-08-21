'use strict';

/**
 * @ngdoc function
 * @name modesti.controller:CommentsModalController
 * @description # CommentsModalController Controller of the modesti
 */
angular.module('modesti').controller('CommentsModalController', CommentsModalController);

function CommentsModalController($modalInstance, request, RequestService, AuthService) {
  var self = this;

  self.request = request;
  self.text = '';

  self.addComment = addComment;
  self.close = close;

  /**
   *
   */
  function addComment() {
    if (self.text.length) {
      var comment = {
        text: self.text,
        user: AuthService.getCurrentUser(),
        timestamp: Date.now()
      };

      self.request.comments.push(comment);

      // Save the request
      RequestService.saveRequest(self.request).then(function() {
        self.text = '';
      });
    }
  }

  function close() {
    $modalInstance.close();
  }
}