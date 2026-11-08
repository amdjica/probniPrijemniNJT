import { posaljiZahtev } from './klijent.js';

export function kreirajPrijavu(terminiId, nacinPlacanja, token) {
  return posaljiZahtev('/prijave', { metod: 'POST', telo: { terminiId, nacinPlacanja }, token });
}

export function getMojePrijave(token) {
  return posaljiZahtev('/prijave/moje', { token });
}

export function getDetaljiPrijave(id, token) {
  return posaljiZahtev(`/prijave/${id}`, { token });
}

export function pretraziPrijave(filteri, token) {
  const params = new URLSearchParams();
  Object.entries(filteri || {}).forEach(([kljuc, vrednost]) => {
    if (vrednost !== undefined && vrednost !== null && vrednost !== '') {
      params.set(kljuc, vrednost);
    }
  });
  const upit = params.toString() ? `?${params.toString()}` : '';
  return posaljiZahtev(`/prijave${upit}`, { token });
}

export function odobriPrijavu(id, token) {
  return posaljiZahtev(`/prijave/${id}/odobri`, { metod: 'PUT', token });
}

export function odbijPrijavu(id, napomena, token) {
  return posaljiZahtev(`/prijave/${id}/odbij`, { metod: 'PUT', telo: { napomena }, token });
}

export function getStatistikaPrijava(token) {
  return posaljiZahtev('/prijave/statistika', { token });
}
