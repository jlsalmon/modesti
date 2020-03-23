import {User} from '../user/user';
import {Authority} from '../user/authority';
import IDeferred = angular.IDeferred;
import IPromise = angular.IPromise;
import IHttpService = angular.IHttpService;
import IQService = angular.IQService;
import IStateService = angular.ui.IStateService;

export class AuthService {
  public static $inject: string[] = ['$http', '$q', '$localStorage', '$cookies', '$uibModal', '$state', 'authService'];

  public loginModalOpened: boolean = false;

  public constructor(private $http: IHttpService, private $q: IQService, private $localStorage: any,
                     private $cookies: any, private $modal: any, private $state: IStateService,
                     private authService: any) {}

  public login(): IPromise<User> {
    let q: IDeferred<User> = this.$q.defer();
      
    this.$http.get('/api/user').then((response: any) => {
      if (response.data.idToken !== undefined ) {
        this.$localStorage.user = this.getAuthenticatedUser(response.data);    
        q.resolve(this.$localStorage.user);
      } else {
        // The user is not authenticated
        window.location.href = '/api/sso?callback=' + encodeURIComponent(document.URL); 
      };
    },
    (error: any) => {
      console.log("Error in call to '/api/user'", error);
      window.location.href = '/api/sso?callback=' + encodeURIComponent(document.URL); 
    });

    return q.promise;
  }

  private getAuthenticatedUser(principal: any) : User {
    let user: User = new User();
    user.username = principal.name;
    user.firstName=principal.givenName;
    user.lastName=principal.familyName;
    user.email=principal.email;
    user.authorities=principal.attributes.cern_roles.map(element => {
      return new Authority(element);
    });
    return user;
  }

  public logout(): IPromise<{}> {
    let q: IDeferred<{}> = this.$q.defer();

    this.$http.get('/logout').then(() => {
      this.$localStorage.user = undefined;
      this.$cookies.remove('JSESSIONID');
      delete this.$cookies.JSESSIONID;
      q.resolve();
    },

    (error: any) => {
      console.log('failed to log out');
      this.$localStorage.user = undefined;
      q.reject(error);
    });

    return q.promise;
  }

  public getCurrentUser(): User {
    return this.$localStorage.user;
  }

  public isCurrentUserAuthenticated(): boolean {
    return this.$localStorage.user != null;
  }

  public isCurrentUserAdministrator(): boolean {
    if (!this.isCurrentUserAuthenticated()) {
      return false;
    }

    for (let i: number = 0, len: number = this.$localStorage.user.authorities.length; i < len; i++) {
      let authority: Authority = this.$localStorage.user.authorities[i];

      if (authority.authority === 'modesti-administrators') {
        return true;
      }
    }

    return false;
  }

  public getUser(username: string): IPromise<User> {
    let q: IDeferred<User> = this.$q.defer();

    this.$http.get('/api/users/search/findOneByUsername', {params: {username: username}}).then((response: any) => {
      q.resolve(response.data);
    },

    (error: any) => {
      console.log('failed to get user ' + username);
      q.reject(error);
    });

    return q.promise;
  }
}
