export class ValidationService {
  public static $inject:string[] = ['$q', '$http'];

  public constructor(private $q:any, private $http:any) {}

  public validateRequest(request) {
    var q = this.$q.defer();

    this.$http.post('/api/requests/' + request.requestId + '/validate').then((response) => {
        request = response.data;
        q.resolve(request);
      },

      (error) => {
        console.log('error validating request: ' + error.statusText);
        q.reject(error);
      });

    return q.promise;
  }

  /**
   * Set an error message on a single field of a point.
   *
   * @param point
   * @param propertyName
   * @param message
   */
  public setErrorMessage(point, propertyName, message) {
    var exists = false;

    point.errors.forEach((error) => {
      if (error.property === propertyName) {
        exists = true;
        if (!error.errors.indexOf(message > -1)) {
          error.errors.push(message);
        }
      }
    });

    if (!exists) {
      point.errors.push({property: propertyName, errors: [message]});
    }
  }
}
