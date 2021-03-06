import {Point} from '../request/point/point';
import IHttpService = angular.IHttpService;
import IQService = angular.IQService;
import IPromise = angular.IPromise;
import IDeferred = angular.IDeferred;

export class SearchService {
  public static $inject: string[] = ['$http', '$q'];

  constructor(private $http: IHttpService, private $q: IQService) {}

  public getPoints(domain: string, primary: string, query: string, page: any, sort: any): IPromise<Point[]> {
    let q: IDeferred<Point[]> = this.$q.defer();
    page.number = page.number || 0;
    page.size = page.size || 15;

    if (typeof(sort) != 'undefined' && sort.length > 0 && sort.indexOf(primary) == -1) {
      sort = [sort, primary + ',asc'];
    }

    this.$http.get('/api/points/search',
    {
      params: {
        domain: domain,
        query: query,
        page: page.number,
        size: page.size,
        sort: sort
      }
    }).then((response: any) => {
      q.resolve(response.data);
    },

    (error: any) => {
      console.log('error: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }
}
