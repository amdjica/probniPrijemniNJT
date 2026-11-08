const OSNOVNA_PUTANJA = '/api';

export class ApiGreska extends Error {
  constructor(poruka, status) {
    super(poruka);
    this.name = 'ApiGreska';
    this.status = status;
  }
}

export async function posaljiZahtev(putanja, { metod = 'GET', telo, token } = {}) {
  const zaglavlja = {};
  if (telo !== undefined) {
    zaglavlja['Content-Type'] = 'application/json';
  }
  if (token) {
    zaglavlja.Authorization = `Bearer ${token}`;
  }

  const odgovor = await fetch(`${OSNOVNA_PUTANJA}${putanja}`, {
    method: metod,
    headers: zaglavlja,
    body: telo !== undefined ? JSON.stringify(telo) : undefined,
  });

  if (odgovor.status === 204) {
    return null;
  }

  const tekst = await odgovor.text();
  const podaci = tekst ? JSON.parse(tekst) : null;

  if (!odgovor.ok) {
    const poruka = (podaci && podaci.poruka) || 'Došlo je do neočekivane greške na serveru.';
    throw new ApiGreska(poruka, odgovor.status);
  }

  return podaci;
}

export async function posaljiFormu(putanja, { metod = 'POST', formData, token } = {}) {
  const zaglavlja = {};
  if (token) {
    zaglavlja.Authorization = `Bearer ${token}`;
  }

  const odgovor = await fetch(`${OSNOVNA_PUTANJA}${putanja}`, {
    method: metod,
    headers: zaglavlja,
    body: formData,
  });

  if (!odgovor.ok) {
    const tekst = await odgovor.text();
    const podaci = tekst ? JSON.parse(tekst) : null;
    const poruka = (podaci && podaci.poruka) || 'Došlo je do neočekivane greške na serveru.';
    throw new ApiGreska(poruka, odgovor.status);
  }

  return odgovor.json();
}

export async function preuzmiFajl(putanja, { token } = {}) {
  const zaglavlja = {};
  if (token) {
    zaglavlja.Authorization = `Bearer ${token}`;
  }

  const odgovor = await fetch(`${OSNOVNA_PUTANJA}${putanja}`, { headers: zaglavlja });

  if (!odgovor.ok) {
    let poruka = 'Fajl nije moguće preuzeti.';
    try {
      const podaci = await odgovor.json();
      poruka = podaci?.poruka || poruka;
    } catch {
      // odgovor bez JSON tela (npr. PDF stream) - ostaje podrazumevana poruka
    }
    throw new ApiGreska(poruka, odgovor.status);
  }

  const blob = await odgovor.blob();
  const nazivIzZaglavlja = odgovor.headers.get('Content-Disposition');
  const naziv = nazivIzZaglavlja?.match(/filename="?([^"]+)"?/)?.[1];
  return { blob, naziv };
}
