import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, NgModel, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  standalone: true,
  imports: [ReactiveFormsModule,CommonModule,FormsModule],
  styleUrls: ['./user-management.component.css'],
})
export class UserManagementComponent implements OnInit {
  users: any[] = [];
  availableRoles: string[] = [
    'CONSULTA_USUARIO',
    'ACTUALIZA_USUARIO',
    'ELIMINA_USUARIO',
    'CREAR_USUARIO_BD',
    'CONSULTA_USUARIO_LDAP',
  ];
  selectedRoles: string[] = []; // Roles seleccionados
  roleControl = new FormControl(''); // Control independiente para el select

  isModalOpen: boolean = false;
  isEditMode: boolean = false;
  modalTitle: string = '';
  userForm!: FormGroup;
  currentUserId: number | null = null;

  constructor(private userService: UserService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.loadUsers();

    // Inicializar formulario
    this.userForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: [''], // Solo requerido para creación
    });
  }

  loadUsers(): void {
    this.userService.getUsers().subscribe({
      next: (data) => (this.users = data),
      error: (err) => console.error('Error al cargar usuarios', err),
    });
  }

  deleteUser(userId: number): void {
    if (confirm('¿Estás seguro de que deseas eliminar este usuario?')) {
      this.userService.deleteUser(userId).subscribe({
        next: () => {
          alert('Usuario eliminado con éxito.');
          this.loadUsers(); // Actualizar la lista después de eliminar
        },
        error: (err) => {
          if (err.status === 200) {
            // Manejar manualmente el código 200 como exitoso
            console.log('Eliminación exitosa con código 200.');
            alert('Usuario eliminado con éxito.');
            this.loadUsers(); // Actualizar la lista después de eliminar
          } else {
            // Manejar otros errores
            console.error('Error al eliminar usuario:', err);
            alert('Hubo un error al intentar eliminar el usuario.');
          }
        },
      });
    }
  }

  openUserModal(user?: any): void {
    this.isModalOpen = true;
    this.isEditMode = !!user;
    this.modalTitle = user ? 'Editar Usuario' : 'Crear Usuario';
  
    if (user) {
      this.currentUserId = user.id;
      this.userForm.patchValue({
        email: user.email,
      });
      this.selectedRoles = user.roles ? user.roles.split(',') : [];
    } else {
      this.currentUserId = null;
      this.userForm.reset();
      this.selectedRoles = [];
      this.userForm.addControl('password', this.fb.control('', [Validators.required])); // Agregar campo password para creación
    }
  }
  
  closeModal(): void {
    this.isModalOpen = false;
    this.userForm.reset();
    this.userForm.removeControl('password'); // Quitar password al cerrar el modal
    this.selectedRoles = [];
  }

  addRole(): void {
    const role = this.roleControl.value;
    if (role && !this.selectedRoles.includes(role)) {
      this.selectedRoles.push(role);
      this.roleControl.reset(); // Resetear el control del select
    } else {
      alert('El rol ya está asignado o no es válido.');
    }
  }

  removeRole(role: string): void {
    this.selectedRoles = this.selectedRoles.filter((r) => r !== role);
  }

  saveUser(): void {
    if (this.userForm.invalid) {
      alert('Por favor completa todos los campos requeridos.');
      return;
    }

    const userData = {
      ...this.userForm.value,
      roles: this.selectedRoles.join(','), // Convertir a una cadena separada por comas
    };

    if (this.isEditMode) {
      // Editar usuario
      this.userService.updateUser({ id: this.currentUserId, ...userData }).subscribe({
        next: () => {
          // Este bloque debería activarse solo para códigos 2xx excepto 202
          console.log('Actualización completada correctamente.');
          this.loadUsers();
          alert('Usuario actualizado con éxito.');
          this.closeModal();
          this.loadUsers();
        },
        error: (err) => {
          if (err.status === 202) {
            // Manejar manualmente el caso de código 202
            console.log('Actualización completada con código 202.');
            this.loadUsers();
            alert('Usuario actualizado con éxito (202).');
            this.closeModal();
            this.loadUsers();
          } else {
            // Otros errores reales
            console.error('Error al actualizar usuario:', err);
            this.loadUsers();
            alert('Error al actualizar usuario: ' + err.message);
          }
        },
      });
      
    } else {
      // Crear usuario
      this.userService.createUser(userData).subscribe({
        next: () => {
          
          alert('Usuario creado con éxito.');
          this.closeModal(); // Cerrar modal
          this.loadUsers(); // Cargar lista de usuarios
        },
        error: (err) => {
          if (err.status === 202) {
            // Manejar 202 como caso exitoso
            console.log('Usuario creado con código 202.');

            alert('Usuario creado con éxito.');
            this.closeModal(); // Cerrar modal
            this.loadUsers(); // Cargar lista de usuarios
          } else {
            // Manejar otros errores
            console.error('Error al crear usuario:', err);
            alert('Error al crear usuario: ' + err.message);
            this.loadUsers();
          }
        },
      });
      this.loadUsers();
      
    }
  }
}

