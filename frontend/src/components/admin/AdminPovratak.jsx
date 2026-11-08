import { Link } from 'react-router-dom';

export default function AdminPovratak() {
  return (
    <Link to="/kontrolna-tabla" className="admin-povratak">
      ← Nazad na kontrolnu tablu
    </Link>
  );
}
