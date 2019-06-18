import { Component, ViewChildren, QueryList, ElementRef, AfterViewInit, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {DataService} from '../data/data.service';
import {StepperformComponent} from '../stepperform/stepperform.component';
import {MatDialog} from '@angular/material';
import { XtractdocumentformComponent } from '../xtractdocumentform/xtractdocumentform.component';
import {Template} from '../Template';
import {Page} from '../Page';
import {Field} from '../Field';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})

export class WelcomeComponent implements OnInit {

  template:Template;

  images:string[] = []

  currImageIndex:number;

  pages:Page[] = [];

  //canvas to view image and draw rectangle
  canvas:any;
  //canvas 2d context
  context:any;
  //div element to hold canvas
  holder:any;

  @ViewChildren("homeCanvas") homeCanvas: QueryList<ElementRef>;

  ngOnInit(){
    this.template = {pageCount:0,docCode:"",barcode:{pageId:"",boundary:{x:0,y:0,width:0,height:0}},pages:new Map<string,Page>()};
  }

  constructor(private dataService: DataService, private dialog: MatDialog) {
    //call server to perform preprocessing and generate template

  }

  openStepperForm(): void {
    //load processing form
    let dialogRef = this.dialog.open(StepperformComponent, {
      width: '100vw',
      height: '95%'
    });
  }

  openXtractForm():void{

    let dialogRef = this.dialog.open(XtractdocumentformComponent, {
      width: '500px',
      height: '550px'
    });
    //get updated result from selected view such as delete or edit (TODO)
    dialogRef.afterClosed().subscribe(result => {
        //TODO - handle deletion of some fields from model
        if(result===undefined){
          console.log("Templates not found")
        }else{
          console.log("template: ",result);
          this.template=result;
          this.buildView(this.template)
        }
    });
  }

  reUpload():void{
    this.template.docCode = "";
  }

  // set up initial configuration for field selection
  buildView(template:Template){
    // reset image array, pages,current image index, clear cordinates
    this.images = [];
    this.pages = [];
    //store pages in array
    for (var key in template.pages) {
      this.pages.push(template.pages[key]);
    }
    this.pages.sort(function(a, b) {
        return a.id.localeCompare(b.id);
    });
    //save images for preview
    for(var index in this.pages){
        this.images[index] = "data:image/PNG;base64,"+this.pages[index].content;
    }
  }

  //load selected image to canves
  loadImageToCanvas(index: number){
      this.currImageIndex=index;
      let self = this;
      console.log("Holder width: ",this.canvas.width)
      this.canvas.width = this.holder.offsetWidth;
      console.log("Holder width: ",this.holder.offsetWidth)
      var image = new Image();
      image.onload = function() {
          console.log("Image height: ",image.height)
          console.log("Image width: ",image.width)
          self.canvas.height = Math.ceil((image.height/image.width)*self.holder.offsetWidth);
          console.log("Computed canvas height: ",self.canvas.height)
          console.log("Scale factor: ",(self.holder.offsetWidth/image.width),(self.canvas.height/image.height))
          self.context.scale((self.holder.offsetWidth/image.width),(self.canvas.height/image.height));
          self.context.drawImage(image, 0, 0);
          self.context.strokeStyle = 'red';
          self.context.lineWidth = 10;
      };
      image.src= this.images[index];
    }

  //on select of form field, highlight area on scanned image
  highLightArea(imageIndex:number,field:Field){

    console.log("Image index: ",imageIndex);
    console.log("Field : ",field);
    this.currImageIndex=imageIndex;
    let self = this;
    console.log("Holder width: ",this.canvas.width)
    this.canvas.width = this.holder.offsetWidth;
    console.log("Holder width: ",this.holder.offsetWidth)
    var image = new Image();
    image.onload = function() {
        console.log("Image height: ",image.height)
        console.log("Image width: ",image.width)
        self.canvas.height = Math.ceil((image.height/image.width)*self.holder.offsetWidth);
        console.log("Computed canvas height: ",self.canvas.height)
        console.log("Scale factor: ",(self.holder.offsetWidth/image.width),(self.canvas.height/image.height))
        self.context.scale((self.holder.offsetWidth/image.width),(self.canvas.height/image.height));
        self.context.drawImage(image, 0, 0);
        self.context.beginPath()
        self.context.rect(field.boundary.x,field.boundary.y,field.boundary.width,field.boundary.height);
        self.context.strokeStyle = 'red';
        self.context.lineWidth = 10;
        self.context.stroke();
    };
    image.src= this.images[imageIndex];
  }

  export(){

  }

  ngAfterViewInit() {
    var self = this;
    console.log("Before changes", this.homeCanvas);
    this.homeCanvas.changes.subscribe((canvas) => {
      console.log("Canvas: ",canvas);
      //set canvas and context
      self.canvas = document.getElementById("home-canvas");
      console.log(self.canvas)
      self.holder = document.getElementById("home-image-holder");
      console.log(self.holder)
      self.context = self.canvas.getContext("2d");
      console.log(self.context)
      //load first page of pdf in canvas
      self.loadImageToCanvas(0)
    });
  }
}
