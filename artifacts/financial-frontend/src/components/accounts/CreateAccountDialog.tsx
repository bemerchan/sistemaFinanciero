import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { createAccount } from '@/services/accounts';
import { ApiError } from '@/services/api';

const schema = z.object({
  accountType: z.enum(['SAVINGS', 'CHECKING']),
});

type FormData = z.infer<typeof schema>;

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  customerId: number;
}

export default function CreateAccountDialog({ open, onOpenChange, customerId }: Props) {
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const {
    setValue,
    watch,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { accountType: 'SAVINGS' },
  });

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      createAccount({ accountType: data.accountType, customerId }),
    onSuccess: (res) => {
      toast({ title: res.message });
      queryClient.invalidateQueries({ queryKey: ['accounts', customerId] });
      reset();
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
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Nueva Cuenta Bancaria</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <Label>Tipo de cuenta *</Label>
            <Select
              value={watch('accountType')}
              onValueChange={(v) => setValue('accountType', v as FormData['accountType'])}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="SAVINGS">Cuenta de Ahorro (53XXXXXXXX)</SelectItem>
                <SelectItem value="CHECKING">Cuenta Corriente (33XXXXXXXX)</SelectItem>
              </SelectContent>
            </Select>
            {errors.accountType && (
              <p className="text-xs text-destructive">{errors.accountType.message}</p>
            )}
          </div>

          <p className="text-xs text-muted-foreground">
            El número de cuenta será generado automáticamente. El saldo inicial es $0.00.
          </p>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={mutation.isPending}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? 'Creando...' : 'Crear cuenta'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
