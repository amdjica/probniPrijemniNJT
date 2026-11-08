const STATUSI = {
  NA_CEKANJU: { oznaka: 'Na čekanju uplate', ton: 'upozorenje' },
  ODOBRENA: { oznaka: 'Odobrena', ton: 'uspeh' },
  ODBIJENA: { oznaka: 'Odbijena', ton: 'kriticno' },
};

export function infoOStatusu(status) {
  return STATUSI[status] ?? { oznaka: status, ton: 'neutralno' };
}
