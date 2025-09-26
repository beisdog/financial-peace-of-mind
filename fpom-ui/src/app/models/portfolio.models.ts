/**
 * TypeScript interfaces for Portfolio Position and Account models
 * These match the DTOs from the Spring Boot backend
 */

export interface PortfolioPosition {
  id?: number;
  partnerIdFake?: string;
  accountIdFake?: string;
  positionCreatedDate?: string;
  fiUnitTypeCode?: string;
  balanceAmount?: number;
  valueAmount?: number;
  tradeAmount?: number;
  valuationDate?: string;
  asOfDate?: string;
  valueCurrency?: string;
  sourceCurrency?: string;
  originalQuantity?: number;
  marketValueAmount?: number;
  fxRate?: number;
  valor?: string;
  isin?: string;
  instrumentNameShort?: string;
  symbolId?: string;
  titleGroupId?: string;
  titleId?: string;
  titleIdDescription?: string;
  symbolIdGpc?: string;
  productDescription?: string;
  productId?: string;
  productIdDescription?: string;
  productClassId?: string;
  productClassDescription?: string;
  productFamilyId?: string;
  productFamilyDescription?: string;
  assetClass?: string;
  assetClassSubtype?: string;
  assetClassDescriptionShort?: string;
  assetClassDescriptionLong?: string;
  uacInstrCatType?: string;
  instrumentId?: string;
  portfolioCurrency?: string;
  portfolioShortName?: string;
  currencyId?: string;
  mandatePricingId?: string;
  mandateProgram?: string;
  mandatePricingNameShort?: string;
  mandatePricingNameLong?: string;
  mandatePricingType?: string;
  mandateProgramSecondary?: string;
  investmentStrategy?: string;
  investmentStrategyName?: string;
  solutionSubtypeId?: string;
  solutionSubtypeNameShort?: string;
  solutionNameShort?: string;
  solutionNameLong?: string;
  mandateType?: string;
  mandateSubtype?: string;
  mandateGroup?: string;
  domicile?: string;
  clientAdvisorIdFake?: number;
}

export interface AccountSummary {
  accountIdFake: string;
  partnerIdFake: string;
  positionCount: number;
  totalValue: number;
  currency: string;
  averagePositionValue: number;
}

export interface PositionSummary {
  id: number;
  instrumentNameShort?: string;
  isin?: string;
  valueAmount?: number;
  valueCurrency?: string;
  assetClassDescriptionShort?: string;
  fxRate?: number;
}

export interface AccountRiskMetrics {
  currencyCount: number;
  assetClassCount: number;
  concentrationRisk: number;
  hasFxExposure: boolean;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
}

export interface AccountDetails {
  accountIdFake: string;
  partnerIdFake: string;
  positionCount: number;
  totalsByCurrency: { [currency: string]: number };
  assetClassBreakdown: { [assetClass: string]: number };
  positions?: PositionSummary[];
  totalValueChf?: number;
  primaryCurrency?: string;
  riskMetrics: AccountRiskMetrics;
}

export interface PortfolioSummary {
  totalPositions: number;
  valueByAssetClass: Array<[string, number]>;
  valueByCurrency: Array<[string, number]>;
  assetClasses: string[];
  currencies: string[];
  mandateTypes: string[];
}

export interface DatabaseStats {
  totalRecords: number;
  databaseStatus: 'populated' | 'empty';
  uniqueAssetClasses: number;
  uniqueCurrencies: number;
  uniqueAccounts: number;
  uniquePartners: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
}

export interface ImportResponse {
  success: boolean;
  message: string;
  importedCount: number;
  existingCountBefore: number;
  totalCountAfter: number;
  clearedExisting: boolean;
}

export interface PagedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
    };
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
}
