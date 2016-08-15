import {AuthService} from '../auth/auth.service';
import {User} from './user';

export class UserComponent implements ng.IComponentOptions {
  public templateUrl:string = '/user/user.component.html';
  public controller:Function = UserController;
}

class UserController {
  public static $inject:string[] = ['$stateParams', 'AuthService'];

  public user:User;

  constructor(private $stateParams:any, private authService:AuthService) {}

  public $onInit() {
    this.authService.getUser(this.$stateParams.id).then((user) => this.user = user);
  }
}
