import React, { useState, useEffect } from 'react';
import { doctorAPI, appointmentAPI, healthRecordAPI, patientAPI } from '../services/api';

function DoctorPortal() {
  const [view, setView] = useState('register'); // register, appointments, records
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [patients, setPatients] = useState([]);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [hasLinkedDoctor, setHasLinkedDoctor] = useState(false);
  
  // Form states
  const [doctorForm, setDoctorForm] = useState({
    name: '', specialty: '', contact: '', email: '', schedule: ''
  });
  const [recordForm, setRecordForm] = useState({
    patientId: '', doctorId: '', symptoms: '', diagnosis: '', treatment: '', prescription: '', recordDate: ''
  });

  useEffect(() => {
    loadDoctors();
    loadPatients();
    checkLinkedDoctor();
  }, []);

  const checkLinkedDoctor = async () => {
    const userData = JSON.parse(localStorage.getItem('userData') || '{}');
    const linkedEntityId = userData.linkedEntityId;
    
    if (linkedEntityId && linkedEntityId.startsWith('D')) {
      setHasLinkedDoctor(true);
      try {
        const doctor = await doctorAPI.getById(linkedEntityId);
        selectDoctor(doctor);
      } catch (error) {
        console.error('Failed to load linked doctor:', error);
      }
    }
  };

  const loadDoctors = async () => {
    try {
      const userData = JSON.parse(localStorage.getItem('userData') || '{}');
      const linkedEntityId = userData.linkedEntityId;
      
      // If user has a linked doctor, only load that doctor
      if (linkedEntityId && linkedEntityId.startsWith('D')) {
        const doctor = await doctorAPI.getById(linkedEntityId);
        setDoctors([doctor]);
      } else {
        // Otherwise load all doctors (for admin/staff view)
        const data = await doctorAPI.getAll();
        setDoctors(data);
      }
    } catch (error) {
      showMessage('Failed to load doctors', 'error');
    }
  };

  const loadPatients = async () => {
    try {
      const data = await patientAPI.getAll();
      setPatients(data);
    } catch (error) {
      showMessage('Failed to load patients', 'error');
    }
  };

  const loadAppointments = async (doctorId) => {
    try {
      const data = await appointmentAPI.getByDoctor(doctorId);
      setAppointments(data);
    } catch (error) {
      showMessage('Failed to load appointments', 'error');
    }
  };

  const showMessage = (text, type = 'info') => {
    setMessage({ text, type });
    setTimeout(() => setMessage({ text: '', type: '' }), 5000);
  };

  const handleRegisterDoctor = async (e) => {
    e.preventDefault();
    try {
      const userData = JSON.parse(localStorage.getItem('userData') || '{}');
      const userId = userData.userId;
      
      const response = await doctorAPI.register({
        ...doctorForm,
        userId: userId
      });
      showMessage('Doctor registered successfully', 'success');
      setDoctorForm({ name: '', specialty: '', contact: '', email: '', schedule: '' });
      setHasLinkedDoctor(true);
      
      // Update linkedEntityId in localStorage
      if (response.doctorId) {
        userData.linkedEntityId = response.doctorId;
        localStorage.setItem('userData', JSON.stringify(userData));
        
        // Fetch the complete doctor object and select it
        const doctor = await doctorAPI.getById(response.doctorId);
        selectDoctor(doctor);
      }
      
      await loadDoctors();
    } catch (error) {
      showMessage(error.message || 'Failed to register doctor', 'error');
    }
  };

  const handleAddHealthRecord = async (e) => {
    e.preventDefault();
    try {
      await healthRecordAPI.add(recordForm);
      showMessage('Health record added successfully', 'success');
      setRecordForm({ patientId: '', doctorId: '', symptoms: '', diagnosis: '', treatment: '', prescription: '', recordDate: '' });
    } catch (error) {
      showMessage(error.message || 'Failed to add health record', 'error');
    }
  };

  const selectDoctor = (doctor) => {
    setSelectedDoctor(doctor);
    loadAppointments(doctor.doctorId);
    setView('appointments');
  };

  const updateAppointmentStatus = async (appointmentId, status) => {
    try {
      await appointmentAPI.updateStatus(appointmentId, status);
      showMessage('Appointment status updated', 'success');
      if (selectedDoctor) {
        loadAppointments(selectedDoctor.doctorId);
      }
    } catch (error) {
      showMessage(error.message || 'Failed to update appointment status', 'error');
    }
  };

  return (
    <div className="container" style={{ marginTop: '2rem', marginBottom: '2rem' }}>
      <h1>Doctor Portal</h1>
      
      {message.text && (
        <div className={`message message-${message.type}`}>{message.text}</div>
      )}

      <div className="btn-group" style={{ margin: '2rem 0' }}>
        {!hasLinkedDoctor && (
          <button 
            onClick={() => setView('register')} 
            className={view === 'register' ? 'btn btn-primary' : 'btn btn-secondary'}
          >
            Register Doctor
          </button>
        )}
        <button 
          onClick={() => setView('appointments')} 
          className={view === 'appointments' ? 'btn btn-primary' : 'btn btn-secondary'}
        >
          Appointments
        </button>
        <button 
          onClick={() => setView('records')} 
          className={view === 'records' ? 'btn btn-primary' : 'btn btn-secondary'}
        >
          Add Health Record
        </button>
      </div>

      {view === 'register' && (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Register New Doctor</h2>
          </div>
          <form onSubmit={handleRegisterDoctor}>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Name</label>
                <input 
                  type="text" 
                  className="form-input" 
                  value={doctorForm.name}
                  onChange={(e) => setDoctorForm({...doctorForm, name: e.target.value})}
                  required 
                />
              </div>
              <div className="form-group">
                <label className="form-label">Specialty</label>
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="e.g., Cardiology, Pediatrics"
                  value={doctorForm.specialty}
                  onChange={(e) => setDoctorForm({...doctorForm, specialty: e.target.value})}
                  required 
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Contact</label>
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="Phone number"
                  value={doctorForm.contact}
                  onChange={(e) => setDoctorForm({...doctorForm, contact: e.target.value})}
                  required 
                />
              </div>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input 
                  type="email" 
                  className="form-input" 
                  placeholder="doctor@example.com"
                  value={doctorForm.email}
                  onChange={(e) => setDoctorForm({...doctorForm, email: e.target.value})}
                  required 
                />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Schedule</label>
              <input 
                type="text" 
                className="form-input" 
                placeholder="e.g., Mon-Fri 9AM-5PM"
                value={doctorForm.schedule}
                onChange={(e) => setDoctorForm({...doctorForm, schedule: e.target.value})}
                required 
              />
            </div>
            <button type="submit" className="btn btn-primary">Register Doctor</button>
          </form>
        </div>
      )}

      {view === 'appointments' && (
        <>
          {!selectedDoctor ? (
            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Select a Doctor</h2>
              </div>
              {doctors.map(doctor => (
                <div key={doctor.doctorId} className="list-item" onClick={() => selectDoctor(doctor)} style={{ cursor: 'pointer' }}>
                  <strong>Dr. {doctor.name}</strong> - {doctor.specialty}<br />
                  <span style={{ fontSize: '0.875rem', color: 'var(--gray-500)' }}>
                    📞 {doctor.contact} | ✉️ {doctor.email}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <>
              <div className="card">
                <div className="card-header">
                  <h2 className="card-title">Appointments</h2>
                  <p className="card-subtitle">Dr. {selectedDoctor.name} - {selectedDoctor.specialty}</p>
                </div>
                <button type="button" onClick={() => setSelectedDoctor(null)} className="btn btn-secondary">
                  Back to Doctor List
                </button>
                <div style={{ marginTop: '1.5rem' }}>
                  {appointments.length === 0 ? (
                    <p style={{ color: 'var(--gray-500)' }}>No appointments found</p>
                  ) : (
                    appointments.map(apt => (
                      <div key={apt.appointmentId} className="list-item">
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
                        Patient ID: {apt.patientId}<br />
                        Reason: {apt.reason}<br />
                        <div className="btn-group" style={{ marginTop: '0.75rem' }}>
                          <button 
                            onClick={() => updateAppointmentStatus(apt.appointmentId, 'CONFIRMED')} 
                            className="btn btn-secondary"
                            style={{ fontSize: '0.75rem', padding: '0.5rem 1rem' }}
                            disabled={apt.status === 'COMPLETED'}
                          >
                            Confirm
                          </button>
                          <button 
                            onClick={() => updateAppointmentStatus(apt.appointmentId, 'COMPLETED')} 
                            className="btn btn-primary"
                            style={{ fontSize: '0.75rem', padding: '0.5rem 1rem' }}
                            disabled={apt.status === 'COMPLETED'}
                          >
                            Complete
                          </button>
                          <button 
                            onClick={() => updateAppointmentStatus(apt.appointmentId, 'CANCELLED')} 
                            className="btn btn-outline"
                            style={{ fontSize: '0.75rem', padding: '0.5rem 1rem' }}
                            disabled={apt.status === 'COMPLETED'}
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </>
          )}
        </>
      )}

      {view === 'records' && (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Add Health Record</h2>
          </div>
          <form onSubmit={handleAddHealthRecord}>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Patient</label>
                <select 
                  className="form-select"
                  value={recordForm.patientId}
                  onChange={(e) => setRecordForm({...recordForm, patientId: e.target.value})}
                  required
                >
                  <option value="">Select Patient</option>
                  {patients.map(patient => (
                    <option key={patient.patientId} value={patient.patientId}>
                      {patient.name} (ID: {patient.patientId})
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Doctor</label>
                <select 
                  className="form-select"
                  value={recordForm.doctorId}
                  onChange={(e) => setRecordForm({...recordForm, doctorId: e.target.value})}
                  required
                >
                  <option value="">Select Doctor</option>
                  {doctors.map(doctor => (
                    <option key={doctor.doctorId} value={doctor.doctorId}>
                      Dr. {doctor.name} - {doctor.specialty}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Record Date</label>
              <input 
                type="date" 
                className="form-input" 
                value={recordForm.recordDate}
                onChange={(e) => setRecordForm({...recordForm, recordDate: e.target.value})}
                required 
              />
            </div>
            <div className="form-group">
              <label className="form-label">Symptoms</label>
              <textarea 
                className="form-textarea" 
                placeholder="Patient's reported symptoms"
                value={recordForm.symptoms}
                onChange={(e) => setRecordForm({...recordForm, symptoms: e.target.value})}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Diagnosis</label>
              <textarea 
                className="form-textarea" 
                placeholder="Medical diagnosis"
                value={recordForm.diagnosis}
                onChange={(e) => setRecordForm({...recordForm, diagnosis: e.target.value})}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Treatment</label>
              <textarea 
                className="form-textarea" 
                placeholder="Recommended treatment plan"
                value={recordForm.treatment}
                onChange={(e) => setRecordForm({...recordForm, treatment: e.target.value})}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Prescription</label>
              <textarea 
                className="form-textarea" 
                placeholder="Prescribed medications (optional)"
                value={recordForm.prescription}
                onChange={(e) => setRecordForm({...recordForm, prescription: e.target.value})}
              />
            </div>
            <button type="submit" className="btn btn-primary">Add Health Record</button>
          </form>
        </div>
      )}
    </div>
  );
}

export default DoctorPortal;
