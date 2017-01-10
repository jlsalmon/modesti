import {Authority} from './authority';

export class User {
  username:string;
  firstName:string;
  lastName:string;
  email:string;
  authorities:Authority[];
}
