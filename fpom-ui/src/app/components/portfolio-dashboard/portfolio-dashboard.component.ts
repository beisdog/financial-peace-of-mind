import { Component, OnInit, signal, computed, effect, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

// Material Design Imports
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';

// Service and Models
import { PortfolioService } from '../../services/portfolio.service';
import {
  AccountSummary,
  PortfolioPosition,
  AccountDetails
} from '../../models/portfolio.models';

interface PositionTableRow {
  id: number;
  instrumentName: string;
  isin: string;
  assetClass: string;
  valueAmount: number;
  valueCurrency: string;
  fxRate: number;
  marketValue: number;
}

@Component({
  selector: 'app-portfolio-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatToolbarModule,
    MatSelectModule,
    MatFormFieldModule,
    MatTableModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatIconModule,
    MatButtonModule,
    MatSnackBarModule,
    MatChipsModule,
    MatDividerModule
  ],
  templateUrl: './portfolio-dashboard.component.html',
  styles: [`
    .account-option {
      padding: 4px 0;
    }

    .account-main {
      font-weight: 500;
      font-size: 16px;
    }

    .account-details {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.6);
      margin-top: 2px;
    }

    .instrument-cell {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .instrument-name {
      font-weight: 500;
    }

    .instrument-isin {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.6);
    }

    .value-cell {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 2px;
    }

    .amount {
      font-weight: 500;
    }

    .currency {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.6);
    }

    .no-data {
      text-align: center;
      padding: 48px;
      color: rgba(0, 0, 0, 0.6);
    }

    .no-data mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
    }

    .currency-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .loading-shade {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(255, 255, 255, 0.8);
      z-index: 1000;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  `]
})
export class PortfolioDashboardComponent implements OnInit {
  // Inject services
  private portfolioService = inject(PortfolioService);
  private snackBar = inject(MatSnackBar);
  private destroyRef = inject(DestroyRef);

  // Form controls
  accountControl = new FormControl<string>('');

  // Signals for state management
  availableAccounts = signal<AccountSummary[]>([]);
  selectedAccountId = signal<string | null>(null);
  selectedAccountDetails = signal<AccountDetails | null>(null);
  accountPositions = signal<PortfolioPosition[]>([]);
  isLoadingAccounts = signal<boolean>(false);
  isLoadingPositions = signal<boolean>(false);

  // Table configuration
  displayedColumns: string[] = ['instrumentName', 'assetClass', 'valueAmount', 'marketValue', 'fxRate', 'actions'];

  // Computed signals
  tableData = computed<PositionTableRow[]>(() => {
    return this.accountPositions().map(position => ({
      id: position.id || 0,
      instrumentName: position.instrumentNameShort || 'N/A',
      isin: position.isin || 'N/A',
      assetClass: position.assetClassDescriptionShort || 'N/A',
      valueAmount: position.valueAmount || 0,
      valueCurrency: position.valueCurrency || 'CHF',
      fxRate: position.fxRate || 0,
      marketValue: position.marketValueAmount || 0
    }));
  });

  currencyBreakdown = computed(() => {
    const details = this.selectedAccountDetails();
    if (!details?.totalsByCurrency) return [];

    return Object.entries(details.totalsByCurrency)
      .map(([code, amount]) => ({ code, amount }))
      .sort((a, b) => b.amount - a.amount);
  });

  constructor() {
    // Effect to handle account selection
    effect(() => {
      const accountId = this.accountControl.value;
      if (accountId && accountId !== this.selectedAccountId()) {
        this.selectedAccountId.set(accountId);
        this.loadAccountData(accountId);
      }
    }, { allowSignalWrites: true });

    // Setup form control listener
    this.accountControl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(accountId => {
        // This will trigger the effect above
      });
  }

  ngOnInit(): void {
    this.loadAccounts();
  }

  /**
   * Load all available accounts
   */
  private loadAccounts(): void {
    this.isLoadingAccounts.set(true);

    this.portfolioService.getAllAccounts()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (accounts) => {
          this.availableAccounts.set(accounts);
          this.isLoadingAccounts.set(false);

          // Auto-select first account if none selected
          if (accounts.length > 0 && !this.selectedAccountId()) {
            const firstAccount = accounts[0];
            this.accountControl.setValue(firstAccount.accountIdFake);
          }
        },
        error: (error) => {
          this.handleError('Failed to load accounts', error);
          this.isLoadingAccounts.set(false);
        }
      });
  }

  /**
   * Load data for the selected account
   */
  private loadAccountData(accountId: string): void {
    this.isLoadingPositions.set(true);

    // Load account details and positions in parallel
    Promise.all([
      this.portfolioService.getAccountDetails(accountId).toPromise(),
      this.portfolioService.getPositionsByAccountId(accountId).toPromise()
    ]).then(([details, positions]) => {
      this.selectedAccountDetails.set(details || null);
      this.accountPositions.set(positions || []);
      this.isLoadingPositions.set(false);
    }).catch((error) => {
      this.handleError('Failed to load account data', error);
      this.isLoadingPositions.set(false);
    });
  }

  /**
   * Track function for account options
   */
  trackByAccountId(index: number, account: AccountSummary): string {
    return account.accountIdFake;
  }

  /**
   * Format currency amount
   */
  formatCurrency(amount: number, currency: string): string {
    return new Intl.NumberFormat('en-CH', {
      style: 'currency',
      currency: currency
    }).format(amount);
  }

  /**
   * Format number
   */
  formatNumber(amount: number): string {
    return new Intl.NumberFormat('en-CH', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  }

  /**
   * Get risk level CSS class
   */
  getRiskLevelClass(riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH'): string {
    switch (riskLevel) {
      case 'LOW': return 'risk-low';
      case 'MEDIUM': return 'risk-medium';
      case 'HIGH': return 'risk-high';
      default: return '';
    }
  }

  /**
   * Get concentration risk CSS class
   */
  getConcentrationRiskClass(concentrationRisk?: number): string {
    if (!concentrationRisk) return '';
    if (concentrationRisk > 50) return 'risk-high';
    if (concentrationRisk > 25) return 'risk-medium';
    return 'risk-low';
  }

  /**
   * Handle position details view
   */
  viewPositionDetails(positionId: number): void {
    this.snackBar.open(`Viewing details for position ${positionId}`, 'Close', {
      duration: 3000
    });
  }

  /**
   * Handle position edit
   */
  editPosition(positionId: number): void {
    this.snackBar.open(`Editing position ${positionId}`, 'Close', {
      duration: 3000
    });
  }

  /**
   * Handle errors with user feedback
   */
  private handleError(message: string, error: any): void {
    console.error(message, error);
    this.snackBar.open(`${message}: ${error.message || 'Unknown error'}`, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }
}
