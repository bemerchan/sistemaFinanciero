import { useQuery } from '@tanstack/react-query';
import { ArrowDownCircle, ArrowUpCircle, Clock } from 'lucide-react';
import { getLastTransactions } from '@/services/transactions';
import type { Transaction } from '@/types';

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 2,
  }).format(value);
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '';
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(dateStr));
}

interface Props {
  accountId: number;
}

export default function TransactionList({ accountId }: Props) {
  const { data: transactions = [], isLoading, error } = useQuery<Transaction[]>({
    queryKey: ['transactions', accountId],
    queryFn: () => getLastTransactions(accountId, 5),
    staleTime: 10_000,
  });

  if (isLoading) {
    return (
      <div className="flex items-center gap-2 text-xs text-muted-foreground py-2">
        <Clock className="w-3 h-3 animate-spin" />
        Cargando movimientos...
      </div>
    );
  }

  if (error) {
    return <p className="text-xs text-destructive">Error cargando transacciones.</p>;
  }

  if (transactions.length === 0) {
    return (
      <p className="text-xs text-muted-foreground py-2">
        Sin movimientos registrados.
      </p>
    );
  }

  return (
    <div className="space-y-1.5">
      {transactions.map((tx) => (
        <div
          key={tx.id}
          className="flex items-center justify-between py-2 px-3 rounded-md bg-muted/50 border border-border/50"
        >
          <div className="flex items-center gap-2 min-w-0">
            {tx.transactionType === 'DEPOSIT' ? (
              <ArrowDownCircle className="w-4 h-4 text-green-500 shrink-0" />
            ) : (
              <ArrowUpCircle className="w-4 h-4 text-destructive shrink-0" />
            )}
            <div className="min-w-0">
              <p className="text-xs font-medium text-foreground">
                {tx.transactionType === 'DEPOSIT' ? 'Consignación' : 'Retiro'}
              </p>
              {tx.description && (
                <p className="text-xs text-muted-foreground truncate max-w-[140px]">{tx.description}</p>
              )}
              <p className="text-xs text-muted-foreground">{formatDate(tx.createdAt)}</p>
            </div>
          </div>
          <div className="text-right shrink-0 ml-2">
            <p
              className={`text-xs font-semibold ${
                tx.transactionType === 'DEPOSIT' ? 'text-green-600' : 'text-destructive'
              }`}
            >
              {tx.transactionType === 'DEPOSIT' ? '+' : '-'}
              {formatCurrency(tx.amount)}
            </p>
            <p className="text-xs text-muted-foreground">
              Saldo: {formatCurrency(tx.balanceAfter)}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}
