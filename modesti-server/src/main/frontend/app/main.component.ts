import {AuthService} from './auth/auth.service';
import {User} from './user/user';
import IComponentOptions = angular.IComponentOptions;
import IScope = angular.IScope;
import IPromise = angular.IPromise;
import IRootScopeService = angular.IRootScopeService;
import ILocationService = angular.ILocationService;
import IStateService = angular.ui.IStateService;

export class MainComponent implements IComponentOptions {
  public templateUrl: string = '/main.component.html';
  public controller: Function = MainController;
}

class MainController {
  public static $inject: string[] = ['$scope', '$rootScope', '$location', 'AuthService', 'httpBuffer', '$state', '$window'];

  private user: User;

  constructor(private $scope: IScope, private $rootScope: IRootScopeService, private $location: ILocationService,
              private authService: AuthService, private httpBuffer: any, private $state: IStateService, private $window: any) {

    let userPromise: IPromise<User> = this.authService.login();
    userPromise.then((response: any) => {
      this.user = authService.getCurrentUser();
    });

    // When an API request returns 401 Unauthorized, angular-http-auth broadcasts
    // this event. We simply catch it and show the login modal.
    this.$scope.$on('event:auth-loginRequired', () => this.login());
  }

  public isAuthenticated(): boolean {
    return this.authService.isCurrentUserAuthenticated();
  }

  public login(): void {
    this.httpBuffer.rejectAll("ClearBuffer");
    this.authService.login().then((user:any) => {
      this.user = user;
      if (this.user !== undefined) {
        // Force a page reload since the httpBuffer was cleaned
        this.$window.location.href = this.$window.location.href;
      }
    });
  }

  public logout(): void {
    this.authService.logout().then(() => this.$location.path('/'));
  }
}
