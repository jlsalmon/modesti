import {AuthService} from '../auth/auth.service';
import {User} from './user';
import IComponentOptions = angular.IComponentOptions;
import IStateParamsService = angular.ui.IStateParamsService;
import IRootScopeService = angular.IRootScopeService;

export class UserComponent implements IComponentOptions {
  public templateUrl: string = '/user/user.component.html';
  public controller: Function = UserController;
}

class UserController {
  public static $inject: string[] = ['$stateParams', '$rootScope', 'AuthService'];

  public user: User;

  constructor(private $stateParams: IStateParamsService, private $rootScope: IRootScopeService,
              private authService: AuthService) {}

  public $onInit(): void {
    this.authService.getUser(this.$stateParams.id).then((user: User) => this.user = user);
  }
}
