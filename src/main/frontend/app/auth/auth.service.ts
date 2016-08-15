export class AuthService {
  public static $inject:string[] = ['$http', '$q', '$localStorage', '$cookies', '$uibModal', '$state', 'authService'];

  public loginModalOpened:boolean = false;

  public constructor(private $http:any, private $q:any, private $localStorage:any, private $cookies:any, private $modal:any,
                     private $state:any, private authService:any) {}

  public login() {
    var q = this.$q.defer();

    if (this.loginModalOpened) {
      return this.$q.when();
    }

    this.loginModalOpened = true;

    var modalInstance = this.$modal.open({
      animation: false,
      templateUrl: '/auth/login.modal.html',
      controller: 'LoginModalController as ctrl'
    });

    modalInstance.result.then(() => {
      this.loginModalOpened = false;
      q.resolve(this.$localStorage.user);
    }, () => {
      this.loginModalOpened = false;
      this.$state.go('home');
    });

    return q.promise;
  }

  public doLogin(credentials) {
    var q = this.$q.defer();

    // Build a basic auth header
    var headers = credentials ? {
      authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password)
    } : {};

    // Set ignoreAuthModule so that angular-http-auth doesn't show another modal
    // if the authentication fails
    this.$http.get('/api/login', {headers: headers, ignoreAuthModule: true}).then((response) => {
      console.log('authenticated');

      // Set data in local storage for other parts of the app to use
      this.$localStorage.user = response.data;

      // Confirm the login, so that angular-http-auth can resume any ajax requests
      // that were suspended due to 401s
      this.authService.loginConfirmed();

      q.resolve();
    },

    (error) => {
      console.log('failed to authenticate');
      this.$localStorage.user = undefined;
      q.reject(error);
    });

    return q.promise;
  }

  public logout() {
    this.$localStorage.user = undefined;
    this.$cookies.remove('JSESSIONID');
    delete this.$cookies.JSESSIONID;
    return this.$q.when();
  }

  public getCurrentUser() {
    return this.$localStorage.user;
  }

  public isCurrentUserAuthenticated() {
    return this.$localStorage.user !== undefined;
  }

  public isCurrentUserAdministrator() {
    if (!this.isCurrentUserAuthenticated()) {
      return false;
    }

    for (var i = 0, len = this.$localStorage.user.authorities.length; i < len; i++) {
      var authority = this.$localStorage.user.authorities[i];

      if (authority.authority === 'modesti-administrators') {
        return true;
      }
    }

    return false;
  }

  public getUser(username) {
    var q = this.$q.defer();

    this.$http.get('/api/users/search/findOneByUsername', {params: {username: username}}).then((response) => {
        q.resolve(response.data);
      },

      (error) => {
        console.log('failed to get user ' + username);
        q.reject(error);
      });

    return q.promise;
  }
}
