import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { XtractdocumentformComponent } from './xtractdocumentform.component';

describe('XtractdocumentformComponent', () => {
  let component: XtractdocumentformComponent;
  let fixture: ComponentFixture<XtractdocumentformComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ XtractdocumentformComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(XtractdocumentformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
