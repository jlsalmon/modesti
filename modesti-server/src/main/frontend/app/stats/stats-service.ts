import {AuthService} from '../auth/auth.service';
import IHttpService = angular.IHttpService;

export class StatsService {

  public static $inject: string[] = ['AuthService', '$http'];

  constructor(private authService: AuthService, private $http: IHttpService) {}

  public recordVisit(page: string) : void {
    let username: string = this.authService.getCurrentUser().username;
    console.log("The user ", username, " has visited the page ", page);
    this.record(username, "page");
  }

  public recordLogin(username: string) : void {
    console.log("The user ", username, " is logged in.");
    this.record(username, "logged");
  }

  private record(username: string, page:string): void {
    this.$http.get('https://test-modesti-stats.web.cern.ch/Default.htm', 
      {
        params: {
            user: username, 
            page: page
        }
      });
  }
}