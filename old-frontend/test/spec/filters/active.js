'use strict';

describe('Filter: active', function () {

  // load the filter's module
  beforeEach(module('modesti'));

  // initialize a new instance of the filter before each test
  var active;
  beforeEach(inject(function ($filter) {
    active = $filter('active');
  }));

  it('should return an empty list', function () {
    var datasources = [];
    var request = { points: [] };
    expect(active(datasources, request)).toEqual(datasources);
  });

});
