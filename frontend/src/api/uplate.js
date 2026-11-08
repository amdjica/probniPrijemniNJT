import { posaljiZahtev, posaljiFormu, preuzmiFajl } from './klijent.js';

export function dodajUplatu(prijavaId, { iznos, datumUplate, uplatnica }, token) {
  const formData = new FormData();
  formData.append('iznos', iznos);
  if (datumUplate) {
    formData.append('datumUplate', datumUplate);
  }
  formData.append('uplatnica', uplatnica);
  return posaljiFormu(`/prijave/${prijavaId}/uplate`, { formData, token });
}

export function getUplateZaPrijavu(prijavaId, token) {
  return posaljiZahtev(`/prijave/${prijavaId}/uplate`, { token });
}

export function preuzmiUplatnicu(prijavaId, uplataId, token) {
  return preuzmiFajl(`/prijave/${prijavaId}/uplate/${uplataId}/uplatnica`, { token });
}
