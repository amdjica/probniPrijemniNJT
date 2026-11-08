import { formatujCenu } from '../utils/cena.js';

export default function RedTermina({ termin, izabran, onPromena, samoPregled }) {
  return (
    <div className={`red-termina ${izabran ? 'je-izabran' : ''}`}>
      {!samoPregled && (
        <input
          type="checkbox"
          checked={!!izabran}
          onChange={() => onPromena(termin)}
          disabled={!termin.dostupan}
        />
      )}
      <div className="red-termina__info">
        <span className="red-termina__vrsta">{termin.vrstaIspitaNaziv}</span>
        <span className="red-termina__kada">
          {termin.datum} u {termin.vremePocetka}
        </span>
        <span className="red-termina__mesto">
          {termin.lokacijaNaziv} — {termin.lokacijaAdresa}
        </span>
      </div>
      <div className="red-termina__cena">{formatujCenu(termin.cena)}</div>
    </div>
  );
}
