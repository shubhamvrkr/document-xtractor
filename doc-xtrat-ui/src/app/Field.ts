import {Boundary} from "./Boundary"

export interface  Field {
  name:string;
  value:string;
  type:string;
  section:string;
  boundary:Boundary;
  format:string;
}
