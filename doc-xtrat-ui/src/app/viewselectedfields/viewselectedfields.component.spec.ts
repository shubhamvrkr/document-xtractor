import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewselectedfieldsComponent } from './viewselectedfields.component';

describe('ViewselectedfieldsComponent', () => {
  let component: ViewselectedfieldsComponent;
  let fixture: ComponentFixture<ViewselectedfieldsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewselectedfieldsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewselectedfieldsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
