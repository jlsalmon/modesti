'use strict';

describe('Filter: enabled', function () {

  // load the filter's module
  beforeEach(module('modesti'));

  // initialize a new instance of the filter before each test
  var enabled;
  beforeEach(inject(function ($filter) {
    enabled = $filter('enabled');
  }));

  it('should return an empty list', function () {
    var categories = [];
    var status = 'IN_PROGRESS';
    expect(enabled(categories, status)).toEqual(categories);
  });

});
