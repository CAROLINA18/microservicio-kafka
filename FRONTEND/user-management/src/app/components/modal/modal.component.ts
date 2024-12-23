import { NgIf } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [NgIf],
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css'],
})
export class ModalComponent {
  @Input() title = '';
  @Input() message = '';
  @Input() isError = false;

  @Output() confirm = new EventEmitter<void>();

  constructor(public activeModal: NgbActiveModal) {}

  onConfirm(): void {
    this.confirm.emit();
    this.activeModal.close();
  }
}
