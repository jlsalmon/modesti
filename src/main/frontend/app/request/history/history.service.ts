import {Change} from './change';
import IQService = angular.IQService;
import IPromise = angular.IPromise;
import IDeferred = angular.IDeferred;

export class HistoryService {
  public static $inject: string[] = ['$q', 'Restangular'];

  public constructor(private $q: IQService, private restangular: any) {}

  public getHistory(requestId: string): IPromise<Change[]> {
    let q: IDeferred<Change[]> = this.$q.defer();

    this.restangular.one('requests/' + requestId + '/history').get().then((response: any) => {
      console.log('fetched history for request ' + requestId);

      let history: Change[] = response.data._embedded.historicEvents;
      q.resolve(history);
    },

    (error: any) => {
      console.log('error querying history for request ' + requestId + ': ' + error.data.message);
      q.reject(error);
    });

    return q.promise;
  }
}
