import {Field} from "./Field"

export interface  Page {
  id:string;
  width:number;
  height:number;
  content:string;
  fields:Field[];
}
