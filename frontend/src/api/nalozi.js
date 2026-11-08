import { posaljiZahtev } from './klijent.js';

export function prijaviKandidata(email, password) {
  return posaljiZahtev('/nalozi/kandidati/prijavljivanje', { metod: 'POST', telo: { email, password } });
}

export function pokreniRegistracijuKandidata(podaci) {
  return posaljiZahtev('/nalozi/kandidati/registracija/pocni', { metod: 'POST', telo: podaci });
}

export function potvrdiPrivremenuLozinku(email, privremenaLozinka) {
  return posaljiZahtev('/nalozi/kandidati/registracija/potvrdi', {
    metod: 'POST',
    telo: { email, privremenaLozinka },
  });
}

export function postaviTrajnuLozinku(email, privremenaLozinka, novaLozinka) {
  return posaljiZahtev('/nalozi/kandidati/registracija/postavi-lozinku', {
    metod: 'POST',
    telo: { email, privremenaLozinka, novaLozinka },
  });
}

export function prijaviAdministratora(email, password) {
  return posaljiZahtev('/nalozi/administratori/prijavljivanje', { metod: 'POST', telo: { email, password } });
}
