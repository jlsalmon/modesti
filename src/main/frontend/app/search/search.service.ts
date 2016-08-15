
export class SearchService {
  public static $inject:string[] = ['$http', '$q'];

  constructor(private $http:any, private $q:any) {}

  public getPoints(domain, query, page, size, sort) {
    var q = this.$q.defer();
    page = page || 0;
    size = size || 15;
    //sort = sort || 'pointId,desc';

    this.$http.get('/api/points/search',
    {
      params: {
        domain: domain,
        query: query,
        page: page - 1,
        size: size,
        sort: sort
      }
    }).then((response) => {
      q.resolve(response.data);
    },

    (error) => {
      console.log('error: ' + error.statusText);
      q.reject(error);
    });

    return q.promise;
  }
}
