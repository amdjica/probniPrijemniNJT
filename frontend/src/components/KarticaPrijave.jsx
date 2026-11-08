import { useState } from 'react';
import OznakaStatusa from './OznakaStatusa.jsx';
import PregledUplatnice from './PregledUplatnice.jsx';
import { formatujCenu } from '../utils/cena.js';

export default function KarticaPrijave({ prijava, naDodavanjeUplate }) {
  const [uplatnica, setUplatnica] = useState(null);
  const [poruka, setPoruka] = useState(null);
  const [saljeSe, setSaljeSe] = useState(false);
  const [formaOtvorena, setFormaOtvorena] = useState(false);
  const [uplataZaPregled, setUplataZaPregled] = useState(null);

  const sledecaRata =
    prijava.nacinPlacanja === 'RATE' ? Math.min(prijava.iznosJedneRate, prijava.preostalo) : prijava.preostalo;

  async function posaljiUplatu(dogadjaj) {
    dogadjaj.preventDefault();
    setPoruka(null);

    if (!uplatnica) {
      setPoruka('Priložite sliku ili dokument uplatnice.');
      return;
    }

    setSaljeSe(true);
    try {
      await naDodavanjeUplate(prijava.id, { iznos: sledecaRata, uplatnica });
      setUplatnica(null);
      setFormaOtvorena(false);
    } catch (greska) {
      setPoruka(greska.message);
    } finally {
      setSaljeSe(false);
    }
  }

  return (
    <article className="kartica-prijave">
      <header className="kartica-prijave__zaglavlje">
        <div>
          <h3>Prijava #{prijava.id}</h3>
          <span className="kartica-prijave__datum">Podneta {prijava.datumPrijave}</span>
        </div>
        <OznakaStatusa status={prijava.status} />
      </header>

      <ul className="kartica-prijave__termini">
        {prijava.termini.map((termin) => (
          <li key={termin.id}>
            {termin.vrstaIspitaNaziv} — {termin.datum} u {termin.vremePocetka}, {termin.lokacijaNaziv}
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

      {prijava.status === 'ODBIJENA' && prijava.napomena && (
        <p className="kartica-prijave__razlog">Napomena administratora: {prijava.napomena}</p>
      )}

      {prijava.mozeDaDodaUplatu && prijava.preostalo > 0 && (
        <div className="blok-uplatnica">
          {!formaOtvorena ? (
            <button type="button" className="dugme dugme--sekundarno" onClick={() => setFormaOtvorena(true)}>
              Evidentiraj sledeću uplatu ({formatujCenu(sledecaRata)})
            </button>
          ) : (
            <form className="forma-uplatnice" onSubmit={posaljiUplatu}>
              <p>
                Prilažeš dokaz za uplatu u iznosu od <strong>{formatujCenu(sledecaRata)}</strong>.
              </p>
              <label>
                Slika ili dokument uplatnice
                <input
                  type="file"
                  accept="image/png,image/jpeg,application/pdf"
                  onChange={(dogadjaj) => setUplatnica(dogadjaj.target.files?.[0] ?? null)}
                  required
                />
              </label>
              {poruka && <p className="kartica-prijave__greska">{poruka}</p>}
              <div className="forma-dugmad">
                <button type="submit" className="dugme dugme--primarno" disabled={saljeSe}>
                  {saljeSe ? 'Slanje...' : 'Potvrdi uplatu'}
                </button>
                <button type="button" className="dugme dugme--sekundarno" onClick={() => setFormaOtvorena(false)}>
                  Otkaži
                </button>
              </div>
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
