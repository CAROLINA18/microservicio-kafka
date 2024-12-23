import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { UserManagementComponent } from './components/user-management/user-management.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full', 
  },
  {
    path: 'login',
    component: LoginComponent,
    title: 'Login',
  },
  {
    path: 'users',
    component: UserManagementComponent,
    title: 'Gestión de Usuarios', 
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
