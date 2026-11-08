import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function ZasticenaRuta({ uloga, children }) {
  const { jeUlogovan, jeKandidat, jeAdmin } = useAuth();

  if (!jeUlogovan) {
    return <Navigate to="/prijavljivanje" replace />;
  }
  if (uloga === 'KANDIDAT' && !jeKandidat) {
    return <Navigate to="/" replace />;
  }
  if (uloga === 'ADMINISTRATOR' && !jeAdmin) {
    return <Navigate to="/" replace />;
  }
  return children;
}
