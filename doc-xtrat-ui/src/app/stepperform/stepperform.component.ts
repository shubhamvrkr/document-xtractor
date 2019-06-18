import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {DataService} from '../data/data.service';
import {MatSnackBar} from "@angular/material";
import {Template} from '../Template';
import {Response} from '@angular/http'
import {Page} from '../Page';
import {Field} from '../Field';
import {Barcode} from '../Barcode';
import {Boundary} from '../Boundary';
import {MatDialog,MatDialogRef} from '@angular/material';
import {ViewselectedfieldsComponent} from '../viewselectedfields/viewselectedfields.component';
import {AddfieldComponent} from '../addfield/addfield.component';
import {ImageViewerConfig, CustomEvent} from 'ngx-image-viewer';
import {DomSanitizer} from '@angular/platform-browser';
import * as $ from 'jquery';

@Component({
  selector: 'app-stepperform',
  templateUrl: './stepperform.component.html',
  styleUrls: ['./stepperform.component.css']
})


export class StepperformComponent implements OnInit {

    //input source file
    pdfSrc: any;
    //page number of pdf file
    page: number = 0;
    //total number of pages in input pdf
    totalPages: number;
    //read input pdf file
    reader: FileReader;
    //input pdf file to upload
    fileToUpload: File;
    //disable next button
    nextDisabled:boolean = false;
    //multipage input form
    //form index
    stepperIndex: number = 0;
    //# - Form 1 variable declaration
    //progress bar
    IsWait:boolean = false;
    //# - Form 2 variable declaration
    //images for displaying on canvas
    images:string[] = [];
    //pages to add fields
    pages:Page[] = [];
    //current shown image on canvas
    imageIndex:number = -1;
    //canvas to view image and draw rectangle
    canvas:any;
    //canvas 2d context
    context:any;
    //div element to hold canvas
    holder:any;
    //is mouse dragged
    drag:boolean;
    //maintain cordinates
    x1:number;y1:number;x2:number;y2:number;x:number;y:number;

    //template output from server
    templates: Template;

    ngOnInit() {
      this.totalPages = 0
      this.templates = {pageCount:0,docCode:"",barcode:{pageId:"",boundary:{x:0,y:0,width:0,height:0}},pages:new Map<string,Page>()};
    }

    //constructor
    constructor(private sanitizer:DomSanitizer, private dialogRef: MatDialogRef<StepperformComponent>,private dataService: DataService,private snackBar: MatSnackBar,private dialog: MatDialog) {

      // this.dataService.createTemplate(this.fileToUpload,true).subscribe((templates:Template)=>{
      //       this.templates = templates;
      //       console.log("Templates returned: ",this.templates);
      //       this.buildView(this.templates)
      //     //  this.stepperIndex=1;
      // });

    }

    //called from html when user selects a pdf to upload
    afterLoadComplete(pdfData: any) {
      this.page = 1;
      this.totalPages = pdfData.numPages;
    }

    //page selection navigator on form 1
    nextPage() {
      this.page++;
    }

    //page selection navigator on form 1
    prevPage() {
      this.page--;
    }

    //on upload button click
    onClick() {

       //read pdf input file
       this.reader = new FileReader();
       const fileUpload = document.getElementById('fileUpload') as HTMLInputElement;
       fileUpload.click();
       fileUpload.onchange = () => {
         //set file to upload
          this.fileToUpload = fileUpload.files[0];
          this.reader.readAsArrayBuffer(this.fileToUpload);
          console.log(this.fileToUpload);
       };
       //set pdf source to display
       this.reader.onloadend = (e: any) => {
          this.pdfSrc = e.target.result;
       };
    }

    //call server to generate template and move to form 2 (field selection)
    validateFormAndMoveNext(cropEnabled:boolean){

        console.log("File to upload: ",this.fileToUpload);
        console.log("Smart crop flag: ",cropEnabled);
        if(this.fileToUpload===undefined){
          //no file selected
          this.openSnackBar("Kindly provide PDF template document!!","close")
          return
        }
        if(cropEnabled===undefined){
          //default flag for crop if not set
          cropEnabled = false;
        }
        //show progress bar
        this.IsWait = true;
        this.nextDisabled = true;
        //call server to perform preprocessing and generate template
        this.dataService.createTemplate(this.fileToUpload,cropEnabled).subscribe((templates:Template)=>{
              //disable progress bar
              this.IsWait = false;
              this.nextDisabled = false;
              //save templates
              this.templates = templates;
              console.log("Templates returned: ",this.templates);
              //perform setup for next form
              this.buildView(this.templates)
              //move to next form
              this.stepperIndex = 1;
        },
        err => {
            console.log(err)
            this.nextDisabled = false;
            this.IsWait = false;
            //display error message
            this.openSnackBar("Failed: "+err.message,"close")
        });
    }

    // set up initial configuration for field selection
    buildView(template:Template){
      // reset image array, pages,current image index, clear cordinates
      this.images = [];
      this.pages = [];
      this.imageIndex = -1;
      this.drag = false;
      this.clearCordinates()

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
      //set canvas and context
      this.canvas = document.getElementById("myCanvas");
      this.holder = document.getElementById("image-holder");
      this.context = this.canvas.getContext("2d");
      //load first page of pdf in canvas
      this.loadImageToCanvas(0)
    }

    //show all selected fields
    viewSelectedList(){
      //open model to show selected fields
      let dialogRef = this.dialog.open(ViewselectedfieldsComponent, {
          width:  '60vw',
          height: '95%',
          disableClose:true,
          autoFocus:true,
          data: this.pages
      });
      //get updated result from selected view such as delete or edit (TODO)
      dialogRef.afterClosed().subscribe(result => {
          //TODO - handle deletion of some fields from model
          if(result===undefined){
            console.log("Selected fields modal closed")
          }else{
            console.log("Selected Fields: ",result);
          }
      });
    }

    //mouse clicked
    onMouseDown(event){
      //save the starting position of the mouse
      this.x1 = event.pageX - this.canvas.getBoundingClientRect().left;
      this.y1 = event.pageY - this.canvas.getBoundingClientRect().top;
      this.drag=true;
    }

    //mouse moved on canvas
    onMouseMove(event){
      //save current position of the mouse
      this.x = event.pageX - this.canvas.getBoundingClientRect().left;
      this.y = event.pageY - this.canvas.getBoundingClientRect().top;
      if (this.drag){
          //draw rectangle -- needs perfection for rectangle boundary (TODO)
          this.context.beginPath();
          var boundary = this.calculateFinalBoundary(this.imageIndex,this.x1,this.y1,this.x,this.y,this.canvas.width,this.canvas.height)
          this.context.rect(boundary.x,boundary.y,boundary.width,boundary.height);
          this.context.strokeStyle = 'red';
          this.context.lineWidth = 5;
          this.context.stroke();
      }
    }

    //mouse released on canvas
    onMouseUp(event){
      //disable drag flag
      this.drag = false;
      //reload image to remove the selected canvas
      this.loadImageToCanvas(this.imageIndex)
      //save final position of the mouse
      this.x2 = event.pageX - this.canvas.getBoundingClientRect().left;
      this.y2 = event.pageY - this.canvas.getBoundingClientRect().top;
      console.log("Start Point: "+this.x1+" "+this.y1);
      console.log("End Point: "+this.x2+" "+this.y2);
      console.log("Image Width, Height : "+this.canvas.width+", "+this.canvas.height);
      this.loadAddFieldDialog(this.imageIndex,this.x1,this.y1,this.x2,this.y2,this.canvas.width,this.canvas.height);
    }

    //mouse leaves canvas element during drag then reset cordinate
    onMouseLeave(event){
      if(this.drag){
        this.drag=false;
        this.loadImageToCanvas(this.imageIndex);
        this.clearCordinates();
      }
    }

    //open dialog for adding field
    loadAddFieldDialog(imageIndex:number,x1:number,y1:number,x2:number,y2:number,width:number,height:number){
      var self = this;
      //open dialof
      let dialogRef = this.dialog.open(AddfieldComponent, {
        disableClose:true,
        autoFocus:true
      });
      //add field dialog is closed
      dialogRef.afterClosed().subscribe(result => {
          //TODO - handle deletion of some fields from model
          if(result===undefined){
            console.log("Add Field Dialog Closed")
          }else{
            var field = result;
            var boundary = self.calculateFinalBoundary(imageIndex,x1,y1,x2,y2,width,height)
            console.log("Added field: ",field);
            console.log("Field Boundary: ",boundary);
            //if barcode add to template than page
            if(field.type==="BARCODE"){
                //add barcode boundary
                let barcode:Barcode = this.templates.barcode
                barcode.boundary = boundary;
                //add barcode to template
                this.templates.barcode = barcode;
            }else{
              //add boundary to field
              field.boundary = boundary
              //add field to respective page
              self.addFieldToPage(field,imageIndex)
              this.openSnackBar("Added field successfully","close")
            }
          }
          this.clearCordinates()
      });
    }

    //save template on server
    saveTemplate(){
      //check if mandatory fields are provided (i.e barcode and boundary)
      if(this.templates.docCode.length<=0 || this.templates.barcode===null|| this.templates.barcode===undefined||
        this.templates.barcode.boundary.width==0||this.templates.barcode.boundary.height==0){
          this.openSnackBar("Barcode details might not be processed during initial processing. \nKindly provide barcode value and its boundary manually", "ok")
      }else{
        //get template pages
        let pgs = this.templates.pages;
        for(let page of this.pages){
          console.log("Updated Page: ",page)
          //update page
          pgs[page.id]=page;
        }
        this.templates.pages = pgs;
        console.log("Template to send server: ",this.templates);
        //save template to server
        this.dataService.saveTemplate(this.templates).subscribe((response:Response) => {
              this.openSnackBar(response.text(),"ok")
              this.dialogRef.close()

        },(err) => {
            this.openSnackBar("Failed: "+err.message,"close")
        });
      }
    }

    //cordinates scale conversion
    calculateFinalBoundary(imageIndex:number,x1:number,y1:number,x2:number,y2:number,width:number,height:number){
      //final points declaration
      let nx1:number= 0 ;let ny1:number= 0;let nx2:number = 0; let ny2:number = 0;
      console.log("Selected field on page index: ",imageIndex);
      //get original width and height of the page
      let page:Page = this.pages[imageIndex];
      let originalWidth:number = page.width;
      let originalHeight:number = page.height;
      console.log("Page dimension: ",originalWidth,originalHeight);
      console.log("Passed cordinates: ",x1,y1,x2,y2);
      let x1p:number = x1 / width;
      let x2p:number = x2 / width;
      let y1p:number = y1 / height;
      let y2p:number = y2 / height;
      console.log("Ratio: ",x1p,y1p,x2p,y2p);
      nx1 = x1p * originalWidth;
      nx2 = x2p * originalWidth;
      ny1 = y1p * originalHeight;
      ny2 = y2p * originalHeight;
      let resp:Boundary = {x:0,y:0,width:0,height:0};
      //round off
      resp.x = Math.ceil(nx1)
      resp.y = Math.ceil(ny1)
      resp.width = Math.ceil(this.getDifference(nx1,nx2))
      resp.height = Math.ceil(this.getDifference(ny1,ny2));
      return resp
    }

    //add field to respective page
    addFieldToPage(field:Field,imageIndex:number){
      let page:Page;
      page =  this.pages[imageIndex];
      if(page.fields==null){
        page.fields = []
        page.fields.push(field);
      }else{
        page.fields.push(field);
      }
      this.pages[imageIndex] = page;
    }

    //find width and height
    getDifference(num1:number,num2:number){
      if (num1<num2){
        return num2-num1;
      }else{
        return num2-num1;
      }
    }

    //display scakbar with message
    openSnackBar(message: string, action: string) {
      this.snackBar.open(message, action, {
         duration: 2000,
      });
    }

    //reset cordinates to origin
    clearCordinates(){
      this.x1 = 0;
      this.y1 = 0;
      this.x =0;
      this.y = 0;
      this.x2 = 0;
      this.y2 = 0;
    }

    //load selected image to canves
    loadImageToCanvas(index: number){
      console.log("loading image at index "+index+" to canvas")
      this.imageIndex=index;
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
}
