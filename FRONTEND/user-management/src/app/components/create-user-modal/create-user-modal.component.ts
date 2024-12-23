import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { ModalComponent } from '../modal/modal.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';


@Component({
  selector: 'app-create-user-modal',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './create-user-modal.component.html',
  styleUrls: ['./create-user-modal.component.css'],
})
export class CreateUserModalComponent {
  @Input() user: any = null; // Si es null, es un usuario nuevo; si no, es un usuario existente.
  @Output() userSaved = new EventEmitter<void>();

  userForm = this.fb.group({
    id: [null], // Se usará solo para actualizaciones
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]], // Para actualizaciones, este campo podría no ser obligatorio
    roles: ['', [Validators.required]],
  });

  roles = [
    'CONSULTA_USUARIO',
    'ACTUALIZA_USUARIO',
    'ELIMINA_USUARIO',
    'CREAR_USUARIO_BD',
    'CONSULTA_USUARIO_LDAP',
  ];

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {
    if (this.user) {
      this.userForm.patchValue(this.user); // Cargar datos en el formulario si es una actualización
    }
  }

  onSubmit(): void {
    if (this.user) {
      // Actualización
      this.userService.updateUser(this.userForm.value).subscribe({
        next: () => {
          this.showModal('Actualización Exitosa', 'El usuario ha sido actualizado correctamente.', false);
          this.userSaved.emit();
        },
        error: (err) => this.showModal('Error al Actualizar Usuario', err.message, true),
      });
    } else {
      // Creación
      this.userService.createUser(this.userForm.value).subscribe({
        next: () => {
          this.showModal('Creación Exitosa', 'El usuario ha sido creado correctamente.', false);
          this.userSaved.emit();
        },
        error: (err) => this.showModal('Error al Crear Usuario', err.message, true),
      });
    }
  }

  private showModal(title: string, message: string, isError: boolean): void {
    const modalRef = this.modalService.open(ModalComponent);
    modalRef.componentInstance.title = title;
    modalRef.componentInstance.message = message;
    modalRef.componentInstance.isError = isError;
  }
}
