import React, { useState, useEffect } from 'react';
import { patientAPI, doctorAPI, appointmentAPI, healthRecordAPI, authAPI } from '../services/api';
import { useNavigate } from 'react-router-dom';
import './AdminPortal.css';

function AdminPortal() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');
  const [stats, setStats] = useState({
    patients: 0,
    doctors: 0,
    appointments: 0,
    healthRecords: 0
  });
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [healthRecords, setHealthRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    // Check if user is admin
    const userData = authAPI.getUserData();
    if (!userData || userData.role !== 'ADMIN') {
      navigate('/login');
      return;
    }
    loadAllData();
  }, [navigate]);

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
      setHealthRecords(recordsData);
      
      setStats({
        patients: patientsData.length,
        doctors: doctorsData.length,
        appointments: appointmentsData.length,
        healthRecords: recordsData.length
      });
    } catch (error) {
      console.error('Failed to load data:', error);
      showMessage('error', 'Failed to load data: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 5000);
  };

  const handleDeletePatient = async (patientId, patientName) => {
    if (!window.confirm(`Are you sure you want to delete patient "${patientName}" (${patientId})?`)) {
      return;
    }

    try {
      await patientAPI.delete(patientId);
      showMessage('success', 'Patient deleted successfully');
      loadAllData();
    } catch (error) {
      showMessage('error', 'Failed to delete patient: ' + error.message);
    }
  };

  const handleDeleteDoctor = async (doctorId, doctorName) => {
    if (!window.confirm(`Are you sure you want to delete doctor "${doctorName}" (${doctorId})?`)) {
      return;
    }

    try {
      await doctorAPI.delete(doctorId);
      showMessage('success', 'Doctor deleted successfully');
      loadAllData();
    } catch (error) {
      showMessage('error', 'Failed to delete doctor: ' + error.message);
    }
  };

  const handleDeleteAppointment = async (appointmentId) => {
    if (!window.confirm(`Are you sure you want to delete appointment ${appointmentId}?`)) {
      return;
    }

    try {
      await appointmentAPI.delete(appointmentId);
      showMessage('success', 'Appointment deleted successfully');
      loadAllData();
    } catch (error) {
      showMessage('error', 'Failed to delete appointment: ' + error.message);
    }
  };

  const handleDeleteHealthRecord = async (recordId) => {
    if (!window.confirm(`Are you sure you want to delete health record ${recordId}?`)) {
      return;
    }

    try {
      await healthRecordAPI.delete(recordId);
      showMessage('success', 'Health record deleted successfully');
      loadAllData();
    } catch (error) {
      showMessage('error', 'Failed to delete health record: ' + error.message);
    }
  };

  const handleLogout = () => {
    authAPI.clearAuth();
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="admin-container">
        <div className="loading-spinner">Loading...</div>
      </div>
    );
  }

  return (
    <div className="admin-container">
      <div className="admin-wrapper">
        <div className="admin-header">
          <h1>🛡️ Admin Dashboard</h1>
          <button onClick={handleLogout} className="logout-button">
            Logout
          </button>
        </div>

        {message.text && (
          <div className={`message ${message.type}`}>
            {message.type === 'success' ? '✅' : '❌'} {message.text}
          </div>
        )}

        <div className="admin-tabs">
        <button
          className={`tab ${activeTab === 'overview' ? 'active' : ''}`}
          onClick={() => setActiveTab('overview')}
        >
          Overview
        </button>
        <button
          className={`tab ${activeTab === 'patients' ? 'active' : ''}`}
          onClick={() => setActiveTab('patients')}
        >
          Patients ({stats.patients})
        </button>
        <button
          className={`tab ${activeTab === 'doctors' ? 'active' : ''}`}
          onClick={() => setActiveTab('doctors')}
        >
          Doctors ({stats.doctors})
        </button>
        <button
          className={`tab ${activeTab === 'appointments' ? 'active' : ''}`}
          onClick={() => setActiveTab('appointments')}
        >
          Appointments ({stats.appointments})
        </button>
        <button
          className={`tab ${activeTab === 'records' ? 'active' : ''}`}
          onClick={() => setActiveTab('records')}
        >
          Health Records ({stats.healthRecords})
        </button>
      </div>

      <div className="admin-content">
        {activeTab === 'overview' && (
          <div className="overview-grid">
            <div className="stat-card">
              <div className="stat-icon">👤</div>
              <div className="stat-value">{stats.patients}</div>
              <div className="stat-label">Total Patients</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">👨‍⚕️</div>
              <div className="stat-value">{stats.doctors}</div>
              <div className="stat-label">Total Doctors</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">📅</div>
              <div className="stat-value">{stats.appointments}</div>
              <div className="stat-label">Total Appointments</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">📋</div>
              <div className="stat-value">{stats.healthRecords}</div>
              <div className="stat-label">Health Records</div>
            </div>
          </div>
        )}

        {activeTab === 'patients' && (
          <div className="data-section">
            <div className="section-header">
              <h2>👤 Patients Management</h2>
              <button onClick={loadAllData} className="refresh-button">
                🔄 Refresh
              </button>
            </div>
            {patients.length === 0 ? (
              <div className="empty-state">No patients found</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Age</th>
                    <th>Gender</th>
                    <th>Contact</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {patients.map((patient) => (
                    <tr key={patient.patientId}>
                      <td><code>{patient.patientId}</code></td>
                      <td>{patient.name}</td>
                      <td>{patient.age}</td>
                      <td>{patient.gender}</td>
                      <td>{patient.contact}</td>
                      <td>
                        <button
                          onClick={() => handleDeletePatient(patient.patientId, patient.name)}
                          className="delete-button"
                        >
                          🗑️ Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === 'doctors' && (
          <div className="data-section">
            <div className="section-header">
              <h2>👨‍⚕️ Doctors Management</h2>
              <button onClick={loadAllData} className="refresh-button">
                🔄 Refresh
              </button>
            </div>
            {doctors.length === 0 ? (
              <div className="empty-state">No doctors found</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Specialty</th>
                    <th>Contact</th>
                    <th>Email</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {doctors.map((doctor) => (
                    <tr key={doctor.doctorId}>
                      <td><code>{doctor.doctorId}</code></td>
                      <td>{doctor.name}</td>
                      <td>{doctor.specialty}</td>
                      <td>{doctor.contact}</td>
                      <td>{doctor.email || 'N/A'}</td>
                      <td>
                        <button
                          onClick={() => handleDeleteDoctor(doctor.doctorId, doctor.name)}
                          className="delete-button"
                        >
                          🗑️ Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === 'appointments' && (
          <div className="data-section">
            <div className="section-header">
              <h2>📅 Appointments Management</h2>
              <button onClick={loadAllData} className="refresh-button">
                🔄 Refresh
              </button>
            </div>
            {appointments.length === 0 ? (
              <div className="empty-state">No appointments found</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Patient</th>
                    <th>Doctor</th>
                    <th>Date & Time</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((apt) => (
                    <tr key={apt.appointmentId}>
                      <td><code>{apt.appointmentId}</code></td>
                      <td><code>{apt.patientId}</code></td>
                      <td><code>{apt.doctorId}</code></td>
                      <td>{apt.dateTime}</td>
                      <td>
                        <span className={`status-badge ${apt.status.toLowerCase()}`}>
                          {apt.status}
                        </span>
                      </td>
                      <td>
                        <button
                          onClick={() => handleDeleteAppointment(apt.appointmentId)}
                          className="delete-button"
                        >
                          🗑️ Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === 'records' && (
          <div className="data-section">
            <div className="section-header">
              <h2>📋 Health Records Management</h2>
              <button onClick={loadAllData} className="refresh-button">
                🔄 Refresh
              </button>
            </div>
            {healthRecords.length === 0 ? (
              <div className="empty-state">No health records found</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Patient</th>
                    <th>Doctor</th>
                    <th>Date</th>
                    <th>Diagnosis</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {healthRecords.map((record) => (
                    <tr key={record.recordId}>
                      <td><code>{record.recordId}</code></td>
                      <td><code>{record.patientId}</code></td>
                      <td><code>{record.doctorId}</code></td>
                      <td>{record.date}</td>
                      <td>{record.diagnosis}</td>
                      <td>
                        <button
                          onClick={() => handleDeleteHealthRecord(record.recordId)}
                          className="delete-button"
                        >
                          🗑️ Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
      </div>
    </div>
  );
}

export default AdminPortal;

