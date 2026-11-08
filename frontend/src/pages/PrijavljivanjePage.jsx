import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function PrijavljivanjePage() {
  const { prijaviSe } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [greska, setGreska] = useState(null);
  const [saljeSe, setSaljeSe] = useState(false);

  async function posalji(dogadjaj) {
    dogadjaj.preventDefault();
    setGreska(null);
    setSaljeSe(true);
    try {
      const odgovor = await prijaviSe(email, password);
      navigate(odgovor.tipNaloga === 'ADMINISTRATOR' ? '/kontrolna-tabla' : '/termini');
    } catch (err) {
      setGreska(err.message);
    } finally {
      setSaljeSe(false);
    }
  }

  return (
    <div className="stranica stranica--forma">
      <div className="kartica-forme">
        <h1>Prijava na sistem</h1>
        <form onSubmit={posalji}>
          <label>
            Email adresa
            <input
              type="email"
              value={email}
              onChange={(dogadjaj) => setEmail(dogadjaj.target.value)}
              required
            />
          </label>
          <label>
            Lozinka
            <input
              type="password"
              value={password}
              onChange={(dogadjaj) => setPassword(dogadjaj.target.value)}
              required
            />
          </label>
          {greska && <p className="poruka-greske">{greska}</p>}
          <button type="submit" className="dugme dugme--primarno dugme--puno" disabled={saljeSe}>
            {saljeSe ? 'Prijavljivanje...' : 'Prijavi se'}
          </button>
        </form>
        <p className="kartica-forme__podnozje">
          Nemaš nalog? <Link to="/registracija">Registruj se</Link>
        </p>
      </div>
    </div>
  );
}
