import 'rxjs/add/operator/map'
import { Injectable } from '@angular/core';
import {Template} from '../Template';
import {Observable} from 'rxjs/Rx';
import {Http,Response} from '@angular/http'
import 'rxjs/add/operator/catch';
import {delay} from 'rxjs/operators'
@Injectable()
export class DataService {
  //server base URL
  private url:string;
  //create url for creating template
  private createTemplateURI:string;
  //save template url
  private saveTemplateURI:string;
  //process template url
  private processTemplateURI:string;
  //template object
  private template: Template;
  //sets respective url
  constructor(private http: Http) {
    this.url = "http://localhost:8080";
    this.createTemplateURI="/template/create/";
    this.saveTemplateURI="/template/save";
    this.processTemplateURI="/template/process/";
  }
  //create template service
  createTemplate(file:File,cropEnabled:boolean): Observable<Template> {
      console.log("creating template")
      var formData = new FormData();
      formData.append('file', file);
      return this.http.post(this.url+this.createTemplateURI+cropEnabled, formData)
      .map((resp:Response)=> resp.json()).catch(this.handleError);
      // return this.http.get("../assets/13456477474373-template.json").map((resp:Response)=> {
      //   return resp.json();
      // }).catch(this.handleError);
  }
  //save template
  saveTemplate(template:Template): Observable<Response>{
    return this.http.post(this.url+this.saveTemplateURI, template)
            .catch(this.handleError)
  }

  processTemplate(file:File,cropEnabled:boolean) :Observable<Template>{
    console.log("processing template")
    var formData = new FormData();
    formData.append('file', file);
    return this.http.post(this.url+this.processTemplateURI+cropEnabled, formData)
    .map((resp:Response)=> resp.json()).catch(this.handleError);
    // return this.http.get("../assets/scanned-form-1-response.json").map((resp:Response)=> {
    //   return resp.json();
    // }).catch(this.handleError);
  }
  //handle error from server
  private handleError(error: any) {
    let errMsg = (error.message) ? error.message : error.status ? `${error.status} - ${error.statusText}` : 'Server error';
    return Observable.throw(error);
  }

}
