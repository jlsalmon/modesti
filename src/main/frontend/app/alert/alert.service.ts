import {Alert} from './alert.ts';

export class AlertService {
  public static $inject:string[] = ['$rootScope', '$timeout'];

  public constructor(private $rootScope:any, private $timeout:any) {
    // Create an array of globally available alerts
    $rootScope.alerts = [];
  }

  public add(type:string, message:string, timeout:number = 10000) {
    var alert:Alert = new Alert(type, message, this);

    this.$rootScope.alerts.push(alert);
    this.$timeout(() => alert.close(), timeout);
  }

  public closeAlert(alert:Alert) {
    return this.closeAlertByIndex(this.$rootScope.alerts.indexOf(alert));
  }

  public closeAlertByIndex(index:number) {
    return this.$rootScope.alerts.splice(index, 1);
  }

  public clear() {
    this.$rootScope.alerts.forEach((alert:Alert) => alert.close());
  }
}
