export type IdentificationType = 'CC' | 'CE' | 'TI' | 'PASSPORT' | 'NIT';

export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  identificationType: IdentificationType;
  identificationNumber: string;
  email: string;
  birthDate: string;
  age?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  identificationType: IdentificationType;
  identificationNumber: string;
  email: string;
  birthDate: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface ApiErrorBody {
  status: number;
  error: string;
  message: string;
  errors?: Array<{ field?: string; rejectedValue?: string; message: string }>;
  timestamp: string;
}

export interface HalCustomers {
  _embedded?: { customers: Customer[] };
  page?: { size: number; totalElements: number; totalPages: number; number: number };
}
