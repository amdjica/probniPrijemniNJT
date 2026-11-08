import { useEffect, useState } from 'react';
import { getTermini, kreirajTermin, azurirajTermin, obrisiTermin } from '../../api/termini.js';
import { getVrsteIspita } from '../../api/vrsteIspita.js';
import { getLokacije } from '../../api/lokacije.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { formatujCenu } from '../../utils/cena.js';
import AdminPovratak from '../../components/admin/AdminPovratak.jsx';

const PRAZNA_FORMA = {
  datum: '',
  vremePocetka: '',
  napomena: '',
  dostupan: true,
  vrstaIspitaId: '',
  lokacijaId: '',
};

export default function AdminTerminiPage() {
  const { token } = useAuth();
  const [termini, setTermini] = useState([]);
  const [vrsteIspita, setVrsteIspita] = useState([]);
  const [lokacije, setLokacije] = useState([]);
  const [forma, setForma] = useState(PRAZNA_FORMA);
  const [idKojiSeMenja, setIdKojiSeMenja] = useState(null);
  const [poruka, setPoruka] = useState(null);

  function ucitaj() {
    getTermini({ token })
      .then(setTermini)
      .catch((e) => setPoruka(e.message));
  }

  useEffect(() => {
    ucitaj();
    getVrsteIspita().then(setVrsteIspita);
    getLokacije().then(setLokacije);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  function izmeni(polje, vrednost) {
    setForma((f) => ({ ...f, [polje]: vrednost }));
  }

  function pokreniIzmenu(t) {
    setIdKojiSeMenja(t.id);
    setForma({
      datum: t.datum,
      vremePocetka: t.vremePocetka,
      napomena: t.napomena ?? '',
      dostupan: t.dostupan,
      vrstaIspitaId: t.vrstaIspitaId,
      lokacijaId: t.lokacijaId,
    });
  }

  function otkaziIzmenu() {
    setIdKojiSeMenja(null);
    setForma(PRAZNA_FORMA);
  }

  async function posaljiFormu(dogadjaj) {
    dogadjaj.preventDefault();
    setPoruka(null);
    const telo = {
      ...forma,
      vrstaIspitaId: Number(forma.vrstaIspitaId),
      lokacijaId: Number(forma.lokacijaId),
    };
    try {
      if (idKojiSeMenja) {
        await azurirajTermin(idKojiSeMenja, telo, token);
      } else {
        await kreirajTermin(telo, token);
      }
      otkaziIzmenu();
      ucitaj();
    } catch (err) {
      setPoruka(err.message);
    }
  }

  async function obrisi(id) {
    setPoruka(null);
    try {
      await obrisiTermin(id, token);
      ucitaj();
    } catch (err) {
      setPoruka(err.message);
    }
  }

  return (
    <div className="stranica">
      <AdminPovratak />
      <h1>Termini ispita</h1>
      {poruka && <p className="poruka-greske">{poruka}</p>}

      <form onSubmit={posaljiFormu} className="forma-termina">
        <h2>{idKojiSeMenja ? `Izmena termina #${idKojiSeMenja}` : 'Novi termin'}</h2>
        <div className="forma-dva-stupca">
          <label>
            Datum
            <input type="date" value={forma.datum} onChange={(e) => izmeni('datum', e.target.value)} required />
          </label>
          <label>
            Vreme početka
            <input
              value={forma.vremePocetka}
              onChange={(e) => izmeni('vremePocetka', e.target.value)}
              placeholder="npr. 10:00"
              required
            />
          </label>
          <label>
            Vrsta ispita
            <select value={forma.vrstaIspitaId} onChange={(e) => izmeni('vrstaIspitaId', e.target.value)} required>
              <option value="">Izaberi...</option>
              {vrsteIspita.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.naziv}
                </option>
              ))}
            </select>
          </label>
          <label>
            Lokacija
            <select value={forma.lokacijaId} onChange={(e) => izmeni('lokacijaId', e.target.value)} required>
              <option value="">Izaberi...</option>
              {lokacije.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.naziv}
                </option>
              ))}
            </select>
          </label>
        </div>
        <label>
          Napomena
          <input value={forma.napomena} onChange={(e) => izmeni('napomena', e.target.value)} />
        </label>
        <label className="oznaka-checkbox">
          <input type="checkbox" checked={forma.dostupan} onChange={(e) => izmeni('dostupan', e.target.checked)} />
          Otvoren za prijave
        </label>
        <div className="forma-dugmad">
          <button type="submit" className="dugme dugme--primarno">
            {idKojiSeMenja ? 'Sačuvaj izmene' : 'Kreiraj termin'}
          </button>
          {idKojiSeMenja && (
            <button type="button" className="dugme dugme--sekundarno" onClick={otkaziIzmenu}>
              Otkaži
            </button>
          )}
        </div>
      </form>

      <div className="tabela-omotac">
        <table className="tabela">
          <thead>
            <tr>
              <th>Datum</th>
              <th>Vreme</th>
              <th>Vrsta ispita</th>
              <th>Lokacija</th>
              <th>Cena</th>
              <th>Prijava</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            {termini.map((t) => (
              <tr key={t.id}>
                <td data-label="Datum">{t.datum}</td>
                <td data-label="Vreme">{t.vremePocetka}</td>
                <td data-label="Vrsta ispita">{t.vrstaIspitaNaziv}</td>
                <td data-label="Lokacija">{t.lokacijaNaziv}</td>
                <td data-label="Cena">{formatujCenu(t.cena)}</td>
                <td data-label="Prijava">{t.brojPrijava}</td>
                <td data-label="Akcije" className="tabela__akcije">
                  <button
                    type="button"
                    className="dugme dugme--sekundarno"
                    disabled={!t.mozeDaSeMenja}
                    onClick={() => pokreniIzmenu(t)}
                  >
                    Izmeni
                  </button>
                  <button
                    type="button"
                    className="dugme dugme--opasnost"
                    disabled={!t.mozeDaSeObrise}
                    onClick={() => obrisi(t.id)}
                  >
                    Obriši
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
