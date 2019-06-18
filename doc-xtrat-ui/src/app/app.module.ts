import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MaterialModule} from './material.module';
import { AppComponent } from './app.component';
import {AppRouters} from './app.routes';
import { WelcomeComponent } from './welcome/welcome.component';
import {DataService} from './data/data.service';
import { StepperformComponent } from './stepperform/stepperform.component'
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { HttpModule }    from '@angular/http';
import { FlexLayoutModule } from '@angular/flex-layout';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { ImageViewerModule } from "ngx-image-viewer";
import { ViewselectedfieldsComponent } from './viewselectedfields/viewselectedfields.component';
import * as $ from 'jquery';
import { AddfieldComponent } from './addfield/addfield.component';
import { XtractdocumentformComponent } from './xtractdocumentform/xtractdocumentform.component';

@NgModule({
  declarations: [
    AppComponent,
    WelcomeComponent,
    StepperformComponent,
    ViewselectedfieldsComponent,
    AddfieldComponent,
    XtractdocumentformComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MaterialModule,
    AppRouters,
    FormsModule,
    FlexLayoutModule,
    ReactiveFormsModule,
    HttpModule,
    PdfViewerModule,
    ImageViewerModule.forRoot({
      btnClass: 'default', // The CSS class(es) that will apply to the buttons
      zoomFactor: 0,
      wheelZoom: false,
      allowFullscreen: false, // If true, the fullscreen button will be shown, allowing the user to entr fullscreen mode
      allowKeyboardNavigation: false, // If true, the left / right arrow keys can be used for navigation
      btnIcons: { // The icon classes that will apply to the buttons. By default, font-awesome is used.
        next: 'fa fa-arrow-right',
        prev: 'fa fa-arrow-left',
        fullscreen: 'fa fa-arrows-alt',
      },
      btnShow: {
        zoomIn: false,
        zoomOut: false,
        rotateClockwise: false,
        rotateCounterClockwise: false,
        next: true,
        prev: true
      }
}),
  //add more modules
  ],
  providers: [DataService],
  bootstrap: [AppComponent],
  entryComponents: [StepperformComponent,
    ViewselectedfieldsComponent,
    AddfieldComponent,
    XtractdocumentformComponent],
})
export class AppModule { }
