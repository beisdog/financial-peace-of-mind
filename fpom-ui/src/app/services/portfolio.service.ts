import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  PortfolioPosition,
  AccountSummary,
  AccountDetails,
  PortfolioSummary,
  DatabaseStats,
  ImportResponse,
  PagedResponse,
  ApiResponse
} from '../models/portfolio.models';

/**
 * Angular service for Portfolio Position and Account operations
 * Communicates with the Spring Boot backend REST API
 */
@Injectable({
  providedIn: 'root'
})
export class PortfolioService {
  private readonly baseUrl = 'http://localhost:8080/api/portfolio-positions';

  constructor(private http: HttpClient) {}

  // ==================== ACCOUNT OPERATIONS ====================

  /**
   * Get all accounts with summary information
   * @returns Observable<AccountSummary[]>
   */
  getAllAccounts(): Observable<AccountSummary[]> {
    return this.http.get<AccountSummary[]>(`${this.baseUrl}/accounts`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get simple list of account IDs
   * @returns Observable<string[]>
   */
  getAccountsList(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/accounts/list`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get detailed account information including positions
   * @param accountIdFake - The account ID to get details for
   * @returns Observable<AccountDetails>
   */
  getAccountDetails(accountIdFake: string): Observable<AccountDetails> {
    return this.http.get<AccountDetails>(`${this.baseUrl}/accounts/${accountIdFake}/details`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get all positions for a specific account
   * @param accountIdFake - The account ID to get positions for
   * @returns Observable<PortfolioPosition[]>
   */
  getPositionsByAccountId(accountIdFake: string): Observable<PortfolioPosition[]> {
    return this.http.get<PortfolioPosition[]>(`${this.baseUrl}/account/${accountIdFake}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  // ==================== POSITION OPERATIONS ====================

  /**
   * Get all positions with pagination
   * @param page - Page number (0-based)
   * @param size - Page size
   * @param sort - Sort criteria (e.g., 'valueAmount,desc')
   * @returns Observable<PagedResponse<PortfolioPosition>>
   */
  getAllPositions(page: number = 0, size: number = 20, sort: string = 'id,asc'): Observable<PagedResponse<PortfolioPosition>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<PagedResponse<PortfolioPosition>>(this.baseUrl, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get a specific position by ID
   * @param id - Position ID
   * @returns Observable<PortfolioPosition>
   */
  getPositionById(id: number): Observable<PortfolioPosition> {
    return this.http.get<PortfolioPosition>(`${this.baseUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get positions by partner ID
   * @param partnerIdFake - Partner ID
   * @returns Observable<PortfolioPosition[]>
   */
  getPositionsByPartnerId(partnerIdFake: string): Observable<PortfolioPosition[]> {
    return this.http.get<PortfolioPosition[]>(`${this.baseUrl}/partner/${partnerIdFake}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get positions by asset class
   * @param assetClass - Asset class name
   * @returns Observable<PortfolioPosition[]>
   */
  getPositionsByAssetClass(assetClass: string): Observable<PortfolioPosition[]> {
    return this.http.get<PortfolioPosition[]>(`${this.baseUrl}/asset-class/${assetClass}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get positions by currency
   * @param currency - Currency code
   * @returns Observable<PortfolioPosition[]>
   */
  getPositionsByCurrency(currency: string): Observable<PortfolioPosition[]> {
    return this.http.get<PortfolioPosition[]>(`${this.baseUrl}/currency/${currency}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Search positions by instrument name
   * @param instrumentName - Instrument name to search for
   * @returns Observable<PortfolioPosition[]>
   */
  searchPositions(instrumentName: string): Observable<PortfolioPosition[]> {
    const params = new HttpParams().set('instrumentName', instrumentName);
    
    return this.http.get<PortfolioPosition[]>(`${this.baseUrl}/search`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get positions with value greater than specified amount
   * @param amount - Minimum value amount
   * @returns Observable<PortfolioPosition[]>
   */
  getPositionsWithValueGreaterThan(amount: number): Observable<PortfolioPosition[]> {
    return this.http.get<PortfolioPosition[]>(`${this.baseUrl}/value-greater-than/${amount}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get top positions by value
   * @param limit - Number of positions to return (default: 10)
   * @returns Observable<PortfolioPosition[]>
   */
  getTopPositions(limit: number = 10): Observable<PortfolioPosition[]> {
    const params = new HttpParams().set('limit', limit.toString());
    
    return this.http.get<PortfolioPosition[]>(`${this.baseUrl}/top-positions`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  // ==================== CRUD OPERATIONS ====================

  /**
   * Create a new position
   * @param position - Portfolio position to create
   * @returns Observable<PortfolioPosition>
   */
  createPosition(position: PortfolioPosition): Observable<PortfolioPosition> {
    return this.http.post<PortfolioPosition>(this.baseUrl, position)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Update an existing position (full update)
   * @param id - Position ID
   * @param position - Updated position data
   * @returns Observable<PortfolioPosition>
   */
  updatePosition(id: number, position: PortfolioPosition): Observable<PortfolioPosition> {
    return this.http.put<PortfolioPosition>(`${this.baseUrl}/${id}`, position)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Partially update a position
   * @param id - Position ID
   * @param updates - Partial position updates
   * @returns Observable<PortfolioPosition>
   */
  patchPosition(id: number, updates: Partial<PortfolioPosition>): Observable<PortfolioPosition> {
    return this.http.patch<PortfolioPosition>(`${this.baseUrl}/${id}`, updates)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Delete a position by ID
   * @param id - Position ID to delete
   * @returns Observable<ApiResponse<any>>
   */
  deletePosition(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.baseUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Delete multiple positions
   * @param ids - Array of position IDs to delete
   * @returns Observable<ApiResponse<any>>
   */
  deleteMultiplePositions(ids: number[]): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.baseUrl}/batch`, { body: ids })
      .pipe(
        catchError(this.handleError)
      );
  }

  // ==================== SUMMARY AND ANALYTICS ====================

  /**
   * Get portfolio summary statistics
   * @returns Observable<PortfolioSummary>
   */
  getPortfolioSummary(): Observable<PortfolioSummary> {
    return this.http.get<PortfolioSummary>(`${this.baseUrl}/summary`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get portfolio summary for a specific partner
   * @param partnerIdFake - Partner ID
   * @returns Observable<any>
   */
  getPartnerPortfolioSummary(partnerIdFake: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/summary/partner/${partnerIdFake}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get database statistics
   * @returns Observable<DatabaseStats>
   */
  getDatabaseStats(): Observable<DatabaseStats> {
    return this.http.get<DatabaseStats>(`${this.baseUrl}/stats`)
      .pipe(
        catchError(this.handleError)
      );
  }

  // ==================== IMPORT OPERATIONS ====================

  /**
   * Import portfolio positions from Excel
   * @param clearExisting - Whether to clear existing data first
   * @returns Observable<ImportResponse>
   */
  importPortfolioPositions(clearExisting: boolean = false): Observable<ImportResponse> {
    const params = new HttpParams().set('clearExisting', clearExisting.toString());
    
    return this.http.post<ImportResponse>(`${this.baseUrl}/import`, {}, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Clear all portfolio positions (use with caution!)
   * @returns Observable<ApiResponse<any>>
   */
  clearAllPositions(): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.baseUrl}/clear-all`)
      .pipe(
        catchError(this.handleError)
      );
  }

  // ==================== UTILITY METHODS ====================

  /**
   * Check if the backend is healthy and has data
   * @returns Observable<boolean>
   */
  isHealthy(): Observable<boolean> {
    return this.getDatabaseStats().pipe(
      map(stats => stats.databaseStatus === 'populated'),
      catchError(() => throwError(() => new Error('Backend service unavailable')))
    );
  }

  /**
   * Get formatted account display name
   * @param accountSummary - Account summary object
   * @returns string
   */
  getAccountDisplayName(accountSummary: AccountSummary): string {
    return `${accountSummary.accountIdFake} (${accountSummary.partnerIdFake})`;
  }

  /**
   * Format currency amount
   * @param amount - Amount to format
   * @param currency - Currency code
   * @returns string
   */
  formatCurrencyAmount(amount: number, currency: string): string {
    return new Intl.NumberFormat('en-CH', {
      style: 'currency',
      currency: currency
    }).format(amount);
  }

  /**
   * Get risk level color class for UI
   * @param riskLevel - Risk level
   * @returns string
   */
  getRiskLevelColorClass(riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'): string {
    switch (riskLevel) {
      case 'LOW': return 'text-green-600';
      case 'MEDIUM': return 'text-yellow-600';
      case 'HIGH': return 'text-red-600';
      default: return 'text-gray-600';
    }
  }

  // ==================== PRIVATE METHODS ====================

  /**
   * Handle HTTP errors
   * @param error - HTTP error response
   * @returns Observable<never>
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Server Error: ${error.status} - ${error.message}`;
      
      if (error.error?.message) {
        errorMessage = error.error.message;
      }
    }
    
    console.error('Portfolio Service Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
