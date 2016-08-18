import {AuthService} from '../auth/auth.service';
import {User} from './user';

export class UserComponent implements ng.IComponentOptions {
  public templateUrl: string = '/user/user.component.html';
  public controller: Function = UserController;
}

class UserController {
  public static $inject: string[] = ['$stateParams', '$rootScope', 'AuthService'];

  public user: User;

  constructor(private $stateParams: any, private $rootScope: any, private authService: AuthService) {}

  public $onInit(): void {
    this.authService.getUser(this.$stateParams.id).then((user: User) => this.user = user);
  }
}
