import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/customers/customers.component').then(m => m.CustomersComponent)
  },
  {
    path: 'customers/:id',
    loadComponent: () =>
      import('./features/customer-detail/customer-detail.component').then(m => m.CustomerDetailComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
