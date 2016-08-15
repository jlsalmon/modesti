export class AlertService {
  public static $inject:string[] = ['$rootScope', '$timeout'];

  public constructor(private $rootScope:any, private $timeout:any) {
    // Create an array of globally available alerts
    $rootScope.alerts = [];
  }

  public add(type:string, message:string, timeout:number = 10000) {
    //timeout = typeof timeout !== 'undefined' ? timeout : 10000;

    var alert = {
      'type': type,
      'message': message,
      close: () => this.closeAlert(this)
    };

    this.$rootScope.alerts.push(alert);
    this.$timeout(() => alert.close(), timeout);
  }

  public closeAlert(alert) {
    return this.closeAlertByIndex(this.$rootScope.alerts.indexOf(alert));
  }

  public closeAlertByIndex(index) {
    return this.$rootScope.alerts.splice(index, 1);
  }

  public clear() {
    this.$rootScope.alerts.forEach((alert) => alert.close());
  }
}
