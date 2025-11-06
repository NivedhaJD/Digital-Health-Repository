import React from 'react';
import { Link } from 'react-router-dom';

function Home() {
  return (
    <>
      <section className="hero">
        <div className="container">
          <h1>Digital Health Repository</h1>
          <p>Modern healthcare management system</p>
          <div className="btn-group" style={{ justifyContent: 'center' }}>
            <Link to="/patient" className="btn btn-primary">Patient Portal</Link>
            <Link to="/doctor" className="btn btn-outline">Doctor Portal</Link>
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
