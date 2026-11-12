import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import type { FormEvent } from "react";
import { ApiError, fetchInstruments, fetchOrders, fetchPortfolio, placeOrder } from "../api";
import { useAuth } from "../auth";
import { money, price, tone, when } from "../format";

export function DeskPage() {
  const { session, signOut } = useAuth();
  const queryClient = useQueryClient();
  const [symbol, setSymbol] = useState("AAPL");
  const [side, setSide] = useState<"BUY" | "SELL">("BUY");
  const [quantity, setQuantity] = useState("1");
  const [ticketError, setTicketError] = useState<string | null>(null);

  const instruments = useQuery({
    queryKey: ["instruments"],
    queryFn: fetchInstruments,
    refetchInterval: 5000,
  });
  const portfolio = useQuery({
    queryKey: ["portfolio"],
    queryFn: fetchPortfolio,
    refetchInterval: 5000,
  });
  const orders = useQuery({
    queryKey: ["orders"],
    queryFn: fetchOrders,
    refetchInterval: 5000,
  });

  const order = useMutation({
    mutationFn: (input: { symbol: string; side: "BUY" | "SELL"; quantity: number }) =>
      placeOrder(input.symbol, input.side, input.quantity),
    onSuccess: async () => {
      setTicketError(null);
      await queryClient.invalidateQueries({ queryKey: ["portfolio"] });
      await queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
    onError: (caught) => {
      setTicketError(caught instanceof ApiError ? caught.message : "Order failed.");
    },
  });

  const selected = instruments.data?.find((item) => item.symbol === symbol) ?? instruments.data?.[0];
  const sharesHeld = portfolio.data?.positions.find((position) => position.symbol === selected?.symbol)?.quantity ?? 0;

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    const size = Number(quantity);
    if (!Number.isInteger(size) || size < 1) {
      setTicketError("Quantity has to be a whole number of shares.");
      return;
    }
    const traded = selected?.symbol ?? symbol;
    setSymbol(traded);
    order.mutate({ symbol: traded, side, quantity: size });
  }

  return (
    <div className="desk">
      <header className="topbar">
        <div>
          <p className="eyebrow">Nexis paper desk</p>
          <p className="who">{session?.email}</p>
        </div>
        <dl className="balances">
          <div>
            <dt>Cash</dt>
            <dd>{portfolio.data ? money(portfolio.data.cash) : "—"}</dd>
          </div>
          <div>
            <dt>Equity</dt>
            <dd>{portfolio.data ? money(portfolio.data.equity) : "—"}</dd>
          </div>
          <div>
            <dt>Open P&amp;L</dt>
            <dd className={portfolio.data ? tone(portfolio.data.unrealizedPnl) : "flat"}>
              {portfolio.data ? money(portfolio.data.unrealizedPnl) : "—"}
            </dd>
          </div>
        </dl>
        <button className="ghost" type="button" onClick={signOut}>
          Sign out
        </button>
      </header>

      <div className="workspace">
        <section className="panel market">
          <header className="panel-head">
            <h2>Market</h2>
            <p>Prices move on the server every few seconds.</p>
          </header>
          {instruments.isError ? <p className="form-error">The market feed is unavailable.</p> : null}
          <ul className="symbols">
            {(instruments.data ?? []).map((item) => (
              <li key={item.symbol}>
                <button
                  type="button"
                  className={item.symbol === selected?.symbol ? "symbol selected" : "symbol"}
                  onClick={() => {
                    setSymbol(item.symbol);
                    setTicketError(null);
                  }}
                >
                  <span>
                    <strong>{item.symbol}</strong>
                    <small>{item.name}</small>
                  </span>
                  <span className="px">{price(item.price)}</span>
                </button>
              </li>
            ))}
          </ul>
        </section>

        <section className="panel ticket">
          <header className="panel-head">
            <h2>{selected ? selected.symbol : "Ticket"}</h2>
            <p>{selected ? `${selected.name} · ${price(selected.price)} · you hold ${sharesHeld}` : "Loading quotes"}</p>
          </header>
          <form onSubmit={onSubmit}>
            <div className="sides" role="group" aria-label="Order side">
              <button type="button" className={side === "BUY" ? "side buy on" : "side buy"} onClick={() => setSide("BUY")}>
                Buy
              </button>
              <button type="button" className={side === "SELL" ? "side sell on" : "side sell"} onClick={() => setSide("SELL")}>
                Sell
              </button>
            </div>
            <label>
              Shares
              <input
                inputMode="numeric"
                value={quantity}
                onChange={(event) => setQuantity(event.target.value)}
                required
              />
            </label>
            {ticketError ? <p className="form-error">{ticketError}</p> : null}
            <button className="primary" type="submit" disabled={order.isPending || !selected}>
              {order.isPending ? "Sending..." : `${side === "BUY" ? "Buy" : "Sell"} ${selected?.symbol ?? ""}`}
            </button>
          </form>
        </section>

        <section className="panel positions">
          <header className="panel-head">
            <h2>Positions</h2>
            <p>Average cost updates on each buy. A full sell closes the row.</p>
          </header>
          {portfolio.data && portfolio.data.positions.length === 0 ? (
            <p className="empty">No open positions. A market buy fills at the price on the ticket.</p>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Symbol</th>
                    <th>Qty</th>
                    <th>Avg</th>
                    <th>Last</th>
                    <th>Value</th>
                    <th>P&amp;L</th>
                  </tr>
                </thead>
                <tbody>
                  {portfolio.data?.positions.map((position) => (
                    <tr key={position.symbol}>
                      <td>{position.symbol}</td>
                      <td>{position.quantity}</td>
                      <td>{price(position.averageCost)}</td>
                      <td>{price(position.lastPrice)}</td>
                      <td>{money(position.marketValue)}</td>
                      <td className={tone(position.unrealizedPnl)}>{money(position.unrealizedPnl)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="panel blotter">
          <header className="panel-head">
            <h2>Fills</h2>
            <p>Market orders only. Status is filled as soon as the API accepts them.</p>
          </header>
          {orders.data && orders.data.length === 0 ? (
            <p className="empty">Fills land here after the first order.</p>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Time</th>
                    <th>Side</th>
                    <th>Symbol</th>
                    <th>Qty</th>
                    <th>Fill</th>
                    <th>Realized</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.data?.map((fill) => (
                    <tr key={fill.id}>
                      <td>{when(fill.createdAt)}</td>
                      <td className={fill.side === "BUY" ? "up" : "down"}>{fill.side}</td>
                      <td>{fill.symbol}</td>
                      <td>{fill.quantity}</td>
                      <td>{price(fill.fillPrice)}</td>
                      <td className={fill.realizedPnl === null ? "flat" : tone(fill.realizedPnl)}>
                        {fill.realizedPnl === null ? "—" : money(fill.realizedPnl)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
