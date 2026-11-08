// Paleta validirana dataviz alatom (CVD-bezbedan kategorijalni redosled + fiksne statusne boje).
export const BOJE_STATUSA = {
  NA_CEKANJU: '#fab219',
  ODOBRENA: '#0ca30c',
  ODBIJENA: '#d03b3b',
};

export const KATEGORIJALNE_BOJE = ['#2a78d6', '#1baf7a', '#eda100', '#4a3aa7', '#eb6834', '#e34948'];

export const BOJE_GRAFIKA = {
  povrsina: '#fcfcfb',
  tekstPrimarni: '#0b0b0b',
  tekstSekundarni: '#52514e',
  priguseno: '#898781',
  linijaMreze: '#e1e0d9',
  osnovnaLinija: '#c3c2b7',
};

export function bojaZaIndeks(indeks) {
  return KATEGORIJALNE_BOJE[indeks % KATEGORIJALNE_BOJE.length];
}
