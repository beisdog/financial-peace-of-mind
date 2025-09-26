import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PortfolioService } from './portfolio.service';
import { AccountSummary, PortfolioPosition, AccountDetails } from '../models/portfolio.models';

describe('PortfolioService', () => {
  let service: PortfolioService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080/api/portfolio-positions';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PortfolioService]
    });
    service = TestBed.inject(PortfolioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('Account Operations', () => {
    it('should fetch all accounts', () => {
      const mockAccounts: AccountSummary[] = [
        {
          accountIdFake: 'ACC123',
          partnerIdFake: 'PART123',
          positionCount: 5,
          totalValue: 100000,
          currency: 'CHF',
          averagePositionValue: 20000
        }
      ];

      service.getAllAccounts().subscribe(accounts => {
        expect(accounts).toEqual(mockAccounts);
        expect(accounts.length).toBe(1);
        expect(accounts[0].accountIdFake).toBe('ACC123');
      });

      const req = httpMock.expectOne(`${baseUrl}/accounts`);
      expect(req.request.method).toBe('GET');
      req.flush(mockAccounts);
    });

    it('should fetch account details', () => {
      const mockAccountDetails: AccountDetails = {
        accountIdFake: 'ACC123',
        partnerIdFake: 'PART123',
        positionCount: 5,
        totalsByCurrency: { 'CHF': 50000, 'USD': 30000 },
        assetClassBreakdown: { 'Equities': 3, 'Bonds': 2 },
        primaryCurrency: 'CHF',
        riskMetrics: {
          currencyCount: 2,
          assetClassCount: 2,
          concentrationRisk: 25.5,
          hasFxExposure: true,
          riskLevel: 'MEDIUM'
        }
      };

      service.getAccountDetails('ACC123').subscribe(details => {
        expect(details).toEqual(mockAccountDetails);
        expect(details.accountIdFake).toBe('ACC123');
        expect(details.riskMetrics.riskLevel).toBe('MEDIUM');
      });

      const req = httpMock.expectOne(`${baseUrl}/accounts/ACC123/details`);
      expect(req.request.method).toBe('GET');
      req.flush(mockAccountDetails);
    });

    it('should fetch positions by account ID', () => {
      const mockPositions: PortfolioPosition[] = [
        {
          id: 1,
          partnerIdFake: 'PART123',
          accountIdFake: 'ACC123',
          valueAmount: 25000,
          valueCurrency: 'CHF',
          instrumentNameShort: 'Test Bond',
          assetClassDescriptionShort: 'Bonds'
        }
      ];

      service.getPositionsByAccountId('ACC123').subscribe(positions => {
        expect(positions).toEqual(mockPositions);
        expect(positions.length).toBe(1);
        expect(positions[0].accountIdFake).toBe('ACC123');
      });

      const req = httpMock.expectOne(`${baseUrl}/account/ACC123`);
      expect(req.request.method).toBe('GET');
      req.flush(mockPositions);
    });
  });

  describe('Position Operations', () => {
    it('should create a new position', () => {
      const newPosition: PortfolioPosition = {
        partnerIdFake: 'PART123',
        accountIdFake: 'ACC123',
        valueAmount: 30000,
        valueCurrency: 'CHF',
        instrumentNameShort: 'New Position'
      };

      const createdPosition: PortfolioPosition = {
        ...newPosition,
        id: 123
      };

      service.createPosition(newPosition).subscribe(position => {
        expect(position).toEqual(createdPosition);
        expect(position.id).toBe(123);
      });

      const req = httpMock.expectOne(baseUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newPosition);
      req.flush(createdPosition);
    });

    it('should search positions', () => {
      const mockPositions: PortfolioPosition[] = [
        {
          id: 1,
          instrumentNameShort: 'Test Instrument',
          valueAmount: 15000,
          valueCurrency: 'CHF'
        }
      ];

      service.searchPositions('Test').subscribe(positions => {
        expect(positions).toEqual(mockPositions);
        expect(positions[0].instrumentNameShort).toContain('Test');
      });

      const req = httpMock.expectOne(`${baseUrl}/search?instrumentName=Test`);
      expect(req.request.method).toBe('GET');
      req.flush(mockPositions);
    });
  });

  describe('Utility Methods', () => {
    it('should format currency amount', () => {
      const formatted = service.formatCurrencyAmount(1234.56, 'CHF');
      expect(formatted).toContain('1,234.56');
      expect(formatted).toContain('CHF');
    });

    it('should get risk level color class', () => {
      expect(service.getRiskLevelColorClass('LOW')).toBe('text-green-600');
      expect(service.getRiskLevelColorClass('MEDIUM')).toBe('text-yellow-600');
      expect(service.getRiskLevelColorClass('HIGH')).toBe('text-red-600');
    });

    it('should get account display name', () => {
      const account: AccountSummary = {
        accountIdFake: 'ACC123',
        partnerIdFake: 'PART456',
        positionCount: 5,
        totalValue: 100000,
        currency: 'CHF',
        averagePositionValue: 20000
      };

      const displayName = service.getAccountDisplayName(account);
      expect(displayName).toBe('ACC123 (PART456)');
    });
  });

  describe('Error Handling', () => {
    it('should handle HTTP errors', () => {
      service.getAllAccounts().subscribe({
        next: () => fail('Should have failed'),
        error: (error) => {
          expect(error.message).toContain('Server Error');
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/accounts`);
      req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
    });
  });
});
