import { useEffect, useState } from 'react';
import { getAktivneCenovnike, azurirajCenu } from '../../api/cenovnici.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { formatujCenu } from '../../utils/cena.js';
import AdminPovratak from '../../components/admin/AdminPovratak.jsx';

export default function AdminCenovnikPage() {
  const { token } = useAuth();
  const [cenovnici, setCenovnici] = useState([]);
  const [izabranId, setIzabranId] = useState(null);
  const [izmena, setIzmena] = useState(null);
  const [poruka, setPoruka] = useState(null);

  function ucitaj() {
    getAktivneCenovnike(token)
      .then((podaci) => {
        setCenovnici(podaci);
        if (izabranId === null && podaci.length > 0) {
          setIzabranId(podaci[0].vrstaIspitaId);
        }
      })
      .catch((e) => setPoruka(e.message));
  }

  useEffect(() => {
    ucitaj();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const izabranCenovnik = cenovnici.find((c) => c.vrstaIspitaId === izabranId);

  function pokreniIzmenu(c) {
    setIzmena({
      vrstaIspitaId: c.vrstaIspitaId,
      naziv: c.naziv,
      cena: c.cena,
      vaziOd: c.vaziOd,
      vaziDo: c.vaziDo ?? '',
      opis: c.opis ?? '',
    });
  }

  async function sacuvajIzmenu(dogadjaj) {
    dogadjaj.preventDefault();
    setPoruka(null);
    try {
      await azurirajCenu(
        izmena.vrstaIspitaId,
        {
          naziv: izmena.naziv,
          cena: Number(izmena.cena),
          vaziOd: izmena.vaziOd,
          vaziDo: izmena.vaziDo || null,
          opis: izmena.opis,
        },
        token,
      );
      setIzmena(null);
      ucitaj();
    } catch (err) {
      setPoruka(err.message);
    }
  }

  return (
    <div className="stranica">
      <AdminPovratak />
      <h1>Cenovnik</h1>
      {poruka && <p className="poruka-greske">{poruka}</p>}

      <div className="dvopanelni-prikaz">
        <aside className="panel-filtera">
          <h2>Vrste ispita</h2>
          <ul className="lista-cenovnika">
            {cenovnici.map((c) => (
              <li key={c.vrstaIspitaId}>
                <button
                  type="button"
                  className={`stavka-cenovnika ${c.vrstaIspitaId === izabranId ? 'stavka-cenovnika--aktivna' : ''}`}
                  onClick={() => {
                    setIzabranId(c.vrstaIspitaId);
                    setIzmena(null);
                  }}
                >
                  <span>{c.vrstaIspitaNaziv}</span>
                  <strong>{formatujCenu(c.cena)}</strong>
                </button>
              </li>
            ))}
          </ul>
        </aside>

        <section className="panel-pregleda">
          {!izabranCenovnik && <p>Izaberi vrstu ispita sa leve strane.</p>}

          {izabranCenovnik && izmena?.vrstaIspitaId === izabranCenovnik.vrstaIspitaId ? (
            <form onSubmit={sacuvajIzmenu} className="forma-cenovnika-admin">
              <h2>Izmena cene — {izabranCenovnik.vrstaIspitaNaziv}</h2>
              <div className="forma-dva-stupca">
                <label>
                  Naziv paketa
                  <input
                    value={izmena.naziv}
                    onChange={(dogadjaj) => setIzmena({ ...izmena, naziv: dogadjaj.target.value })}
                    required
                  />
                </label>
                <label>
                  Cena (RSD)
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={izmena.cena}
                    onChange={(dogadjaj) => setIzmena({ ...izmena, cena: dogadjaj.target.value })}
                    required
                  />
                </label>
                <label>
                  Važi od
                  <input
                    type="date"
                    value={izmena.vaziOd}
                    onChange={(dogadjaj) => setIzmena({ ...izmena, vaziOd: dogadjaj.target.value })}
                    required
                  />
                </label>
                <label>
                  Važi do
                  <input
                    type="date"
                    value={izmena.vaziDo}
                    onChange={(dogadjaj) => setIzmena({ ...izmena, vaziDo: dogadjaj.target.value })}
                  />
                </label>
              </div>
              <label>
                Opis
                <textarea
                  value={izmena.opis}
                  onChange={(dogadjaj) => setIzmena({ ...izmena, opis: dogadjaj.target.value })}
                />
              </label>
              <div className="forma-dugmad">
                <button type="submit" className="dugme dugme--primarno">
                  Sačuvaj
                </button>
                <button type="button" className="dugme dugme--sekundarno" onClick={() => setIzmena(null)}>
                  Otkaži
                </button>
              </div>
            </form>
          ) : (
            izabranCenovnik && (
              <div className="pregled-cenovnika">
                <h2>{izabranCenovnik.vrstaIspitaNaziv}</h2>
                <p className="pregled-cenovnika__cena">{formatujCenu(izabranCenovnik.cena)}</p>
                <p>{izabranCenovnik.naziv}</p>
                {izabranCenovnik.opis && <p className="pregled-cenovnika__opis">{izabranCenovnik.opis}</p>}
                <p className="pregled-cenovnika__vazenje">
                  Važi od {izabranCenovnik.vaziOd}
                  {izabranCenovnik.vaziDo ? ` do ${izabranCenovnik.vaziDo}` : ''}
                </p>
                <button type="button" className="dugme dugme--primarno" onClick={() => pokreniIzmenu(izabranCenovnik)}>
                  Izmeni cenu
                </button>
              </div>
            )
          )}
        </section>
      </div>
    </div>
  );
}
