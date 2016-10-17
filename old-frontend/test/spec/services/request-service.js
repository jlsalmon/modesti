'use strict';

describe('Service: RequestService', function () {

  // load the service's module
  beforeEach(module('modesti'));

  // instantiate service
  var requestService;
  beforeEach(inject(function (RequestService) {
    requestService = RequestService;
  }));

  it('should do something', function () {
    expect(!!requestService).toBe(true);
  });

});
