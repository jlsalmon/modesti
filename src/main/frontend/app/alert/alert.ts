import {AlertService} from "./alert.service";

export class Alert {

  constructor(private type:string, private message:string, private alertService:AlertService) {}

  public close() {
    return this.alertService.closeAlert(this);
  }
}
