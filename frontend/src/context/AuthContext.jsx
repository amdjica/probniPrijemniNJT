import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { prijaviAdministratora, prijaviKandidata } from '../api/nalozi.js';
import { ApiGreska } from '../api/klijent.js';

const KLJUC_SKLADISTA = 'njtprobni.nalog';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [korisnik, setKorisnik] = useState(() => {
    const sacuvano = localStorage.getItem(KLJUC_SKLADISTA);
    return sacuvano ? JSON.parse(sacuvano) : null;
  });
  const [ucitavanje, setUcitavanje] = useState(false);

  useEffect(() => {
    if (korisnik) {
      localStorage.setItem(KLJUC_SKLADISTA, JSON.stringify(korisnik));
    } else {
      localStorage.removeItem(KLJUC_SKLADISTA);
    }
  }, [korisnik]);

  async function prijaviSe(email, password) {
    setUcitavanje(true);
    try {
      const odgovor = await prijaviKandidata(email, password);
      setKorisnik(odgovor);
      return odgovor;
    } catch (greska) {
      if (greska instanceof ApiGreska && greska.status === 401) {
        const odgovorAdmin = await prijaviAdministratora(email, password);
        setKorisnik(odgovorAdmin);
        return odgovorAdmin;
      }
      throw greska;
    } finally {
      setUcitavanje(false);
    }
  }

  function postaviKorisnika(odgovor) {
    setKorisnik(odgovor);
  }

  function odjaviSe() {
    setKorisnik(null);
  }

  const vrednost = useMemo(
    () => ({
      korisnik,
      token: korisnik?.token ?? null,
      jeUlogovan: !!korisnik,
      jeKandidat: korisnik?.tipNaloga === 'KANDIDAT',
      jeAdmin: korisnik?.tipNaloga === 'ADMINISTRATOR',
      ucitavanje,
      prijaviSe,
      postaviKorisnika,
      odjaviSe,
    }),
    [korisnik, ucitavanje],
  );

  return <AuthContext.Provider value={vrednost}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const kontekst = useContext(AuthContext);
  if (!kontekst) {
    throw new Error('useAuth mora biti korišćen unutar AuthProvider-a.');
  }
  return kontekst;
}
