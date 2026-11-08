import { useEffect, useState } from 'react';
import {
  PieChart,
  Pie,
  Cell,
  Legend,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from 'recharts';
import { getStatistikaPrijava } from '../../api/prijave.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { formatujCenu } from '../../utils/cena.js';
import { KATEGORIJALNE_BOJE, BOJE_GRAFIKA } from '../../utils/bojeGrafika.js';
import { infoOStatusu } from '../../utils/statusInfo.js';
import AdminPovratak from '../../components/admin/AdminPovratak.jsx';

const BOJE_NACINA = { JEDNOKRATNO: '#2a78d6', RATE: '#eda100' };

export default function AdminStatistikaPage() {
  const { token } = useAuth();
  const [statistika, setStatistika] = useState(null);
  const [greska, setGreska] = useState(null);

  useEffect(() => {
    getStatistikaPrijava(token)
      .then(setStatistika)
      .catch((e) => setGreska(e.message));
  }, [token]);

  if (greska) {
    return (
      <div className="stranica">
        <AdminPovratak />
        <p className="poruka-greske">{greska}</p>
      </div>
    );
  }

  if (!statistika) {
    return (
      <div className="stranica">
        <AdminPovratak />
        <p>Učitavanje pregleda...</p>
      </div>
    );
  }

  const podaciNacinPlacanja = Object.entries(statistika.brojPoNacinuPlacanja ?? {}).map(([nacin, broj]) => ({
    nacin,
    naziv: nacin === 'RATE' ? 'Na 5 rata' : 'Jednokratno',
    broj,
  }));

  const podaciPrihod = Object.entries(statistika.ocekivaniPrihodPoVrstiIspita ?? {}).map(([naziv, iznos], indeks) => ({
    naziv,
    iznos,
    boja: KATEGORIJALNE_BOJE[indeks % KATEGORIJALNE_BOJE.length],
  }));

  const podaciLokacija = Object.entries(statistika.brojPrijavljenihPoLokaciji ?? {})
    .sort((a, b) => b[1] - a[1]);

  return (
    <div className="stranica">
      <AdminPovratak />
      <h1>Pregled aktivnosti prijava</h1>

      <div className="red-stat-kartica">
        <div className="stat-kartica">
          <span className="stat-kartica__oznaka">Ukupno prijava</span>
          <span className="stat-kartica__vrednost">{statistika.ukupnoPrijava}</span>
        </div>
        <div className="stat-kartica">
          <span className="stat-kartica__oznaka">Naplaćeno do sada</span>
          <span className="stat-kartica__vrednost">{formatujCenu(statistika.ukupnoNaplaceno)}</span>
        </div>
        <div className="stat-kartica">
          <span className="stat-kartica__oznaka">Prosečno termina po kandidatu</span>
          <span className="stat-kartica__vrednost">{statistika.prosecanBrojTerminaPoKandidatu.toFixed(1)}</span>
        </div>
        <div className="stat-kartica">
          <span className="stat-kartica__oznaka">Sa priloženom uplatnicom</span>
          <span className="stat-kartica__vrednost">
            {statistika.brojSaUplatnicom} / {statistika.brojSaUplatnicom + statistika.brojBezUplatnice}
          </span>
        </div>
      </div>

      <div className="dvokolonski-prikaz">
        <div className="panel-izvestaja">
          <h2>Način plaćanja</h2>
          {podaciNacinPlacanja.length === 0 ? (
            <p>Nema podataka.</p>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={podaciNacinPlacanja} dataKey="broj" nameKey="naziv" innerRadius={55} outerRadius={85} paddingAngle={2}>
                  {podaciNacinPlacanja.map((stavka) => (
                    <Cell key={stavka.nacin} fill={BOJE_NACINA[stavka.nacin] ?? BOJE_GRAFIKA.priguseno} />
                  ))}
                </Pie>
                <Legend />
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="panel-izvestaja">
          <h2>Prijave po statusu</h2>
          <ul className="lista-brojanja">
            {Object.entries(statistika.brojPoStatusu ?? {}).map(([status, broj]) => (
              <li key={status}>
                <span className={`oznaka oznaka--${infoOStatusu(status).ton}`}>{infoOStatusu(status).oznaka}</span>
                <strong>{broj}</strong>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-izvestaja">
        <h2>Očekivani prihod po vrsti ispita</h2>
        {podaciPrihod.length === 0 ? (
          <p>Još uvek nema podnetih prijava.</p>
        ) : (
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={podaciPrihod}>
              <CartesianGrid stroke={BOJE_GRAFIKA.linijaMreze} vertical={false} />
              <XAxis dataKey="naziv" stroke={BOJE_GRAFIKA.priguseno} tick={{ fontSize: 12 }} />
              <YAxis stroke={BOJE_GRAFIKA.priguseno} />
              <Tooltip formatter={(vrednost) => formatujCenu(vrednost)} />
              <Bar dataKey="iznos" radius={[4, 4, 0, 0]}>
                {podaciPrihod.map((stavka) => (
                  <Cell key={stavka.naziv} fill={stavka.boja} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="panel-izvestaja">
        <h2>Popunjenost po lokaciji</h2>
        {podaciLokacija.length === 0 ? (
          <p>Još uvek nema prijavljenih kandidata.</p>
        ) : (
          <ul className="lista-brojanja">
            {podaciLokacija.map(([naziv, broj]) => (
              <li key={naziv}>
                <span>{naziv}</span>
                <strong>{broj}</strong>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
