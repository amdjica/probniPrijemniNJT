import { useEffect, useState } from 'react';
import { preuzmiUplatnicu } from '../api/uplate.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function PregledUplatnice({ prijavaId, uplataId, onZatvori }) {
  const { token } = useAuth();
  const [url, setUrl] = useState(null);
  const [tip, setTip] = useState(null);
  const [greska, setGreska] = useState(null);
  const [ucitano, setUcitano] = useState(false);

  useEffect(() => {
    let objectUrl;

    preuzmiUplatnicu(prijavaId, uplataId, token)
      .then(({ blob }) => {
        objectUrl = URL.createObjectURL(blob);
        setUrl(objectUrl);
        setTip(blob.type);
      })
      .catch((e) => setGreska(e.message))
      .finally(() => setUcitano(true));

    return () => {
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [prijavaId, uplataId, token]);

  return (
    <div className="modal-podloga" onClick={onZatvori}>
      <div className="modal-uplatnice" onClick={(dogadjaj) => dogadjaj.stopPropagation()}>
        <div className="modal-uplatnice__zaglavlje">
          <h3>Priložena uplatnica</h3>
          <button type="button" className="modal-uplatnice__zatvori" onClick={onZatvori} aria-label="Zatvori">
            ×
          </button>
        </div>

        {!ucitano && <p>Učitavanje...</p>}
        {ucitano && greska && <p className="poruka-greske">{greska}</p>}
        {ucitano && !greska && url && (
          <div className="modal-uplatnice__sadrzaj">
            {tip?.startsWith('image/') ? (
              <img src={url} alt="Priložena uplatnica" />
            ) : (
              <iframe src={url} title="Priložena uplatnica" />
            )}
            <a href={url} target="_blank" rel="noopener noreferrer" className="dugme dugme--sekundarno">
              Otvori u novom tabu
            </a>
          </div>
        )}
      </div>
    </div>
  );
}
