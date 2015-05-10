'use strict';

describe('Filter: enabled', function () {

  // load the filter's module
  beforeEach(module('modesti'));

  // initialize a new instance of the filter before each test
  var enabled;
  beforeEach(inject(function ($filter) {
    enabled = $filter('enabled');
  }));

  it('should return the input prefixed with "enabled filter:"', function () {
    var text = 'angularjs';
    expect(enabled(text)).toBe('enabled filter: ' + text);
  });

});
