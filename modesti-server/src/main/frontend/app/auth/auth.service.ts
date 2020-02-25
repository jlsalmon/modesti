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
      if (response.data.authenticated !== undefined && response.data.authenticated === true) {
        this.$localStorage.user = response.data.principal;    
        q.resolve(this.$localStorage.user);
      } else {
        // The user is not authenticated
        this.isTnAddress().then((tnAddress: boolean) => {
          if (tnAddress) {
            this.showForm(q);
          } else {
            window.location.href = '/api/sso?callback=' + encodeURIComponent(document.URL); 
          }
        });
      };
    },
    (error: any) => {
      console.log("Error in call to '/api/user'", error);
      this.showForm(q);
    });

    return q.promise;
  }

  private isTnAddress() : IPromise<Boolean> {
    let q: IDeferred<Boolean> = this.$q.defer();

    this.$http.get('/api/is_tn_address').then((response: any) => {
      if (response.data !== undefined) {
        q.resolve(response.data);
      } else {
        q.resolve(false);
      }
    }, 
    () => {
      q.resolve(false);
    });

    return q.promise;
  }

  private showForm(q : IDeferred<User> ) : void {
    if (this.loginModalOpened) {
      return this.$q.when(undefined);
    }

    this.loginModalOpened = true;

    let modalInstance: any = this.$modal.open({
      animation: false,
      templateUrl: '/auth/login.modal.html',
      controller: 'LoginModalController as ctrl'
    });

    modalInstance.result.then(() => {
      this.loginModalOpened = false;
      q.resolve(this.$localStorage.user);
    }, () => {
      this.loginModalOpened = false;
      q.reject();
      this.authService.loginCancelled();
      this.$state.go('home');
    });
  }

  public doLogin(credentials: any): IPromise<{}> {
    let q: IDeferred<{}> = this.$q.defer();

    // Build a basic auth header
    let headers: any = credentials ? {
      authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password)
    } : {};

    // Set ignoreAuthModule so that angular-http-auth doesn't show another modal
    // if the authentication fails
    this.$http.get('/api/ldap_login', {headers: headers, params: {ignoreAuthModule: true}}).then((response: any) => {
      console.log('authenticated');

      // Set data in local storage for other parts of the app to use
      this.$localStorage.user = response.data;

      // Confirm the login, so that angular-http-auth can resume any ajax requests
      // that were suspended due to 401s
      this.authService.loginConfirmed();

      q.resolve();
    },

    (error: any) => {
      console.log('failed to authenticate');
      this.$localStorage.user = undefined;
      q.reject(error);
    });

    return q.promise;
  }

  public logout(): IPromise<{}> {
    let q: IDeferred<{}> = this.$q.defer();

    this.$http.get('/logout').then(() => {
      this.$localStorage.user = undefined;
      this.$cookies.remove('JSESSIONID');
      delete this.$cookies.JSESSIONID;
      q.resolve();
      window.location.href = 'https://login.cern.ch/adfs/ls/?wa=wsignout1.0'; 
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
