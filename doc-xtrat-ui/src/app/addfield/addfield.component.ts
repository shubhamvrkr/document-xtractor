import { Component, OnInit } from '@angular/core';
import {MAT_DIALOG_DATA,MatDialogRef} from '@angular/material';
import { Inject } from '@angular/core';
import {Field} from '../Field';

@Component({
  selector: 'app-addfield',
  templateUrl: './addfield.component.html',
  styleUrls: ['./addfield.component.css']
})
export class AddfieldComponent implements OnInit {

  //field details
  field:Field ={} as any;
  name:string;
  type:string;
  section:string;
  format:string;
  selected:string="TEXT";

  ngOnInit() {}

  constructor(private dialogRef: MatDialogRef<AddfieldComponent>) { }

  //add field 
  add() {
    //set details to field
    this.field.name= this.name;
    this.field.type= this.selected;
    this.field.section= this.section;
    this.field.format = this.format;
    //send field to parent
    this.dialogRef.close(this.field);
  }

  //close this dialog
  close() {
     this.dialogRef.close();
  }
}
