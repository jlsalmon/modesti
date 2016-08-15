import {RequestService} from '../request.service';
import {AuthService} from '../../auth/auth.service';

export class RequestCommentsModalController {
  public static $inject:string[] = ['$uibModalInstance', 'request', 'RequestService', 'AuthService'];

  public text:string = '';

  constructor(private $modalInstance:any, private request:any, private requestService:RequestService, private authService:AuthService) {}

  public addComment() {
    if (this.text.length) {
      var comment = {
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

  public ok() {
    this.$modalInstance.close();
  }
}
