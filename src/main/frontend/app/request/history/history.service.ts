export class HistoryService {
  public static $inject:string[] = ['$q', 'Restangular'];

  public constructor(private $q:any, private Restangular:any) {}

  public getHistory(requestId) {
    var q = this.$q.defer();

    this.Restangular.one('requests/' + requestId + '/history').get().then((response) => {
      console.log('fetched history for request ' + requestId);

      var history = response.data._embedded.historicEvents;
      q.resolve(history);
    },

    (error) => {
      console.log('error querying history for request ' + requestId + ': ' + error.data.message);
      q.reject(error);
    });

    return q.promise;
  }
}
