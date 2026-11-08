import { useCallback, useEffect, useState } from 'react';
import { pretraziPrijave } from '../../api/prijave.js';
import { useAuth } from '../../context/AuthContext.jsx';
import AdminPovratak from '../../components/admin/AdminPovratak.jsx';
import AdminRedPrijave from './AdminRedPrijave.jsx';

export default function AdminPrijavePage() {
  const { token } = useAuth();
  const [filteri, setFilteri] = useState({ ime: '', email: '', status: '' });
  const [prijave, setPrijave] = useState([]);
  const [greska, setGreska] = useState(null);

  const ucitaj = useCallback(() => {
    pretraziPrijave(filteri, token)
      .then(setPrijave)
      .catch((e) => setGreska(e.message));
  }, [filteri, token]);

  useEffect(() => {
    ucitaj();
  }, [ucitaj]);

  return (
    <div className="stranica">
      <AdminPovratak />
      <h1>Prijave kandidata</h1>

      <section className="kartica-filtera">
        <h2>Pretraga</h2>
        <div className="kartica-filtera__polja">
          <label>
            Ime i prezime
            <input value={filteri.ime} onChange={(e) => setFilteri({ ...filteri, ime: e.target.value })} />
          </label>
          <label>
            Email
            <input value={filteri.email} onChange={(e) => setFilteri({ ...filteri, email: e.target.value })} />
          </label>
          <label>
            Status
            <select value={filteri.status} onChange={(e) => setFilteri({ ...filteri, status: e.target.value })}>
              <option value="">Svi</option>
              <option value="NA_CEKANJU">Na čekanju</option>
              <option value="ODOBRENA">Odobrena</option>
              <option value="ODBIJENA">Odbijena</option>
            </select>
          </label>
        </div>
      </section>

      {greska && <p className="poruka-greske">{greska}</p>}

      <div className="pregled-prijava-admin">
        {prijave.length === 0 && <p>Nema prijava koje odgovaraju filterima.</p>}
        {prijave.map((p) => (
          <AdminRedPrijave key={p.id} prijava={p} naPromenu={ucitaj} />
        ))}
      </div>
    </div>
  );
}
