import { useEffect, useState } from 'react';
import { getTermini, getPrijavljeneKandidate, preuzmiIzvestajTermina } from '../../api/termini.js';
import { useAuth } from '../../context/AuthContext.jsx';
import AdminPovratak from '../../components/admin/AdminPovratak.jsx';

export default function AdminKandidatiPage() {
  const { token } = useAuth();
  const [termini, setTermini] = useState([]);
  const [izabranId, setIzabranId] = useState('');
  const [kandidati, setKandidati] = useState([]);
  const [greska, setGreska] = useState(null);
  const [preuzimaSe, setPreuzimaSe] = useState(false);

  useEffect(() => {
    getTermini({ token })
      .then(setTermini)
      .catch((e) => setGreska(e.message));
  }, [token]);

  useEffect(() => {
    if (!izabranId) {
      return;
    }
    getPrijavljeneKandidate(izabranId, token)
      .then(setKandidati)
      .catch((e) => setGreska(e.message));
  }, [izabranId, token]);

  const izabranTermin = termini.find((t) => String(t.id) === String(izabranId));

  async function preuzmiPdf() {
    setGreska(null);
    setPreuzimaSe(true);
    try {
      const { blob, naziv } = await preuzmiIzvestajTermina(izabranId, token);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = naziv || `termin-${izabranId}-kandidati.pdf`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      setGreska(e.message);
    } finally {
      setPreuzimaSe(false);
    }
  }

  return (
    <div className="stranica">
      <AdminPovratak />
      <h1>Kandidati po terminu</h1>

      {greska && <p className="poruka-greske">{greska}</p>}

      <div className="dvopanelni-prikaz">
        <aside className="panel-filtera">
          <h2>Izbor termina</h2>
          <label>
            Termin
            <select value={izabranId} onChange={(e) => setIzabranId(e.target.value)}>
              <option value="">-- izaberi termin --</option>
              {termini.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.vrstaIspitaNaziv} — {t.datum} u {t.vremePocetka}
                </option>
              ))}
            </select>
          </label>

          {izabranTermin && (
            <div className="panel-filtera__detalji">
              <p>{izabranTermin.lokacijaNaziv}</p>
              <p>{izabranTermin.lokacijaAdresa}</p>
              <p>Prijavljenih: {izabranTermin.brojPrijava}</p>
            </div>
          )}

          <button
            type="button"
            className="dugme dugme--primarno dugme--puno"
            disabled={!izabranId || preuzimaSe}
            onClick={preuzmiPdf}
          >
            {preuzimaSe ? 'Priprema PDF-a...' : 'Preuzmi spisak (PDF)'}
          </button>
        </aside>

        <section className="panel-pregleda">
          {!izabranId && <p>Izaberi termin sa leve strane da bi video spisak kandidata.</p>}

          {izabranId && (
            <div className="tabela-omotac">
              <table className="tabela">
                <thead>
                  <tr>
                    <th>Ime</th>
                    <th>Prezime</th>
                    <th>Email</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {kandidati.length === 0 && (
                    <tr>
                      <td colSpan={4}>Nema odobrenih prijava za izabrani termin.</td>
                    </tr>
                  )}
                  {kandidati.map((k, i) => (
                    <tr key={i}>
                      <td data-label="Ime">{k.ime}</td>
                      <td data-label="Prezime">{k.prezime}</td>
                      <td data-label="Email">{k.email}</td>
                      <td data-label="Status">{k.statusPrijave}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
