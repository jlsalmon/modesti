import {Request} from '../request';
import {Point} from '../point/point';
import IQService = angular.IQService;
import IHttpService = angular.IHttpService;
import IPromise = angular.IPromise;
import IDeferred = angular.IDeferred;

export class ValidationService {
  public static $inject: string[] = ['$q', '$http'];

  public constructor(private $q: IQService, private $http: IHttpService) {}

  public validateRequest(request: Request): IPromise<Request> {
    let q: IDeferred<Request> = this.$q.defer();

    this.$http.post('/api/requests/' + request.requestId + '/validate', {}).then((response: any) => {
      request = request.deserialize(response.data);
      q.resolve(request);
    },

    (error: any) => {
      console.log('error validating request: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }
}
