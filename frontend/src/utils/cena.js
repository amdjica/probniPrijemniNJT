const FORMATER = new Intl.NumberFormat('sr-RS', { maximumFractionDigits: 2 });

export function formatujCenu(iznos) {
  if (iznos === null || iznos === undefined) {
    return '—';
  }
  return `${FORMATER.format(iznos)} RSD`;
}
