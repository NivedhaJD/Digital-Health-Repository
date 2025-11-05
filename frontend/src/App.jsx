import React from 'react';
import { Routes, Route, Link, useLocation } from 'react-router-dom';
import Home from './pages/Home';
import PatientPortal from './pages/PatientPortal';
import DoctorPortal from './pages/DoctorPortal';
import AdminPortal from './pages/AdminPortal';

function App() {
  const location = useLocation();

  return (
    <>
      <header className="header">
        <div className="container header-content">
          <Link to="/" className="logo">Digital Health System</Link>
          <nav className="nav">
            <Link 
              to="/" 
              className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
            >
              Home
            </Link>
            <Link 
              to="/patient" 
              className={`nav-link ${location.pathname === '/patient' ? 'active' : ''}`}
            >
              Patient Portal
            </Link>
            <Link 
              to="/doctor" 
              className={`nav-link ${location.pathname === '/doctor' ? 'active' : ''}`}
            >
              Doctor Portal
            </Link>
            <Link 
              to="/admin" 
              className={`nav-link ${location.pathname === '/admin' ? 'active' : ''}`}
            >
              Admin
            </Link>
          </nav>
        </div>
      </header>

      <main style={{ flex: 1 }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/patient" element={<PatientPortal />} />
          <Route path="/doctor" element={<DoctorPortal />} />
          <Route path="/admin" element={<AdminPortal />} />
        </Routes>
      </main>

      <footer className="footer">
        <div className="container">
          <p>&copy; 2024 Digital Health System. All rights reserved.</p>
        </div>
      </footer>
    </>
  );
}

export default App;
