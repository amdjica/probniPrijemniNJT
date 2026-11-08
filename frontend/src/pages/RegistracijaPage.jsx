import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  pokreniRegistracijuKandidata,
  potvrdiPrivremenuLozinku,
  postaviTrajnuLozinku,
} from '../api/nalozi.js';

const POCETNI_PODACI = { ime: '', prezime: '', telefon: '', email: '' };

export default function RegistracijaPage() {
  const [korak, setKorak] = useState(1);
  const [podaci, setPodaci] = useState(POCETNI_PODACI);
  const [privremenaLozinka, setPrivremenaLozinka] = useState('');
  const [novaLozinka, setNovaLozinka] = useState('');
  const [potvrdaLozinke, setPotvrdaLozinke] = useState('');
  const [poruka, setPoruka] = useState(null);
  const [greska, setGreska] = useState(null);
  const [saljeSe, setSaljeSe] = useState(false);

  function izmeni(polje, vrednost) {
    setPodaci((prethodno) => ({ ...prethodno, [polje]: vrednost }));
  }

  async function posaljiKorak1(dogadjaj) {
    dogadjaj.preventDefault();
    setGreska(null);
    setSaljeSe(true);
    try {
      const odgovor = await pokreniRegistracijuKandidata(podaci);
      setPoruka(odgovor.poruka);
      setKorak(2);
    } catch (err) {
      setGreska(err.message);
    } finally {
      setSaljeSe(false);
    }
  }

  async function posaljiKorak2(dogadjaj) {
    dogadjaj.preventDefault();
    setGreska(null);
    setSaljeSe(true);
    try {
      const odgovor = await potvrdiPrivremenuLozinku(podaci.email, privremenaLozinka);
      setPoruka(odgovor.poruka);
      setKorak(3);
    } catch (err) {
      setGreska(err.message);
    } finally {
      setSaljeSe(false);
    }
  }

  async function posaljiKorak3(dogadjaj) {
    dogadjaj.preventDefault();
    setGreska(null);
    if (novaLozinka !== potvrdaLozinke) {
      setGreska('Unete lozinke se ne poklapaju. Pokušajte ponovo.');
      return;
    }
    setSaljeSe(true);
    try {
      const odgovor = await postaviTrajnuLozinku(podaci.email, privremenaLozinka, novaLozinka);
      setPoruka(odgovor.poruka);
      setKorak(4);
    } catch (err) {
      setGreska(err.message);
    } finally {
      setSaljeSe(false);
    }
  }

  return (
    <div className="stranica stranica--forma">
      <div className="kartica-forme">
        <h1>Registracija kandidata</h1>

        <ol className="koraci-registracije">
          <li className={korak >= 1 ? 'je-aktivan' : ''}>1. Podaci</li>
          <li className={korak >= 2 ? 'je-aktivan' : ''}>2. Privremena lozinka</li>
          <li className={korak >= 3 ? 'je-aktivan' : ''}>3. Nova lozinka</li>
        </ol>

        {korak === 1 && (
          <form onSubmit={posaljiKorak1}>
            <p className="kartica-forme__opis">
              Unesite osnovne podatke. Lozinku ne unosite sada - poslaćemo vam privremenu lozinku na email.
            </p>
            <div className="forma-dva-stupca">
              <label>
                Ime
                <input value={podaci.ime} onChange={(d) => izmeni('ime', d.target.value)} required />
              </label>
              <label>
                Prezime
                <input value={podaci.prezime} onChange={(d) => izmeni('prezime', d.target.value)} required />
              </label>
            </div>
            <label>
              Telefon
              <input value={podaci.telefon} onChange={(d) => izmeni('telefon', d.target.value)} />
            </label>
            <label>
              Email adresa
              <input type="email" value={podaci.email} onChange={(d) => izmeni('email', d.target.value)} required />
            </label>
            {greska && <p className="poruka-greske">{greska}</p>}
            <button type="submit" className="dugme dugme--primarno dugme--puno" disabled={saljeSe}>
              {saljeSe ? 'Slanje...' : 'Pošalji privremenu lozinku na email'}
            </button>
          </form>
        )}

        {korak === 2 && (
          <form onSubmit={posaljiKorak2}>
            {poruka && <p className="kartica-forme__poruka-uspeha">{poruka}</p>}
            <p className="kartica-forme__opis">
              Proverite prijemno sanduče na adresi <strong>{podaci.email}</strong> i unesite dobijenu privremenu
              lozinku.
            </p>
            <label>
              Privremena lozinka
              <input
                value={privremenaLozinka}
                onChange={(d) => setPrivremenaLozinka(d.target.value)}
                required
                autoFocus
              />
            </label>
            {greska && <p className="poruka-greske">{greska}</p>}
            <button type="submit" className="dugme dugme--primarno dugme--puno" disabled={saljeSe}>
              {saljeSe ? 'Provera...' : 'Potvrdi privremenu lozinku'}
            </button>
          </form>
        )}

        {korak === 3 && (
          <form onSubmit={posaljiKorak3}>
            <p className="kartica-forme__opis">Postavite trajnu lozinku koju ćete koristiti ubuduće za prijavu.</p>
            <label>
              Nova lozinka
              <input
                type="password"
                value={novaLozinka}
                onChange={(d) => setNovaLozinka(d.target.value)}
                required
                minLength={6}
                autoFocus
              />
            </label>
            <label>
              Ponovite novu lozinku
              <input
                type="password"
                value={potvrdaLozinke}
                onChange={(d) => setPotvrdaLozinke(d.target.value)}
                required
                minLength={6}
              />
            </label>
            {greska && <p className="poruka-greske">{greska}</p>}
            <button type="submit" className="dugme dugme--primarno dugme--puno" disabled={saljeSe}>
              {saljeSe ? 'Čuvanje...' : 'Završi registraciju'}
            </button>
          </form>
        )}

        {korak === 4 && (
          <div className="kartica-forme__zavrseno">
            <p className="kartica-forme__poruka-uspeha">{poruka}</p>
            <Link to="/prijavljivanje" className="dugme dugme--primarno dugme--puno">
              Idi na prijavu
            </Link>
          </div>
        )}

        {korak < 4 && (
          <p className="kartica-forme__podnozje">
            Već imaš nalog? <Link to="/prijavljivanje">Prijavi se</Link>
          </p>
        )}
      </div>
    </div>
  );
}
