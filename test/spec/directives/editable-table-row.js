'use strict';

describe('Directive: editableTableRow', function () {

  // load the directive's module
  beforeEach(module('verity'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<editable-table-row></editable-table-row>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the editableTableRow directive');
  }));
});
