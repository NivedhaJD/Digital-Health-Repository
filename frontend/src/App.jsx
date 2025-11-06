import React from 'react';
import { Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import Home from './pages/Home';
import PatientPortal from './pages/PatientPortal';
import DoctorPortal from './pages/DoctorPortal';
import AdminPortal from './pages/AdminPortal';
import Login from './pages/Login';
import Register from './pages/Register';
import { authAPI } from './services/api';

function App() {
  const location = useLocation();
  const navigate = useNavigate();
  const isAuthenticated = authAPI.isAuthenticated();
  const userData = authAPI.getUserData();

  const handleLogout = () => {
    authAPI.clearAuth();
    navigate('/login');
  };

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
            {isAuthenticated ? (
              <>
                {userData?.role === 'PATIENT' && (
                  <Link 
                    to="/patient" 
                    className={`nav-link ${location.pathname === '/patient' ? 'active' : ''}`}
                  >
                    Patient Portal
                  </Link>
                )}
                {userData?.role === 'DOCTOR' && (
                  <Link 
                    to="/doctor" 
                    className={`nav-link ${location.pathname === '/doctor' ? 'active' : ''}`}
                  >
                    Doctor Portal
                  </Link>
                )}
                {userData?.role === 'ADMIN' && (
                  <Link 
                    to="/admin" 
                    className={`nav-link ${location.pathname === '/admin' ? 'active' : ''}`}
                  >
                    Admin
                  </Link>
                )}
                <button 
                  onClick={handleLogout}
                  className="nav-link"
                  style={{ 
                    background: 'none', 
                    border: 'none', 
                    color: 'inherit', 
                    cursor: 'pointer',
                    padding: '8px 16px'
                  }}
                >
                  Logout ({userData?.username})
                </button>
              </>
            ) : (
              <>
                <Link 
                  to="/login" 
                  className={`nav-link ${location.pathname === '/login' ? 'active' : ''}`}
                >
                  Login
                </Link>
                <Link 
                  to="/register" 
                  className={`nav-link ${location.pathname === '/register' ? 'active' : ''}`}
                >
                  Register
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>

      <main style={{ flex: 1 }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
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
