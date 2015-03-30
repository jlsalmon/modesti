'use strict';

describe('Directive: inputField', function () {

  // load the directive's module
  beforeEach(module('modesti'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<input-field></input-field>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the inputField directive');
  }));
});
