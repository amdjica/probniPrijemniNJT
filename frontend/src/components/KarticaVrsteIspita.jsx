import { Link } from 'react-router-dom';

export default function KarticaVrsteIspita({ vrstaIspita }) {
  return (
    <Link to={`/ispiti/${vrstaIspita.id}`} className="kartica-ispita">
      <div className="kartica-ispita__znak">{vrstaIspita.naziv.charAt(0)}</div>
      <h3>{vrstaIspita.naziv}</h3>
      <span className="kartica-ispita__link">Pogledaj termine →</span>
    </Link>
  );
}
