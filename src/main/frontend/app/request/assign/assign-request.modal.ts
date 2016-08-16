export class AssignRequestModalController {
  public static $inject:string[] = ['$uibModalInstance', '$http', 'task'];

  public assignee:any;
  public users:any = [];

  constructor(private $modalInstance:any, private $http:any, private task:any) {}

  public ok() {
    this.$modalInstance.close(this.assignee);
  }

  public cancel() {
    this.$modalInstance.dismiss('cancel');
  }

  public refreshUsers(query:any) {
    return this.$http.get('/api//users/search', {
      params : {
        query : this.parseQuery(query)
      }
    }).then((response:any):any[] => {
      if (!response.data.hasOwnProperty('_embedded')) {
        return [];
      }

      this.users = response.data._embedded.users;
    });
  }

  public parseQuery(query:any) {
    var q = '';

    if (this.task.candidateGroups.length > 1) {
      q += 'authorities.authority =in= (' + this.task.candidateGroups.join() + ')';
    }

    if (query.length !== 0) {
      if (q.length > 0) {
        q += ' and ';
      }

      q += '(username == ' + query;
      q += ' or firstName == ' + query;
      q += ' or lastName == ' + query + ')';
    }

    console.log('parsed query: ' + query);
    return q;
  }
}
