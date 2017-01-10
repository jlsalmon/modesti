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
  public createdAt: string;
  public properties: any;
  public childRequestIds: string[];
  public _links: any;

  public constructor();

  public constructor(type?: string, description?: string, creator?: string) {
    this.type = type;
    this.description = description;
    this.creator = creator;
  }

  public deserialize(request: Request): Request {
    this.requestId = request.requestId;
    this.status = request.status;
    this.type = request.type;
    this.description = request.description;
    this.domain = request.domain;
    this.creator = request.creator;
    this.assignee = request.assignee;
    this.valid = request.valid;
    this.comments = request.comments;
    this.createdAt = request.createdAt;
    this.properties = request.properties;
    this.childRequestIds = request.childRequestIds;
    this._links = request._links;

    if (!this.points) {
      this.points = [];
      request.points.forEach((point: Point) => {
        this.points.push(new Point().deserialize(point));
      });
    } else {
      // FIXME: this is dodgy, and probably won't work when adding rows...
      this.points.forEach((point: Point, index: number) => {
        point = point.deserialize(request.points[index]);
      });
    }

    return this;
  }
}
