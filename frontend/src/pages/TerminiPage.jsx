import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTermini } from '../api/termini.js';
import { kreirajPrijavu } from '../api/prijave.js';
import { getVrsteIspita } from '../api/vrsteIspita.js';
import RedTermina from '../components/RedTermina.jsx';
import { formatujCenu } from '../utils/cena.js';
import { useAuth } from '../context/AuthContext.jsx';

const BROJ_RATA = 5;

export default function TerminiPage() {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [termini, setTermini] = useState([]);
  const [vrsteIspita, setVrsteIspita] = useState([]);
  const [filterVrsta, setFilterVrsta] = useState('');
  const [izabraniIdjevi, setIzabraniIdjevi] = useState([]);
  const [nacinPlacanja, setNacinPlacanja] = useState('JEDNOKRATNO');
  const [greska, setGreska] = useState(null);
  const [saljeSe, setSaljeSe] = useState(false);

  useEffect(() => {
    getVrsteIspita()
      .then(setVrsteIspita)
      .catch(() => {});
  }, []);

  useEffect(() => {
    getTermini({ vrstaIspitaId: filterVrsta || undefined, token })
      .then(setTermini)
      .catch((e) => setGreska(e.message));
  }, [filterVrsta, token]);

  function preklopiIzbor(termin) {
    setIzabraniIdjevi((prethodno) =>
      prethodno.includes(termin.id) ? prethodno.filter((id) => id !== termin.id) : [...prethodno, termin.id],
    );
  }

  const izabraniTermini = termini.filter((t) => izabraniIdjevi.includes(t.id));
  const ukupnaCena = izabraniTermini.reduce((zbir, t) => zbir + (t.cena ?? 0), 0);
  const iznosRate = ukupnaCena / BROJ_RATA;

  async function posaljiPrijavu() {
    setGreska(null);
    setSaljeSe(true);
    try {
      await kreirajPrijavu(izabraniIdjevi, nacinPlacanja, token);
      navigate('/moj-pregled');
    } catch (err) {
      setGreska(err.message);
    } finally {
      setSaljeSe(false);
    }
  }

  return (
    <div className="stranica">
      <h1>Prijava na termin probnog ispita</h1>

      <section className="blok-nacin-placanja">
        <h2>1. Način plaćanja</h2>
        <div className="opcije-placanja">
          <button
            type="button"
            className={`opcija-placanja ${nacinPlacanja === 'JEDNOKRATNO' ? 'opcija-placanja--aktivna' : ''}`}
            onClick={() => setNacinPlacanja('JEDNOKRATNO')}
          >
            <strong>Jednokratno</strong>
            <span>Ceo iznos u jednoj uplati</span>
          </button>
          <button
            type="button"
            className={`opcija-placanja ${nacinPlacanja === 'RATE' ? 'opcija-placanja--aktivna' : ''}`}
            onClick={() => setNacinPlacanja('RATE')}
          >
            <strong>Na {BROJ_RATA} rata</strong>
            <span>Iznos se automatski deli na {BROJ_RATA} jednakih rata</span>
          </button>
        </div>

        <div className="kartica-placanja-info">
          {izabraniTermini.length === 0 ? (
            <p>Izaberi termine ispod da bi video obračun cene.</p>
          ) : nacinPlacanja === 'RATE' ? (
            <p>
              Ukupna cena prijave je <strong>{formatujCenu(ukupnaCena)}</strong>. Plaćaš u <strong>{BROJ_RATA} rata</strong>,
              svaka u iznosu od <strong>{formatujCenu(iznosRate)}</strong>.
            </p>
          ) : (
            <p>
              Ukupna cena prijave je <strong>{formatujCenu(ukupnaCena)}</strong>, plaća se u celosti odjednom.
            </p>
          )}
        </div>
      </section>

      <section>
        <h2>2. Izbor termina</h2>
        <div className="filter-traka">
          <label>
            Vrsta ispita
            <select value={filterVrsta} onChange={(dogadjaj) => setFilterVrsta(dogadjaj.target.value)}>
              <option value="">Sve vrste</option>
              {vrsteIspita.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.naziv}
                </option>
              ))}
            </select>
          </label>
        </div>

        {greska && <p className="poruka-greske">{greska}</p>}

        <div className="raspored-prijave">
          <aside className="panel-izbora">
            <h3>Izabrani termini</h3>
            {izabraniTermini.length === 0 && <p>Još ništa nije izabrano.</p>}
            <ul>
              {izabraniTermini.map((t) => (
                <li key={t.id}>
                  {t.vrstaIspitaNaziv} — {t.datum}
                </li>
              ))}
            </ul>
            <p className="panel-izbora__ukupno">
              Ukupno: <strong>{formatujCenu(ukupnaCena)}</strong>
            </p>
            <button
              type="button"
              className="dugme dugme--primarno dugme--puno"
              disabled={izabraniIdjevi.length === 0 || saljeSe}
              onClick={posaljiPrijavu}
            >
              {saljeSe ? 'Slanje prijave...' : 'Podnesi prijavu'}
            </button>
          </aside>

          <div className="spisak-termina">
            {termini.length === 0 && <p>Trenutno nema dostupnih termina.</p>}
            {termini.map((termin) => (
              <RedTermina
                key={termin.id}
                termin={termin}
                izabran={izabraniIdjevi.includes(termin.id)}
                onPromena={preklopiIzbor}
              />
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
