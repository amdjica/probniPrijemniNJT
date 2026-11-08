import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Zaglavlje() {
  const { jeUlogovan, jeKandidat, jeAdmin, korisnik, odjaviSe } = useAuth();
  const [otvoren, setOtvoren] = useState(false);
  const navigate = useNavigate();

  function odjava() {
    odjaviSe();
    setOtvoren(false);
    navigate('/');
  }

  return (
    <header className="zaglavlje">
      <div className="zaglavlje__sadrzaj">
        <Link to="/" className="zaglavlje__brend" onClick={() => setOtvoren(false)}>
          <span className="zaglavlje__znak">NP</span>
          NJT Probni Ispit
        </Link>

        <button
          type="button"
          className="zaglavlje__prekidac"
          onClick={() => setOtvoren((v) => !v)}
          aria-label="Otvori meni"
        >
          <span />
          <span />
          <span />
        </button>

        <nav className={`zaglavlje__nav ${otvoren ? 'je-otvoren' : ''}`}>
          <NavLink to="/" onClick={() => setOtvoren(false)} end>
            Početna
          </NavLink>
          {jeKandidat && (
            <NavLink to="/termini" onClick={() => setOtvoren(false)}>
              Termini
            </NavLink>
          )}
          {jeKandidat && (
            <NavLink to="/moj-pregled" onClick={() => setOtvoren(false)}>
              Moj pregled
            </NavLink>
          )}
          {jeAdmin && (
            <NavLink to="/kontrolna-tabla" onClick={() => setOtvoren(false)}>
              Kontrolna tabla
            </NavLink>
          )}
          {!jeUlogovan && (
            <NavLink to="/registracija" onClick={() => setOtvoren(false)}>
              Registracija
            </NavLink>
          )}
          {!jeUlogovan ? (
            <NavLink to="/prijavljivanje" className="zaglavlje__cta" onClick={() => setOtvoren(false)}>
              Prijavi se
            </NavLink>
          ) : (
            <button type="button" className="zaglavlje__odjava" onClick={odjava}>
              Odjavi se ({korisnik.ime})
            </button>
          )}
        </nav>
      </div>
    </header>
  );
}
