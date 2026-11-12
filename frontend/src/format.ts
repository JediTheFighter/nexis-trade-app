const usd = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });
const px = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
  minimumFractionDigits: 2,
  maximumFractionDigits: 4,
});

export function money(value: number) {
  return usd.format(value);
}

export function price(value: number) {
  return px.format(value);
}

export function when(iso: string) {
  return new Intl.DateTimeFormat("en-US", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  }).format(new Date(iso));
}

export function tone(value: number) {
  if (value > 0) {
    return "up";
  }
  if (value < 0) {
    return "down";
  }
  return "flat";
}
