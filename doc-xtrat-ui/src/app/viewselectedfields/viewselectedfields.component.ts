import { Component, OnInit } from '@angular/core';
import {MAT_DIALOG_DATA,MatDialogRef} from '@angular/material';
import { Inject } from '@angular/core';
import {Page} from '../Page';

@Component({
  selector: 'app-viewselectedfields',
  templateUrl: './viewselectedfields.component.html',
  styleUrls: ['./viewselectedfields.component.css']
})
export class ViewselectedfieldsComponent implements OnInit {

  //pages containing fields to display
  pages:Page[] = [];
  
  constructor(@Inject(MAT_DIALOG_DATA) public data: Page[],private dialogRef: MatDialogRef<ViewselectedfieldsComponent>) {
    this.pages=data;
  }
  //initialization
  ngOnInit() {
    console.log("Pages to obtain fields from: ",this.pages)
  }
  //send updated pages back to parent
  save() {
       this.dialogRef.close(this.pages);
  }
  //close this dialog without making update
  close() {
     this.dialogRef.close();
  }
}
