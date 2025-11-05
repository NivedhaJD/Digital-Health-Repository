import React, { useState, useEffect } from 'react';
import { patientAPI, doctorAPI, appointmentAPI, healthRecordAPI } from '../services/api';

function AdminPortal() {
  const [stats, setStats] = useState({
    patients: 0,
    doctors: 0,
    appointments: 0,
    healthRecords: 0
  });
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAllData();
  }, []);

  const loadAllData = async () => {
    setLoading(true);
    try {
      const [patientsData, doctorsData, appointmentsData, recordsData] = await Promise.all([
        patientAPI.getAll(),
        doctorAPI.getAll(),
        appointmentAPI.getAll(),
        healthRecordAPI.getAll()
      ]);
      
      setPatients(patientsData);
      setDoctors(doctorsData);
      setAppointments(appointmentsData);
      
      setStats({
        patients: patientsData.length,
        doctors: doctorsData.length,
        appointments: appointmentsData.length,
        healthRecords: recordsData.length
      });
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getAppointmentStats = () => {
    const statusCount = appointments.reduce((acc, apt) => {
      acc[apt.status] = (acc[apt.status] || 0) + 1;
      return acc;
    }, {});
    return statusCount;
  };

  const appointmentStats = getAppointmentStats();

  if (loading) {
    return (
      <div className="container" style={{ marginTop: '2rem', textAlign: 'center' }}>
        <h1>Loading...</h1>
      </div>
    );
  }

  return (
    <div className="container" style={{ marginTop: '2rem', marginBottom: '2rem' }}>
      <h1>Admin Dashboard</h1>
      <p style={{ color: 'var(--gray-500)', marginBottom: '2rem' }}>System Overview and Analytics</p>

      {/* Stats Grid */}
      <div className="stats-grid">
        <div className="stat-card">
          <h3>{stats.patients}</h3>
          <p>Total Patients</p>
        </div>
        <div className="stat-card">
          <h3>{stats.doctors}</h3>
          <p>Total Doctors</p>
        </div>
        <div className="stat-card">
          <h3>{stats.appointments}</h3>
          <p>Total Appointments</p>
        </div>
        <div className="stat-card">
          <h3>{stats.healthRecords}</h3>
          <p>Health Records</p>
        </div>
      </div>

      {/* Appointment Status Breakdown */}
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">Appointment Status Breakdown</h2>
        </div>
        <div className="grid grid-2">
          {Object.entries(appointmentStats).map(([status, count]) => (
            <div key={status} className="list-item">
              <strong>{status}</strong>
              <div style={{ fontSize: '1.5rem', marginTop: '0.5rem' }}>{count}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Patients */}
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">All Patients</h2>
          <p className="card-subtitle">{patients.length} registered patients</p>
        </div>
        <div style={{ maxHeight: '400px', overflow: 'auto' }}>
          {patients.map(patient => (
            <div key={patient.id} className="list-item">
              <strong>{patient.name}</strong> - {patient.age} years, {patient.gender}<br />
              <span style={{ fontSize: '0.875rem', color: 'var(--gray-500)' }}>
                ID: {patient.id} | Contact: {patient.contact}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* All Doctors */}
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">All Doctors</h2>
          <p className="card-subtitle">{doctors.length} registered doctors</p>
        </div>
        <div className="grid grid-2">
          {doctors.map(doctor => (
            <div key={doctor.doctorId} className="card" style={{ margin: 0 }}>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Dr. {doctor.name}</h3>
              <p style={{ color: 'var(--gray-500)', marginBottom: '0.75rem' }}>{doctor.specialty}</p>
              <div style={{ fontSize: '0.875rem' }}>
                <div>📞 {doctor.contact}</div>
                <div>✉️ {doctor.email}</div>
                <div>🕒 {doctor.schedule}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Appointments */}
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">All Appointments</h2>
          <p className="card-subtitle">{appointments.length} total appointments</p>
        </div>
        <div style={{ maxHeight: '400px', overflow: 'auto' }}>
          {appointments.map(apt => (
            <div key={apt.id} className="list-item">
              <div style={{ marginBottom: '0.5rem' }}>
                <strong>{apt.dateTime}</strong>
                <span style={{ 
                  marginLeft: '1rem', 
                  padding: '0.25rem 0.5rem', 
                  background: apt.status === 'COMPLETED' ? 'var(--gray-900)' : 'var(--gray-200)',
                  color: apt.status === 'COMPLETED' ? 'var(--white)' : 'var(--gray-900)',
                  fontSize: '0.75rem',
                  fontWeight: '600'
                }}>
                  {apt.status}
                </span>
              </div>
              Patient ID: {apt.patientId} | Doctor ID: {apt.doctorId}<br />
              Reason: {apt.reason}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default AdminPortal;
