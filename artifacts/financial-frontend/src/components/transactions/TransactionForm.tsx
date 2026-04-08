import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { registerTransaction } from '@/services/transactions';
import { ApiError } from '@/services/api';
import type { Account } from '@/types';

const schema = z.object({
  transactionType: z.enum(['DEPOSIT', 'WITHDRAWAL']),
  amount: z
    .string()
    .min(1, 'Ingrese un monto')
    .refine((v) => !isNaN(Number(v)) && Number(v) >= 0.01, 'Monto mínimo $0.01'),
  description: z.string().max(255).optional(),
});

type FormData = z.infer<typeof schema>;

interface Props {
  account: Account;
  customerId: number;
}

export default function TransactionForm({ account, customerId }: Props) {
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { transactionType: 'DEPOSIT', amount: '', description: '' },
  });

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      registerTransaction(account.id, {
        transactionType: data.transactionType,
        amount: parseFloat(data.amount),
        description: data.description || undefined,
      }),
    onSuccess: (res) => {
      toast({ title: res.message });
      queryClient.invalidateQueries({ queryKey: ['transactions', account.id] });
      queryClient.invalidateQueries({ queryKey: ['accounts', customerId] });
      reset({ transactionType: 'DEPOSIT', amount: '', description: '' });
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

  const isActive = account.status === 'ACTIVE';

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
      {!isActive && (
        <p className="text-xs text-destructive font-medium">
          Esta cuenta está inactiva. No se pueden registrar transacciones.
        </p>
      )}
      <div className="grid grid-cols-2 gap-3">
        <div className="space-y-1">
          <Label className="text-xs">Tipo *</Label>
          <Select
            value={watch('transactionType')}
            onValueChange={(v) => setValue('transactionType', v as 'DEPOSIT' | 'WITHDRAWAL')}
            disabled={!isActive}
          >
            <SelectTrigger className="h-8 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="DEPOSIT">Consignación</SelectItem>
              <SelectItem value="WITHDRAWAL">Retiro</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-1">
          <Label htmlFor={`amount-${account.id}`} className="text-xs">Monto *</Label>
          <Input
            id={`amount-${account.id}`}
            type="number"
            step="0.01"
            min="0.01"
            placeholder="0.00"
            className="h-8 text-xs"
            {...register('amount')}
            disabled={!isActive}
          />
          {errors.amount && <p className="text-xs text-destructive">{errors.amount.message}</p>}
        </div>
      </div>
      <div className="space-y-1">
        <Label htmlFor={`desc-${account.id}`} className="text-xs">Descripción (opcional)</Label>
        <Input
          id={`desc-${account.id}`}
          placeholder="Ej: Pago de nómina"
          className="h-8 text-xs"
          {...register('description')}
          disabled={!isActive}
        />
      </div>
      <Button
        type="submit"
        size="sm"
        className="w-full h-8 text-xs"
        disabled={mutation.isPending || !isActive}
      >
        {mutation.isPending ? 'Procesando...' : 'Registrar transacción'}
      </Button>
    </form>
  );
}
