import {NgModule} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';

import {
  MatSidenavModule,
  MatToolbarModule,
  MatIconModule,
  MatListModule,
  MatDialogModule,
  MatStepperModule,
  MatInputModule,
  MatSnackBarModule,
  MatSelectModule,
} from '@angular/material';

@NgModule({
  imports: [
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatListModule,
    MatButtonModule,
    MatTooltipModule,
    MatDialogModule,
    MatStepperModule,
    MatInputModule,
    MatProgressBarModule,
    MatCardModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatSelectModule,
  ],
  exports: [
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatListModule,
    MatButtonModule,
    MatTooltipModule,
    MatDialogModule,
    MatStepperModule,
    MatInputModule,
    MatProgressBarModule,
    MatCardModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatSelectModule,
  ]
})
export class MaterialModule {}
