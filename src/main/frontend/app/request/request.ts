import {Point} from './point/point';

export class Request implements ISerializable<Request> {

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
  public _links: any;

  public constructor();

  public constructor(type?: string, description?: string, creator?: string) {
    this.type = type;
    this.description = description;
    this.creator = creator;
  }

  public deserialize(json: any): Request {
    this.requestId = json.requestId;
    this.status = json.status;
    this.type = json.type;
    this.description = json.description;
    this.domain = json.domain;
    this.creator = json.creator;
    this.assignee = json.assignee;
    this.valid = json.valid;
    this.comments = json.comments;
    this.properties = json.properties;
    this.childRequestIds = json.childRequestIds;
    this._links = json._links;

    this.points = [];
    json.points.forEach((point: Point) => {
      this.points.push(new Point().deserialize(point));
    });

    return this;
  }
}
