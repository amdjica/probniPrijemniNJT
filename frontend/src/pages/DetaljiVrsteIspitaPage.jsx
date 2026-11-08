import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getTermini } from '../api/termini.js';
import { getVrsteIspita } from '../api/vrsteIspita.js';
import RedTermina from '../components/RedTermina.jsx';
import { useAuth } from '../context/AuthContext.jsx';

export default function DetaljiVrsteIspitaPage() {
  const { vrstaId } = useParams();
  const { jeKandidat } = useAuth();
  const [vrsta, setVrsta] = useState(null);
  const [termini, setTermini] = useState([]);

  useEffect(() => {
    getVrsteIspita().then((sve) => setVrsta(sve.find((v) => String(v.id) === vrstaId)));
    getTermini({ vrstaIspitaId: vrstaId })
      .then(setTermini)
      .catch(() => setTermini([]));
  }, [vrstaId]);

  return (
    <div className="stranica stranica--uska">
      <h1>{vrsta ? vrsta.naziv : 'Vrsta ispita'}</h1>
      <p>Pregled dostupnih termina. Za prijavljivanje je potreban nalog kandidata.</p>

      <div className="lista-termina">
        {termini.length === 0 && <p>Trenutno nema otvorenih termina za ovu vrstu ispita.</p>}
        {termini.map((termin) => (
          <RedTermina key={termin.id} termin={termin} samoPregled />
        ))}
      </div>

      {!jeKandidat && (
        <p className="poziv-na-akciju">
          <Link to="/prijavljivanje">Prijavi se</Link> ili <Link to="/registracija">napravi nalog</Link> da bi
          rezervisao termin.
        </p>
      )}
      {jeKandidat && (
        <p className="poziv-na-akciju">
          <Link to="/termini">Idi na stranicu termina</Link> da izabereš i prijaviš se.
        </p>
      )}
    </div>
  );
}
