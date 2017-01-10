import {RequestService} from '../request.service';
import {AuthService} from '../../auth/auth.service';
import {Request} from '../request';

export class RequestCommentsModalController {
  public static $inject: string[] = ['$uibModalInstance', 'request', 'RequestService', 'AuthService'];

  public text: string = '';

  constructor(private $modalInstance: any, private request: Request, private requestService: RequestService,
              private authService: AuthService) {}

  public addComment(): void {
    if (this.text.length) {
      let comment: any = {
        text: this.text,
        user: this.authService.getCurrentUser().username,
        timestamp: Date.now()
      };

      this.request.comments.push(comment);

      // Save the request
      this.requestService.saveRequest(this.request).then(() => {
        this.text = '';
      });
    }
  }

  public ok(): void {
    this.$modalInstance.close();
  }
}
