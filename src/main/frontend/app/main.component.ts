import {AuthService} from './auth/auth.service';
import {User} from './user/user';

export class MainComponent implements ng.IComponentOptions {
  public templateUrl:string = '/main.component.html';
  public controller:Function = MainController;
}

class MainController {
  public static $inject:string[] = ['$scope', '$rootScope', '$location', 'AuthService'];

  private user:User;

  constructor(private $scope:any, private $rootScope:any, private $location:any, private authService:AuthService) {
    this.user = authService.getCurrentUser();

    // When an API request returns 401 Unauthorized, angular-http-auth broadcasts
    // this event. We simply catch it and show the login modal.
    $scope.$on('event:auth-loginRequired', () => this.login());
  }

  //public isActivePage(page:) {
  //  return this.$location.path().lastIndexOf(page, 0) === 0;
  //}

  public isAuthenticated() {
    return this.authService.isCurrentUserAuthenticated();
  }

  public login() {
    this.authService.login().then((user:any) => this.user = user);
  }

  public logout() {
    this.authService.logout().then(() => this.$location.path('/'));
  }
}
