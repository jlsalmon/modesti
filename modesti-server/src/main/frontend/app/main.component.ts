import {AuthService} from './auth/auth.service';
import {User} from './user/user';
import IComponentOptions = angular.IComponentOptions;
import IScope = angular.IScope;
import IRootScopeService = angular.IRootScopeService;
import ILocationService = angular.ILocationService;

export class MainComponent implements IComponentOptions {
  public templateUrl: string = '/main.component.html';
  public controller: Function = MainController;
}

class MainController {
  public static $inject: string[] = ['$scope', '$rootScope', '$location', 'AuthService'];

  private user: User;

  constructor(private $scope: IScope, private $rootScope: IRootScopeService, private $location: ILocationService,
              private authService: AuthService) {
    this.user = authService.getCurrentUser();

    // When an API request returns 401 Unauthorized, angular-http-auth broadcasts
    // this event. We simply catch it and show the login modal.
    $scope.$on('event:auth-loginRequired', () => this.login());
  }

  // public isActivePage(page:) {
  //  return this.$location.path().lastIndexOf(page, 0) === 0;
  // }

  public isAuthenticated(): boolean {
    return this.authService.isCurrentUserAuthenticated();
  }

  public login(): void {
    this.authService.login().then((user:any) => this.user = user);
  }

  public logout(): void {
    this.authService.logout().then(() => this.$location.path('/'));
  }
}
