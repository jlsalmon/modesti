'use strict';

angular.module('modesti').directive('requestToolbar', function RequestToolbarDirective() {
  return {
    controller: RequestToolbarController,
    controllerAs: 'ctrl',
    templateUrl: 'components/request/toolbar.html',
    scope: {},
    bindToController: {
      request: '=',
      tasks: '=',
      schema: '=',
      table: '=',
      activeCategory: '='
    }
  };
});

function RequestToolbarController($modal, $state, RequestService, TaskService, AlertService, HistoryService) {
  var self = this;

  self.save = save;
  self.undo = undo;
  self.redo = redo;
  self.cut = cut;
  self.copy = copy;
  self.paste = paste;
  self.assignTask = assignTask;
  self.assignTaskToCurrentUser = assignTaskToCurrentUser;
  self.showHelp = showHelp;
  self.showComments = showComments;
  self.showHistory = showHistory;
  self.cloneRequest = cloneRequest;
  self.deleteRequest = deleteRequest;
  self.getAssignee = getAssignee;
  self.isCurrentTaskRestricted = isCurrentTaskRestricted;


  /**
   *
   */
  function save() {
    var request = self.request;

    RequestService.saveRequest(request).then(function () {
      console.log('saved request');
    }, function () {
      console.log('error saving request');
    });
  }

  /**
   *
   */
  function undo() {
    self.table.undo();
  }

  /**
   *
   */
  function redo() {
    self.table.redo();
  }

  /**
   *
   */
  function cut() {
    self.table.copyPaste.triggerCut();
  }

  /**
   *
   */
  function copy() {
    self.table.copyPaste.setCopyableText();
  }

  /**
   *
   */
  function paste() {
    self.table.copyPaste.triggerPaste();
    self.table.copyPaste.copyPasteInstance.onPaste(function (value) {
      console.log('onPaste(): ' + value);
    });
  }

  /**
   *
   */
  function assignTask() {
    TaskService.assignTask(self.request).then(function (newTask) {
      self.tasks[newTask.name] = newTask;
    });
  }

  /**
   *
   */
  function assignTaskToCurrentUser() {
    TaskService.assignTaskToCurrentUser(self.request).then(function (newTask) {
       self.tasks[newTask.name] = newTask;
    });
  }

  /**
   *
   */
  function showHelp() {
    $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/help-modal.html',
      controller: 'HelpModalController as ctrl'
    });
  }

  /**
   *
   */
  function showComments() {
    $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/comments-modal.html',
      controller: 'CommentsModalController as ctrl',
      resolve: {
        request: function () {
          return self.request;
        }
      }
    });
  }

  /**
   *
   */
  function showHistory() {
    $modal.open({
      animation: false,
      size: 'lg',
      templateUrl: 'components/request/modals/history-modal.html',
      controller: 'HistoryModalController as ctrl',
      resolve: {
        request: function () {
          return self.request;
        },
        history: function () {
          return HistoryService.getHistory(self.request.requestId);
        }
      }
    });
  }

  /**
   *
   */
  function deleteRequest() {
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/delete-modal.html',
      controller: 'DeleteModalController as ctrl',
      resolve: {
        request: function () {
          return self.request;
        }
      }
    });

    modalInstance.result.then(function () {
      RequestService.deleteRequest(self.request.requestId).then(function () {
        console.log('deleted request');
        AlertService.add('success', 'Request was deleted successfully.');
        $state.go('requests');
      },

      function (error) {
        console.log('delete failed: ' + error.statusText);
      });
    },

    function () {
      console.log('delete aborted');
    });
  }

  /**
   *
   */
  function cloneRequest() {
    var modalInstance = $modal.open({
      animation: false,
      templateUrl: 'components/request/modals/clone-modal.html',
      controller: 'CloneModalController as ctrl',
      resolve: {
        request: function () {
          return self.request;
        },
        schema: function () {
          return self.schema;
        }
      }
    });

    modalInstance.result.then(function () {

    },

    function () {
      console.log('clone aborted');
    });
  }

  /**
   *
   */
  function getAssignee() {
    var task = self.tasks[Object.keys(self.tasks)[0]];

    if (!task) {
      return null;
    }

    return task.assignee;
  }

  /**
   *
   * @returns {boolean}
   */
  function isCurrentTaskRestricted() {
    var task = self.tasks[Object.keys(self.tasks)[0]];
    return task && task.candidateGroups.length === 1 && task.candidateGroups[0] === 'modesti-administrators';
  }
}
