import {AuthService} from './auth.service';

export class LoginModalController {
  public static $inject: string[] = ['$uibModalInstance', 'AuthService'];

  public credentials: any = {};
  public loggingIn: string = undefined;
  public loginError: boolean = undefined;

  constructor(private $modalInstance: any, private authService:AuthService) {}

  public login(): void {
    this.loggingIn = 'started';

    this.authService.doLogin(this.credentials).then(() => {
      this.loginError = false;
      this.loggingIn = 'success';

      // Close the modal
      this.$modalInstance.close();
    },

    () => {
      this.loginError = true;
      this.loggingIn = 'error';
    });
  }
}
