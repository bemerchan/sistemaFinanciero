import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { createCustomer, updateCustomer } from '@/services/customers';
import { ApiError } from '@/services/api';
import type { Customer } from '@/types';

const schema = z.object({
  firstName: z.string().min(2, 'Mínimo 2 caracteres').max(100),
  lastName: z.string().min(2, 'Mínimo 2 caracteres').max(100),
  identificationType: z.enum(['CC', 'CE', 'TI', 'PASSPORT', 'NIT']),
  identificationNumber: z.string().min(5, 'Mínimo 5 caracteres').max(50),
  email: z.string().email('Correo no válido').max(150),
  birthDate: z.string().min(1, 'Fecha requerida'),
});

type FormData = z.infer<typeof schema>;

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  customer?: Customer | null;
}

export default function CustomerFormDialog({ open, onOpenChange, customer }: Props) {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const isEdit = !!customer;

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      firstName: '',
      lastName: '',
      identificationType: 'CC',
      identificationNumber: '',
      email: '',
      birthDate: '',
    },
  });

  useEffect(() => {
    if (open && customer) {
      reset({
        firstName: customer.firstName,
        lastName: customer.lastName,
        identificationType: customer.identificationType,
        identificationNumber: customer.identificationNumber,
        email: customer.email,
        birthDate: customer.birthDate,
      });
    } else if (open && !customer) {
      reset({
        firstName: '',
        lastName: '',
        identificationType: 'CC',
        identificationNumber: '',
        email: '',
        birthDate: '',
      });
    }
  }, [open, customer, reset]);

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? updateCustomer(customer!.id, data)
        : createCustomer(data),
    onSuccess: (res) => {
      toast({ title: res.message });
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      onOpenChange(false);
    },
    onError: (err) => {
      if (err instanceof ApiError) {
        const detail = err.errors?.map((e) => e.message).join(' | ') || err.message;
        toast({ title: 'Error', description: detail, variant: 'destructive' });
      } else {
        toast({ title: 'Error inesperado', variant: 'destructive' });
      }
    },
  });

  const onSubmit = (data: FormData) => mutation.mutate(data);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar Cliente' : 'Nuevo Cliente'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <Label htmlFor="firstName">Nombre *</Label>
              <Input id="firstName" {...register('firstName')} placeholder="Juan" />
              {errors.firstName && <p className="text-xs text-destructive">{errors.firstName.message}</p>}
            </div>
            <div className="space-y-1">
              <Label htmlFor="lastName">Apellido *</Label>
              <Input id="lastName" {...register('lastName')} placeholder="Pérez" />
              {errors.lastName && <p className="text-xs text-destructive">{errors.lastName.message}</p>}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <Label>Tipo de identificación *</Label>
              <Select
                value={watch('identificationType')}
                onValueChange={(v) => setValue('identificationType', v as FormData['identificationType'])}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccionar..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="CC">CC - Cédula de Ciudadanía</SelectItem>
                  <SelectItem value="CE">CE - Cédula de Extranjería</SelectItem>
                  <SelectItem value="TI">TI - Tarjeta de Identidad</SelectItem>
                  <SelectItem value="PASSPORT">Pasaporte</SelectItem>
                  <SelectItem value="NIT">NIT</SelectItem>
                </SelectContent>
              </Select>
              {errors.identificationType && (
                <p className="text-xs text-destructive">{errors.identificationType.message}</p>
              )}
            </div>
            <div className="space-y-1">
              <Label htmlFor="identificationNumber">No. de identificación *</Label>
              <Input id="identificationNumber" {...register('identificationNumber')} placeholder="12345678" />
              {errors.identificationNumber && (
                <p className="text-xs text-destructive">{errors.identificationNumber.message}</p>
              )}
            </div>
          </div>

          <div className="space-y-1">
            <Label htmlFor="email">Correo electrónico *</Label>
            <Input id="email" type="email" {...register('email')} placeholder="juan@ejemplo.com" />
            {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
          </div>

          <div className="space-y-1">
            <Label htmlFor="birthDate">Fecha de nacimiento *</Label>
            <Input id="birthDate" type="date" {...register('birthDate')} />
            {errors.birthDate && <p className="text-xs text-destructive">{errors.birthDate.message}</p>}
          </div>

          <DialogFooter className="pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={mutation.isPending}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? 'Guardando...' : isEdit ? 'Actualizar' : 'Crear cliente'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
