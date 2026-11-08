import { Link } from 'react-router-dom';

const STAVKE = [
  { put: '/kontrolna-tabla/termini', naslov: 'Termini', opis: 'Kreiraj i uredi termine ispita.' },
  { put: '/kontrolna-tabla/cenovnik', naslov: 'Cenovnik', opis: 'Podesi aktivne cene po vrsti ispita.' },
  { put: '/kontrolna-tabla/prijave', naslov: 'Prijave', opis: 'Pregledaj, odobri ili odbij prijave kandidata.' },
  { put: '/kontrolna-tabla/kandidati', naslov: 'Kandidati po terminu', opis: 'Spisak odobrenih kandidata za izabrani termin i izvoz u PDF.' },
  { put: '/kontrolna-tabla/statistika', naslov: 'Statistika', opis: 'Pregled naplate i uspešnosti prijava.' },
];

export default function AdminPocetnaPage() {
  return (
    <div className="stranica">
      <section className="uvod-kontrolne-table">
        <h1>Kontrolna tabla</h1>
        <p>Odaberi jedan od modula ispod za upravljanje probnim prijemnim ispitom.</p>
      </section>

      <div className="lista-modula">
        {STAVKE.map((stavka, indeks) => (
          <Link key={stavka.put} to={stavka.put} className="red-modula">
            <span className="red-modula__broj">{String(indeks + 1).padStart(2, '0')}</span>
            <span className="red-modula__tekst">
              <strong>{stavka.naslov}</strong>
              <span>{stavka.opis}</span>
            </span>
            <span className="red-modula__strelica">→</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
