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

@Component({
  selector: 'app-xtractdocumentform',
  templateUrl: './xtractdocumentform.component.html',
  styleUrls: ['./xtractdocumentform.component.css']
})
export class XtractdocumentformComponent implements OnInit {

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
  //progress bar
  IsWait:boolean = false;
  //next button
  disableNext:boolean = false;
  //template output from server
  templates: Template;

  ngOnInit() {}

  constructor(private dialogRef: MatDialogRef<XtractdocumentformComponent>,private dataService: DataService,private snackBar: MatSnackBar,private dialog: MatDialog) { }

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
      this.disableNext = true;
      //call server to perform preprocessing and generate template
      this.dataService.processTemplate(this.fileToUpload,cropEnabled).subscribe((templates:Template)=>{
            //disable progress bar
            this.IsWait = false;
            this.disableNext = false;
            //save templates
            this.templates = templates;
            console.log("Templates returned: ",this.templates);
            this.dialogRef.close(this.templates);
      },
      err => {
          console.log(err)
          this.disableNext = false;
          this.IsWait = true;
          //display error message
          this.openSnackBar("Failed: "+err.message,"close")
      });
  }

  //display scakbar with message
  openSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, {
       duration: 2000,
    });
  }

}
