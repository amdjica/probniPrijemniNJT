import { posaljiZahtev } from './klijent.js';

export function getVrsteIspita() {
  return posaljiZahtev('/vrste-ispita');
}
