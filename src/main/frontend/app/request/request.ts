import {Point} from './point/point';

export class Request {

  public requestId: string;

  public status: string;

  public type: string;

  public description: string;

  public domain: string;

  public creator: string;

  public assignee: string;

  public valid: boolean;

  public points: Point[];

  public comments: any[];

  public properties: any;

  public childRequestIds: string[];
}
