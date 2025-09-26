import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { PortfolioService } from '../services/portfolio.service';
import { AccountSummary, PortfolioPosition, AccountDetails } from '../models/portfolio.models';

/**
 * Example component demonstrating how to use the PortfolioService
 * Shows account listing and position details
 */
@Component({
  selector: 'app-account-portfolio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container mx-auto p-6">
      <h1 class="text-3xl font-bold mb-6">Portfolio Accounts</h1>
      
      <!-- Loading State -->
      <div *ngIf="isLoading" class="flex justify-center items-center py-8">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
      
      <!-- Error State -->
      <div *ngIf="errorMessage" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        <strong>Error:</strong> {{ errorMessage }}
      </div>
      
      <!-- Account Summary Section -->
      <div class="mb-8" *ngIf="!isLoading">
        <h2 class="text-2xl font-semibold mb-4">All Accounts</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div 
            *ngFor="let account of accounts" 
            class="bg-white shadow rounded-lg p-4 border hover:shadow-lg transition-shadow cursor-pointer"
            (click)="selectAccount(account.accountIdFake)"
            [class.ring-2]="selectedAccountId === account.accountIdFake"
            [class.ring-blue-500]="selectedAccountId === account.accountIdFake">
            
            <h3 class="font-semibold text-lg mb-2">{{ account.accountIdFake }}</h3>
            <div class="text-sm text-gray-600 space-y-1">
              <p><span class="font-medium">Partner:</span> {{ account.partnerIdFake }}</p>
              <p><span class="font-medium">Positions:</span> {{ account.positionCount }}</p>
              <p><span class="font-medium">Total Value:</span> 
                {{ formatCurrency(account.totalValue, account.currency) }}
              </p>
              <p><span class="font-medium">Avg Position:</span> 
                {{ formatCurrency(account.averagePositionValue, account.currency) }}
              </p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Selected Account Details -->
      <div *ngIf="selectedAccountId && accountDetails" class="mb-8">
        <h2 class="text-2xl font-semibold mb-4">Account Details: {{ selectedAccountId }}</h2>
        
        <div class="bg-white shadow rounded-lg p-6 mb-6">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <!-- Basic Info -->
            <div>
              <h4 class="font-semibold text-gray-700 mb-2">Basic Info</h4>
              <p><span class="font-medium">Partner:</span> {{ accountDetails.partnerIdFake }}</p>
              <p><span class="font-medium">Positions:</span> {{ accountDetails.positionCount }}</p>
              <p><span class="font-medium">Primary Currency:</span> {{ accountDetails.primaryCurrency }}</p>
            </div>
            
            <!-- Currency Breakdown -->
            <div>
              <h4 class="font-semibold text-gray-700 mb-2">By Currency</h4>
              <div *ngFor="let currency of getCurrencyBreakdown()">
                <p>{{ currency.code }}: {{ formatCurrency(currency.amount, currency.code) }}</p>
              </div>
            </div>
            
            <!-- Asset Classes -->
            <div>
              <h4 class="font-semibold text-gray-700 mb-2">Asset Classes</h4>
              <div *ngFor="let asset of getAssetClassBreakdown()">
                <p>{{ asset.name }}: {{ asset.count }} positions</p>
              </div>
            </div>
            
            <!-- Risk Metrics -->
            <div>
              <h4 class="font-semibold text-gray-700 mb-2">Risk Metrics</h4>
              <p><span class="font-medium">Risk Level:</span> 
                <span [class]="getRiskLevelClass(accountDetails.riskMetrics.riskLevel)">
                  {{ accountDetails.riskMetrics.riskLevel }}
                </span>
              </p>
              <p><span class="font-medium">Currencies:</span> {{ accountDetails.riskMetrics.currencyCount }}</p>
              <p><span class="font-medium">Asset Classes:</span> {{ accountDetails.riskMetrics.assetClassCount }}</p>
              <p><span class="font-medium">Concentration:</span> {{ accountDetails.riskMetrics.concentrationRisk.toFixed(1) }}%</p>
              <p><span class="font-medium">FX Exposure:</span> 
                <span [class]="accountDetails.riskMetrics.hasFxExposure ? 'text-yellow-600' : 'text-green-600'">
                  {{ accountDetails.riskMetrics.hasFxExposure ? 'Yes' : 'No' }}
                </span>
              </p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Account Positions -->
      <div *ngIf="selectedAccountId && accountPositions.length > 0">
        <h2 class="text-2xl font-semibold mb-4">Positions ({{ accountPositions.length }})</h2>
        
        <div class="bg-white shadow rounded-lg overflow-hidden">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Instrument
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  ISIN
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Asset Class
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Value
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Currency
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  FX Rate
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr *ngFor="let position of accountPositions">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {{ position.instrumentNameShort || 'N/A' }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ position.isin || 'N/A' }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ position.assetClassDescriptionShort || 'N/A' }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ formatCurrency(position.valueAmount || 0, position.valueCurrency || 'CHF') }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ position.valueCurrency }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ position.fxRate?.toFixed(4) || 'N/A' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <!-- Search Example -->
      <div class="mt-8">
        <h2 class="text-2xl font-semibold mb-4">Search Positions</h2>
        <div class="flex gap-4 mb-4">
          <input 
            type="text" 
            [(ngModel)]="searchTerm"
            placeholder="Search by instrument name..."
            class="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            (keyup.enter)="searchPositions()">
          <button 
            (click)="searchPositions()"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">
            Search
          </button>
        </div>
        
        <div *ngIf="searchResults.length > 0" class="bg-white shadow rounded-lg p-4">
          <h3 class="font-semibold mb-2">Search Results ({{ searchResults.length }})</h3>
          <div class="space-y-2">
            <div *ngFor="let result of searchResults" class="border-b pb-2">
              <p class="font-medium">{{ result.instrumentNameShort }}</p>
              <p class="text-sm text-gray-600">
                {{ result.isin }} | {{ result.assetClassDescriptionShort }} | 
                {{ formatCurrency(result.valueAmount || 0, result.valueCurrency || 'CHF') }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container {
      max-width: 1200px;
    }
  `]
})
export class AccountPortfolioComponent implements OnInit, OnDestroy {
  accounts: AccountSummary[] = [];
  accountPositions: PortfolioPosition[] = [];
  accountDetails: AccountDetails | null = null;
  searchResults: PortfolioPosition[] = [];
  
  selectedAccountId: string | null = null;
  searchTerm: string = '';
  isLoading = false;
  errorMessage = '';
  
  private destroy$ = new Subject<void>();

  constructor(private portfolioService: PortfolioService) {}

  ngOnInit(): void {
    this.loadAccounts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load all accounts using the service
   */
  loadAccounts(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.portfolioService.getAllAccounts()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (accounts) => {
          this.accounts = accounts;
          this.isLoading = false;
        },
        error: (error) => {
          this.errorMessage = error.message;
          this.isLoading = false;
        }
      });
  }

  /**
   * Select an account and load its details and positions
   */
  selectAccount(accountIdFake: string): void {
    this.selectedAccountId = accountIdFake;
    this.loadAccountDetails(accountIdFake);
    this.loadAccountPositions(accountIdFake);
  }

  /**
   * Load detailed account information
   */
  private loadAccountDetails(accountIdFake: string): void {
    this.portfolioService.getAccountDetails(accountIdFake)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (details) => {
          this.accountDetails = details;
        },
        error: (error) => {
          this.errorMessage = `Error loading account details: ${error.message}`;
        }
      });
  }

  /**
   * Load positions for the selected account
   */
  private loadAccountPositions(accountIdFake: string): void {
    this.portfolioService.getPositionsByAccountId(accountIdFake)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (positions) => {
          this.accountPositions = positions;
        },
        error: (error) => {
          this.errorMessage = `Error loading account positions: ${error.message}`;
        }
      });
  }

  /**
   * Search positions by instrument name
   */
  searchPositions(): void {
    if (!this.searchTerm.trim()) {
      this.searchResults = [];
      return;
    }

    this.portfolioService.searchPositions(this.searchTerm)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (results) => {
          this.searchResults = results;
        },
        error: (error) => {
          this.errorMessage = `Error searching positions: ${error.message}`;
        }
      });
  }

  /**
   * Get currency breakdown for display
   */
  getCurrencyBreakdown(): { code: string; amount: number }[] {
    if (!this.accountDetails?.totalsByCurrency) return [];
    
    return Object.entries(this.accountDetails.totalsByCurrency)
      .map(([code, amount]) => ({ code, amount }));
  }

  /**
   * Get asset class breakdown for display
   */
  getAssetClassBreakdown(): { name: string; count: number }[] {
    if (!this.accountDetails?.assetClassBreakdown) return [];
    
    return Object.entries(this.accountDetails.assetClassBreakdown)
      .map(([name, count]) => ({ name, count }));
  }

  /**
   * Format currency amount for display
   */
  formatCurrency(amount: number, currency: string): string {
    return this.portfolioService.formatCurrencyAmount(amount, currency);
  }

  /**
   * Get CSS class for risk level
   */
  getRiskLevelClass(riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'): string {
    return this.portfolioService.getRiskLevelColorClass(riskLevel);
  }
}
