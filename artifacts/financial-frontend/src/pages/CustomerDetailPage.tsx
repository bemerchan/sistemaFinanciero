import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'wouter';
import {
  ArrowLeft, Plus, CreditCard, ChevronDown, ChevronUp,
  User, Mail, Calendar, Hash, Wallet, AlertCircle, Trash2,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { useToast } from '@/hooks/use-toast';
import CreateAccountDialog from '@/components/accounts/CreateAccountDialog';
import TransactionForm from '@/components/transactions/TransactionForm';
import TransactionList from '@/components/transactions/TransactionList';
import { getCustomer } from '@/services/customers';
import { getAccountsByCustomer, deleteAccount } from '@/services/accounts';
import { ApiError } from '@/services/api';
import type { Account, Customer } from '@/types';

function calcAge(birthDate: string): number {
  const today = new Date();
  const birth = new Date(birthDate);
  let age = today.getFullYear() - birth.getFullYear();
  const m = today.getMonth() - birth.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
  return age;
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-';
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit', month: 'short', year: 'numeric',
  }).format(new Date(dateStr));
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 2,
  }).format(value);
}

const ACCOUNT_TYPE_LABELS: Record<string, string> = {
  SAVINGS: 'Ahorro',
  CHECKING: 'Corriente',
};

const ID_TYPE_LABELS: Record<string, string> = {
  CC: 'Cédula de Ciudadanía',
  CE: 'Cédula de Extranjería',
  TI: 'Tarjeta de Identidad',
  PASSPORT: 'Pasaporte',
  NIT: 'NIT',
};

function AccountCard({ account, customerId }: { account: Account; customerId: number }) {
  const [expanded, setExpanded] = useState(false);
  const [deletingAccount, setDeletingAccount] = useState(false);
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const deleteMutation = useMutation({
    mutationFn: () => deleteAccount(account.id),
    onSuccess: () => {
      toast({ title: 'Cuenta eliminada exitosamente' });
      queryClient.invalidateQueries({ queryKey: ['accounts', customerId] });
      setDeletingAccount(false);
    },
    onError: (err) => {
      if (err instanceof ApiError) {
        toast({ title: 'No se puede eliminar', description: err.message, variant: 'destructive' });
      } else {
        toast({ title: 'Error inesperado', variant: 'destructive' });
      }
      setDeletingAccount(false);
    },
  });

  const isActive = account.status === 'ACTIVE';

  return (
    <>
      <Card className={`border ${isActive ? 'border-border' : 'border-muted opacity-70'}`}>
        <CardHeader className="py-3 px-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                account.accountType === 'SAVINGS' ? 'bg-blue-50' : 'bg-emerald-50'
              }`}>
                <CreditCard className={`w-4 h-4 ${
                  account.accountType === 'SAVINGS' ? 'text-blue-600' : 'text-emerald-600'
                }`} />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-mono font-medium text-sm">{account.accountNumber}</span>
                  <Badge variant={isActive ? 'default' : 'secondary'} className="text-xs h-4 px-1.5">
                    {isActive ? 'Activa' : 'Inactiva'}
                  </Badge>
                </div>
                <p className="text-xs text-muted-foreground">
                  Cuenta de {ACCOUNT_TYPE_LABELS[account.accountType] ?? account.accountType}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <div className="text-right">
                <p className="text-xs text-muted-foreground">Saldo</p>
                <p className={`font-bold text-sm ${account.balance < 0 ? 'text-destructive' : 'text-foreground'}`}>
                  {formatCurrency(account.balance)}
                </p>
              </div>
              <Button
                variant="ghost"
                size="icon"
                className="h-7 w-7 text-destructive hover:text-destructive"
                onClick={() => setDeletingAccount(true)}
                title="Eliminar cuenta"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                className="h-7 w-7"
                onClick={() => setExpanded(!expanded)}
                title={expanded ? 'Ocultar' : 'Ver transacciones'}
              >
                {expanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              </Button>
            </div>
          </div>
        </CardHeader>

        {expanded && (
          <CardContent className="px-4 pb-4 space-y-4">
            <Separator />
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-2">
                Registrar transacción
              </p>
              <TransactionForm account={account} customerId={customerId} />
            </div>
            <Separator />
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-2">
                Últimas 5 transacciones
              </p>
              <TransactionList accountId={account.id} />
            </div>
          </CardContent>
        )}
      </Card>

      <AlertDialog open={deletingAccount} onOpenChange={(o) => !o && setDeletingAccount(false)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Eliminar cuenta</AlertDialogTitle>
            <AlertDialogDescription>
              ¿Eliminar la cuenta <strong className="font-mono">{account.accountNumber}</strong>?
              Saldo actual: <strong>{formatCurrency(account.balance)}</strong>.
              Esta acción no se puede deshacer.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={() => deleteMutation.mutate()}
            >
              {deleteMutation.isPending ? 'Eliminando...' : 'Eliminar'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

export default function CustomerDetailPage() {
  const params = useParams<{ id: string }>();
  const customerId = parseInt(params.id ?? '0', 10);
  const [createAccountOpen, setCreateAccountOpen] = useState(false);

  const { data: customer, isLoading: loadingCustomer, error: customerError } = useQuery<Customer>({
    queryKey: ['customer', customerId],
    queryFn: () => getCustomer(customerId),
    enabled: !!customerId,
  });

  const { data: accounts = [], isLoading: loadingAccounts } = useQuery<Account[]>({
    queryKey: ['accounts', customerId],
    queryFn: () => getAccountsByCustomer(customerId),
    enabled: !!customerId,
  });

  if (customerError) {
    return (
      <div className="container mx-auto px-4 py-8">
        <Link href="/">
          <Button variant="ghost" size="sm" className="mb-6">
            <ArrowLeft className="w-4 h-4" /> Volver
          </Button>
        </Link>
        <div className="flex items-center gap-2 text-destructive">
          <AlertCircle className="w-5 h-5" />
          <span>Cliente no encontrado o error al cargar datos.</span>
        </div>
      </div>
    );
  }

  const totalBalance = accounts.reduce((sum, a) => sum + (a.balance ?? 0), 0);

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <Link href="/">
        <Button variant="ghost" size="sm" className="mb-6 -ml-2">
          <ArrowLeft className="w-4 h-4" /> Volver a Clientes
        </Button>
      </Link>

      {loadingCustomer ? (
        <div className="space-y-4">
          <Skeleton className="h-8 w-64" />
          <Skeleton className="h-40 w-full" />
        </div>
      ) : customer ? (
        <>
          <div className="flex items-start justify-between mb-6">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-xl bg-primary flex items-center justify-center">
                <span className="text-primary-foreground font-bold text-lg">
                  {customer.firstName[0]}{customer.lastName[0]}
                </span>
              </div>
              <div>
                <h1 className="text-2xl font-bold">
                  {customer.firstName} {customer.lastName}
                </h1>
                <p className="text-sm text-muted-foreground">Cliente #{customer.id}</p>
              </div>
            </div>
            {accounts.length > 0 && (
              <div className="text-right">
                <p className="text-xs text-muted-foreground">Saldo total</p>
                <p className="text-xl font-bold text-primary">{formatCurrency(totalBalance)}</p>
              </div>
            )}
          </div>

          <Card className="mb-6">
            <CardHeader className="pb-3">
              <CardTitle className="text-base flex items-center gap-2">
                <User className="w-4 h-4 text-primary" />
                Información del cliente
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div className="space-y-0.5">
                  <p className="text-xs text-muted-foreground flex items-center gap-1">
                    <Hash className="w-3 h-3" /> Identificación
                  </p>
                  <p className="text-sm font-medium">
                    {ID_TYPE_LABELS[customer.identificationType] ?? customer.identificationType}: {customer.identificationNumber}
                  </p>
                </div>
                <div className="space-y-0.5">
                  <p className="text-xs text-muted-foreground flex items-center gap-1">
                    <Mail className="w-3 h-3" /> Correo
                  </p>
                  <p className="text-sm font-medium break-all">{customer.email}</p>
                </div>
                <div className="space-y-0.5">
                  <p className="text-xs text-muted-foreground flex items-center gap-1">
                    <Calendar className="w-3 h-3" /> Nacimiento
                  </p>
                  <p className="text-sm font-medium">
                    {formatDate(customer.birthDate)} ({customer.age ?? calcAge(customer.birthDate)} años)
                  </p>
                </div>
                <div className="space-y-0.5">
                  <p className="text-xs text-muted-foreground">Registrado</p>
                  <p className="text-sm font-medium">{formatDate(customer.createdAt)}</p>
                </div>
                <div className="space-y-0.5">
                  <p className="text-xs text-muted-foreground">Actualizado</p>
                  <p className="text-sm font-medium">{formatDate(customer.updatedAt)}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Wallet className="w-5 h-5 text-primary" />
              <h2 className="text-lg font-semibold">Cuentas bancarias</h2>
              <Badge variant="secondary" className="text-xs">
                {accounts.length}
              </Badge>
            </div>
            <Button size="sm" onClick={() => setCreateAccountOpen(true)}>
              <Plus className="w-3.5 h-3.5" />
              Nueva cuenta
            </Button>
          </div>

          {loadingAccounts ? (
            <div className="space-y-3">
              {[0, 1].map((i) => <Skeleton key={i} className="h-20 w-full" />)}
            </div>
          ) : accounts.length === 0 ? (
            <Card className="border-dashed">
              <CardContent className="flex flex-col items-center py-10 text-muted-foreground">
                <CreditCard className="w-10 h-10 mb-3 opacity-30" />
                <p className="font-medium">Este cliente no tiene cuentas registradas</p>
                <Button
                  variant="outline"
                  size="sm"
                  className="mt-3"
                  onClick={() => setCreateAccountOpen(true)}
                >
                  <Plus className="w-3 h-3" /> Crear cuenta bancaria
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-3">
              {accounts.map((account) => (
                <AccountCard key={account.id} account={account} customerId={customerId} />
              ))}
            </div>
          )}
        </>
      ) : null}

      <CreateAccountDialog
        open={createAccountOpen}
        onOpenChange={setCreateAccountOpen}
        customerId={customerId}
      />
    </div>
  );
}
