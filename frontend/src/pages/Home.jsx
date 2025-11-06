import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authAPI } from '../services/api';

function Home() {
  const navigate = useNavigate();
  const isAuthenticated = authAPI.isAuthenticated();
  const userData = authAPI.getUserData();

  useEffect(() => {
    if (isAuthenticated && userData) {
      // Redirect authenticated users to their portal
      if (userData.role === 'ADMIN') {
        navigate('/admin');
      } else if (userData.role === 'PATIENT') {
        navigate('/patient');
      } else if (userData.role === 'DOCTOR') {
        navigate('/doctor');
      }
    }
  }, [isAuthenticated, userData, navigate]);

  return (
    <>
      <section className="hero">
        <div className="container">
          <h1>Digital Health Repository</h1>
          <p>Modern healthcare management system</p>
          <div className="btn-group" style={{ justifyContent: 'center' }}>
            <Link to="/login" className="btn btn-primary">Login</Link>
            <Link to="/register" className="btn btn-outline">Register</Link>
          </div>
        </div>
      </section>

      <section className="container">
        <div className="grid grid-3">
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">For Patients</h3>
            </div>
            <ul style={{ listStyle: 'none' }}>
              <li style={{ marginBottom: '0.5rem' }}>✓ Book appointments</li>
              <li style={{ marginBottom: '0.5rem' }}>✓ View health records</li>
              <li style={{ marginBottom: '0.5rem' }}>✓ Manage appointments</li>
              <li style={{ marginBottom: '0.5rem' }}>✓ Track medical history</li>
            </ul>
          </div>

          <div className="card">
            <div className="card-header">
              <h3 className="card-title">For Doctors</h3>
            </div>
            <ul style={{ listStyle: 'none' }}>
              <li style={{ marginBottom: '0.5rem' }}>✓ Manage schedule</li>
              <li style={{ marginBottom: '0.5rem' }}>✓ View appointments</li>
              <li style={{ marginBottom: '0.5rem' }}>✓ Update patient records</li>
              <li style={{ marginBottom: '0.5rem' }}>✓ Track consultations</li>
            </ul>
          </div>

          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Admin</h3>
            </div>
            <ul style={{ listStyle: 'none' }}>
              <li style={{ marginBottom: '0.5rem' }}>✓ System overview</li>
              <li style={{ marginBottom: '0.5rem' }}>✓ Manage users</li>
              <li style={{ marginBottom: '0.5rem' }}>✓ View analytics</li>
              <li style={{ marginBottom: '0.5rem' }}>✓ Generate reports</li>
            </ul>
          </div>
        </div>
      </section>
    </>
  );
}

export default Home;
