import { posaljiZahtev } from './klijent.js';

export function getAktivneCenovnike(token) {
  return posaljiZahtev('/cenovnici', { token });
}

export function azurirajCenu(vrstaIspitaId, podaci, token) {
  return posaljiZahtev(`/cenovnici/${vrstaIspitaId}`, { metod: 'PUT', telo: podaci, token });
}
