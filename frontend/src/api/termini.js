import { posaljiZahtev, preuzmiFajl } from './klijent.js';

export function getTermini({ vrstaIspitaId, token } = {}) {
  const upit = vrstaIspitaId ? `?vrstaIspitaId=${vrstaIspitaId}` : '';
  return posaljiZahtev(`/termini${upit}`, { token });
}

export function kreirajTermin(podaci, token) {
  return posaljiZahtev('/termini', { metod: 'POST', telo: podaci, token });
}

export function azurirajTermin(id, podaci, token) {
  return posaljiZahtev(`/termini/${id}`, { metod: 'PUT', telo: podaci, token });
}

export function obrisiTermin(id, token) {
  return posaljiZahtev(`/termini/${id}`, { metod: 'DELETE', token });
}

export function getPrijavljeneKandidate(terminId, token) {
  return posaljiZahtev(`/termini/${terminId}/prijavljeni-kandidati`, { token });
}

export function preuzmiIzvestajTermina(terminId, token) {
  return preuzmiFajl(`/termini/${terminId}/izvestaj`, { token });
}
