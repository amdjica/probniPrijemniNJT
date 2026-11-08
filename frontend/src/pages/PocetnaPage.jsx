import { useEffect, useState } from 'react';
import { getVrsteIspita } from '../api/vrsteIspita.js';
import KarticaVrsteIspita from '../components/KarticaVrsteIspita.jsx';
import heroImg from '../assets/hero.png';

export default function PocetnaPage() {
  const [vrsteIspita, setVrsteIspita] = useState([]);
  const [greska, setGreska] = useState(null);

  useEffect(() => {
    getVrsteIspita()
      .then(setVrsteIspita)
      .catch((e) => setGreska(e.message));
  }, []);

  return (
    <div className="stranica">
      <section className="naslovna-sekcija">
        <div className="naslovna-sekcija__tekst">
          <span className="naslovna-sekcija__oznaka">Probni prijemni ispit</span>
          <h1>Pripremi se za prijemni bez iznenađenja na dan ispita.</h1>
          <p>
            Izaberi vrstu ispita, rezerviši termin koji ti odgovara i plati odjednom ili na rate -
            sve na jednom mestu.
          </p>
        </div>
        <img src={heroImg} alt="" className="naslovna-sekcija__slika" />
      </section>

      <section className="sekcija-vrste">
        <h2>Vrste ispita</h2>
        {greska && <p className="poruka-greske">{greska}</p>}
        <div className="mreza-kartica">
          {vrsteIspita.map((vrsta) => (
            <KarticaVrsteIspita key={vrsta.id} vrstaIspita={vrsta} />
          ))}
        </div>
      </section>

      <section className="sekcija-koraci">
        <h2>Kako funkcioniše prijava</h2>
        <ol className="lista-koraka">
          <li>
            <strong>1. Napravi nalog.</strong> Registracija traje manje od minuta.
          </li>
          <li>
            <strong>2. Izaberi termine.</strong> Pregledaj dostupne termine po vrsti ispita i lokaciji.
          </li>
          <li>
            <strong>3. Plati kako ti odgovara.</strong> Jednokratno ili u ratama, dok ne pokriješ punu cenu.
          </li>
          <li>
            <strong>4. Sačekaj odobrenje.</strong> Administrator odobrava prijavu čim je uplata kompletna.
          </li>
        </ol>
      </section>
    </div>
  );
}
