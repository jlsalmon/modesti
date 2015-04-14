'use strict';

describe('Directive: editableTable', function () {

  // load the directive's module
  beforeEach(module('modesti'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<editable-table></editable-table>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the editableTable directive');
  }));
});
