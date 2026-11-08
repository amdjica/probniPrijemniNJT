import { useCallback, useEffect, useState } from 'react';
import { getMojePrijave } from '../api/prijave.js';
import { dodajUplatu } from '../api/uplate.js';
import KarticaPrijave from '../components/KarticaPrijave.jsx';
import { useAuth } from '../context/AuthContext.jsx';

export default function MojPregledPage() {
  const { token } = useAuth();
  const [prijave, setPrijave] = useState([]);
  const [greska, setGreska] = useState(null);
  const [ucitano, setUcitano] = useState(false);

  const ucitajPrijave = useCallback(() => {
    getMojePrijave(token)
      .then((podaci) => {
        setPrijave(podaci);
        setUcitano(true);
      })
      .catch((e) => setGreska(e.message));
  }, [token]);

  useEffect(() => {
    ucitajPrijave();
  }, [ucitajPrijave]);

  async function naDodavanjeUplate(prijavaId, podaciUplate) {
    await dodajUplatu(prijavaId, podaciUplate, token);
    ucitajPrijave();
  }

  return (
    <div className="stranica">
      <h1>Moj pregled prijava</h1>
      {greska && <p className="poruka-greske">{greska}</p>}
      {ucitano && prijave.length === 0 && (
        <p>Još uvek nemaš nijednu prijavu. Idi na stranicu termina da se prijaviš.</p>
      )}
      <div className="lista-prijava">
        {prijave.map((prijava) => (
          <KarticaPrijave key={prijava.id} prijava={prijava} naDodavanjeUplate={naDodavanjeUplate} />
        ))}
      </div>
    </div>
  );
}
