import {Point} from "../request/point/point";
import "lodash";

export class SelectedPointsService {

    private selectedPoints: Point[] = [];

    public getSelectedPoints() : Point[] {
      return this.selectedPoints;
    }

    public getNumSelectedPoints() : number {
      return this.selectedPoints.length;
    }

    public addPoint(point: Point, idProperty: string) : void {
      if (!_.some(this.selectedPoints, [idProperty, _.get(point, idProperty)])) {
        this.selectedPoints.push(point);
      }
    }

    public addPoints(points: Point[], idProperty: string) : void {
      points.forEach((point: Point) => this.addPoint(point, idProperty));
    }
    
    public deletePoint(point: Point, idProperty: string) : void {
      _.remove(this.selectedPoints, [idProperty, _.get(point, idProperty)]);
    }

    public clear() : void {
      this.selectedPoints = [];
    }
}