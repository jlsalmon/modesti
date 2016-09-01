import {Alert} from './alert.ts';

export class AlertService {
  public static $inject: string[] = ['$rootScope', '$timeout'];

  public constructor(private $rootScope: any, private $timeout: any) {
    // Create an array of globally available alerts
    $rootScope.alerts = [];
  }

  public add(type: string, message: string, timeout: number = 20000): void {
    let alert: Alert = new Alert(type, message, this);

    this.$rootScope.alerts.push(alert);
    this.$timeout(() => alert.close(), timeout);
  }

  public closeAlert(alert: Alert): Alert {
    return this.closeAlertByIndex(this.$rootScope.alerts.indexOf(alert));
  }

  public closeAlertByIndex(index: number): Alert {
    return this.$rootScope.alerts.splice(index, 1);
  }

  public clear(): void {
    this.$rootScope.alerts.forEach((alert: Alert) => alert.close());
  }
}
