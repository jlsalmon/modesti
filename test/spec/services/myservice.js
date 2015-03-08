'use strict';

describe('Service: myServices', function () {

  // load the service's module
  beforeEach(module('verity'));

  // instantiate service
  var myService;
  beforeEach(inject(function (_myService_) {
    myService = _myService_;
  }));

  it('should do something', function () {
    expect(!!myService).toBe(true);
  });

});
