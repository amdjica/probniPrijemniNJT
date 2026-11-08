import { useState } from 'react';
import OznakaStatusa from '../../components/OznakaStatusa.jsx';
import PregledUplatnice from '../../components/PregledUplatnice.jsx';
import { formatujCenu } from '../../utils/cena.js';
import { odobriPrijavu, odbijPrijavu } from '../../api/prijave.js';
import { useAuth } from '../../context/AuthContext.jsx';

export default function AdminRedPrijave({ prijava, naPromenu }) {
  const { token } = useAuth();
  const [prosireno, setProsireno] = useState(false);
  const [napomena, setNapomena] = useState('');
  const [prikaziOdbijanje, setPrikaziOdbijanje] = useState(false);
  const [poruka, setPoruka] = useState(null);
  const [saljeSe, setSaljeSe] = useState(false);
  const [uplataZaPregled, setUplataZaPregled] = useState(null);

  async function odobri() {
    setPoruka(null);
    setSaljeSe(true);
    try {
      await odobriPrijavu(prijava.id, token);
      naPromenu();
    } catch (err) {
      setPoruka(err.message);
    } finally {
      setSaljeSe(false);
    }
  }

  async function odbij(dogadjaj) {
    dogadjaj.preventDefault();
    if (!napomena.trim()) {
      setPoruka('Razlog odbijanja je obavezan.');
      return;
    }
    setPoruka(null);
    setSaljeSe(true);
    try {
      await odbijPrijavu(prijava.id, napomena, token);
      setPrikaziOdbijanje(false);
      naPromenu();
    } catch (err) {
      setPoruka(err.message);
    } finally {
      setSaljeSe(false);
    }
  }

  return (
    <article className="kartica-prijave-admin">
      <div className="kartica-prijave-admin__zaglavlje">
        <div className="kartica-prijave-admin__kandidat">
          <strong>
            {prijava.kandidatIme} {prijava.kandidatPrezime}
          </strong>
          <span>{prijava.kandidatEmail}</span>
        </div>
        <div className="kartica-prijave-admin__meta">
          <span>Prijava #{prijava.id}</span>
          <span>{prijava.datumPrijave}</span>
        </div>
        <OznakaStatusa status={prijava.status} />
        <button
          type="button"
          className="dugme dugme--sekundarno kartica-prijave-admin__prekidac"
          onClick={() => setProsireno((v) => !v)}
        >
          {prosireno ? 'Sakrij detalje' : 'Prikaži detalje'}
        </button>
      </div>

      {prosireno && (
        <div className="kartica-prijave-admin__detalji">
          <ul className="kartica-prijave__termini">
            {prijava.termini.map((t) => (
              <li key={t.id}>
                {t.vrstaIspitaNaziv} — {t.datum} u {t.vremePocetka}, {t.lokacijaNaziv}
              </li>
            ))}
          </ul>

          <div className="blok-placanja-prijave">
            <p className="blok-placanja-prijave__nacin">
              {prijava.nacinPlacanja === 'RATE'
                ? `Plaćanje na ${prijava.brojRata} rata, po ${formatujCenu(prijava.iznosJedneRate)}`
                : 'Jednokratno plaćanje'}
            </p>
            <div className="blok-placanja-prijave__brojevi">
              <span>
                Ukupno <strong>{formatujCenu(prijava.cena)}</strong>
              </span>
              <span>
                Uplaćeno <strong>{formatujCenu(prijava.uplaceno)}</strong>
              </span>
              <span>
                Preostalo <strong>{formatujCenu(prijava.preostalo)}</strong>
              </span>
            </div>
          </div>

          {prijava.uplate.length > 0 && (
            <ul className="kartica-prijave__uplate">
              {prijava.uplate.map((uplata) => (
                <li key={uplata.id}>
                  <span>
                    {uplata.datumUplate} — {formatujCenu(uplata.iznos)}
                  </span>
                  {uplata.imaUplatnicu ? (
                    <button type="button" className="dugme-link" onClick={() => setUplataZaPregled(uplata.id)}>
                      Pogledaj uplatnicu
                    </button>
                  ) : (
                    <span className="oznaka-bez-uplatnice">Bez priloga</span>
                  )}
                </li>
              ))}
            </ul>
          )}

          {prijava.napomena && <p>Napomena: {prijava.napomena}</p>}

          {poruka && <p className="poruka-greske">{poruka}</p>}

          <div className="red-prijave__akcije">
            <button
              type="button"
              className="dugme dugme--primarno"
              disabled={!prijava.mozeDaSeOdobri || saljeSe}
              onClick={odobri}
            >
              Odobri prijavu
            </button>
            <button
              type="button"
              className="dugme dugme--opasnost"
              disabled={!prijava.mozeDaSeOdbije || saljeSe}
              onClick={() => setPrikaziOdbijanje((v) => !v)}
            >
              Odbij prijavu
            </button>
          </div>

          {prikaziOdbijanje && (
            <form onSubmit={odbij} className="forma-odbijanja">
              <label>
                Razlog odbijanja
                <textarea value={napomena} onChange={(dogadjaj) => setNapomena(dogadjaj.target.value)} required />
              </label>
              <button type="submit" className="dugme dugme--opasnost" disabled={saljeSe}>
                Potvrdi odbijanje
              </button>
            </form>
          )}
        </div>
      )}

      {uplataZaPregled && (
        <PregledUplatnice
          prijavaId={prijava.id}
          uplataId={uplataZaPregled}
          onZatvori={() => setUplataZaPregled(null)}
        />
      )}
    </article>
  );
}
