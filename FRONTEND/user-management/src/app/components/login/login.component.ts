import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router'; // Importar el Router
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalComponent } from '../modal/modal.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router, // Inyectar el Router
    private modalService: NgbModal
  ) {}

  onSubmit(): void {
    const { email, password } = this.loginForm.value;
    this.authService.login(email!, password!).subscribe({
      next: (response: { token: any; }) => {
        this.authService.saveToken(response.token);
        this.showModal('Login Exitoso', 'Has iniciado sesión correctamente.', false);
        this.modalService.dismissAll(); // Cerrar el modal antes de redirigir
        this.router.navigate(['/users']); // Redirigir a User Management
      },
      error: (err: { message: string; }) => {
        this.showModal('Error de Autenticación', 'No se pudo iniciar sesión. ' + err.message, true);
      },
    });
  }

  private showModal(title: string, message: string, isError: boolean): void {
    const modalRef = this.modalService.open(ModalComponent);
    modalRef.componentInstance.title = title;
    modalRef.componentInstance.message = message;
    modalRef.componentInstance.isError = isError;
  }
}
