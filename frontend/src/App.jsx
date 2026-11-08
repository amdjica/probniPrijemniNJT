import { Route, Routes } from 'react-router-dom';
import Zaglavlje from './components/Zaglavlje.jsx';
import ZasticenaRuta from './components/ZasticenaRuta.jsx';
import PocetnaPage from './pages/PocetnaPage.jsx';
import DetaljiVrsteIspitaPage from './pages/DetaljiVrsteIspitaPage.jsx';
import PrijavljivanjePage from './pages/PrijavljivanjePage.jsx';
import RegistracijaPage from './pages/RegistracijaPage.jsx';
import TerminiPage from './pages/TerminiPage.jsx';
import MojPregledPage from './pages/MojPregledPage.jsx';
import AdminPocetnaPage from './pages/admin/AdminPocetnaPage.jsx';
import AdminTerminiPage from './pages/admin/AdminTerminiPage.jsx';
import AdminCenovnikPage from './pages/admin/AdminCenovnikPage.jsx';
import AdminPrijavePage from './pages/admin/AdminPrijavePage.jsx';
import AdminKandidatiPage from './pages/admin/AdminKandidatiPage.jsx';
import AdminStatistikaPage from './pages/admin/AdminStatistikaPage.jsx';
import './App.css';

function App() {
  return (
    <div className="app-okvir">
      <Zaglavlje />
      <main>
        <Routes>
          <Route path="/" element={<PocetnaPage />} />
          <Route path="/ispiti/:vrstaId" element={<DetaljiVrsteIspitaPage />} />
          <Route path="/prijavljivanje" element={<PrijavljivanjePage />} />
          <Route path="/registracija" element={<RegistracijaPage />} />

          <Route
            path="/termini"
            element={
              <ZasticenaRuta uloga="KANDIDAT">
                <TerminiPage />
              </ZasticenaRuta>
            }
          />
          <Route
            path="/moj-pregled"
            element={
              <ZasticenaRuta uloga="KANDIDAT">
                <MojPregledPage />
              </ZasticenaRuta>
            }
          />

          <Route
            path="/kontrolna-tabla"
            element={
              <ZasticenaRuta uloga="ADMINISTRATOR">
                <AdminPocetnaPage />
              </ZasticenaRuta>
            }
          />
          <Route
            path="/kontrolna-tabla/termini"
            element={
              <ZasticenaRuta uloga="ADMINISTRATOR">
                <AdminTerminiPage />
              </ZasticenaRuta>
            }
          />
          <Route
            path="/kontrolna-tabla/cenovnik"
            element={
              <ZasticenaRuta uloga="ADMINISTRATOR">
                <AdminCenovnikPage />
              </ZasticenaRuta>
            }
          />
          <Route
            path="/kontrolna-tabla/prijave"
            element={
              <ZasticenaRuta uloga="ADMINISTRATOR">
                <AdminPrijavePage />
              </ZasticenaRuta>
            }
          />
          <Route
            path="/kontrolna-tabla/kandidati"
            element={
              <ZasticenaRuta uloga="ADMINISTRATOR">
                <AdminKandidatiPage />
              </ZasticenaRuta>
            }
          />
          <Route
            path="/kontrolna-tabla/statistika"
            element={
              <ZasticenaRuta uloga="ADMINISTRATOR">
                <AdminStatistikaPage />
              </ZasticenaRuta>
            }
          />
        </Routes>
      </main>
    </div>
  );
}

export default App;
