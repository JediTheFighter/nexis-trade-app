export type Instrument = {
  symbol: string;
  name: string;
  price: number;
  asOf: string;
};

export type Position = {
  symbol: string;
  name: string;
  quantity: number;
  averageCost: number;
  lastPrice: number;
  marketValue: number;
  unrealizedPnl: number;
};

export type Portfolio = {
  cash: number;
  equity: number;
  unrealizedPnl: number;
  positions: Position[];
};

export type Order = {
  id: string;
  symbol: string;
  side: "BUY" | "SELL";
  quantity: number;
  status: string;
  fillPrice: number;
  realizedPnl: number | null;
  createdAt: string;
};

export type AuthResult = {
  token: string;
  email: string;
};
