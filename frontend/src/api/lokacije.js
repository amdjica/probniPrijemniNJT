import { posaljiZahtev } from './klijent.js';

export function getLokacije() {
  return posaljiZahtev('/lokacije');
}
