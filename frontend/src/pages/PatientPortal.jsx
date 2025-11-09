import React, { useState, useEffect } from 'react';
import { patientAPI, appointmentAPI, healthRecordAPI, doctorAPI } from '../services/api';

function PatientPortal() {
  const [view, setView] = useState('register'); // register, appointments, records
  const [patients, setPatients] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [healthRecords, setHealthRecords] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [hasLinkedPatient, setHasLinkedPatient] = useState(false);
  
  // Form states
  const [patientForm, setPatientForm] = useState({
    name: '', age: '', gender: '', contact: '', address: '', medicalHistory: ''
  });
  const [appointmentForm, setAppointmentForm] = useState({
    patientId: '', doctorId: '', dateTime: '', reason: ''
  });

  useEffect(() => {
    loadPatients();
    loadDoctors();
    checkLinkedPatient();
  }, []);

  const checkLinkedPatient = async () => {
    const userData = JSON.parse(localStorage.getItem('userData') || '{}');
    const linkedEntityId = userData.linkedEntityId;
    
    if (linkedEntityId && linkedEntityId.startsWith('P')) {
      setHasLinkedPatient(true);
      try {
        const patient = await patientAPI.getById(linkedEntityId);
        selectPatient(patient);
      } catch (error) {
        console.error('Failed to load linked patient:', error);
      }
    }
  };

  const loadPatients = async () => {
    try {
      const userData = JSON.parse(localStorage.getItem('userData') || '{}');
      const linkedEntityId = userData.linkedEntityId;
      
      // If user has a linked patient, only load that patient
      if (linkedEntityId && linkedEntityId.startsWith('P')) {
        const patient = await patientAPI.getById(linkedEntityId);
        setPatients([patient]);
      } else {
        // Otherwise load all patients (for admin/staff view)
        const data = await patientAPI.getAll();
        setPatients(data);
      }
    } catch (error) {
      showMessage('Failed to load patients', 'error');
    }
  };

  const loadDoctors = async () => {
    try {
      const data = await doctorAPI.getAll();
      setDoctors(data);
    } catch (error) {
      showMessage('Failed to load doctors', 'error');
    }
  };

  const loadAppointments = async (patientId) => {
    try {
      const data = await appointmentAPI.getByPatient(patientId);
      setAppointments(data);
    } catch (error) {
      showMessage('Failed to load appointments', 'error');
    }
  };

  const loadHealthRecords = async (patientId) => {
    try {
      const data = await healthRecordAPI.getByPatient(patientId);
      setHealthRecords(data);
    } catch (error) {
      showMessage('Failed to load health records', 'error');
    }
  };

  const showMessage = (text, type = 'info') => {
    setMessage({ text, type });
    setTimeout(() => setMessage({ text: '', type: '' }), 5000);
  };

  const handleRegisterPatient = async (e) => {
    e.preventDefault();
    try {
      const userData = JSON.parse(localStorage.getItem('userData') || '{}');
      const userId = userData.userId;
      
      const response = await patientAPI.register({
        ...patientForm,
        age: parseInt(patientForm.age),
        userId: userId
      });
      showMessage('Patient registered successfully', 'success');
      setPatientForm({ name: '', age: '', gender: '', contact: '', address: '', medicalHistory: '' });
      setHasLinkedPatient(true);
      
      // Update linkedEntityId in localStorage
      if (response.patientId) {
        userData.linkedEntityId = response.patientId;
        localStorage.setItem('userData', JSON.stringify(userData));
        
        // Fetch the complete patient object and select it
        const patient = await patientAPI.getById(response.patientId);
        selectPatient(patient);
      }
      
      await loadPatients();
    } catch (error) {
      showMessage(error.message || 'Failed to register patient', 'error');
    }
  };

  const handleBookAppointment = async (e) => {
    e.preventDefault();
    try {
      console.log('=== BOOKING APPOINTMENT ===');
      console.log('appointmentForm:', appointmentForm);
      console.log('selectedPatient:', selectedPatient);
      console.log('patientId in form:', appointmentForm.patientId);
      
      await appointmentAPI.book(appointmentForm);
      showMessage('Appointment booked successfully', 'success');
      // Reset form but keep patientId for same patient
      setAppointmentForm(prev => ({ patientId: prev.patientId, doctorId: '', dateTime: '', reason: '' }));
      if (selectedPatient) {
        loadAppointments(selectedPatient.patientId);
      }
    } catch (error) {
      console.error('Appointment booking error:', error);
      showMessage(error.message || 'Failed to book appointment', 'error');
    }
  };

  const selectPatient = (patient) => {
    console.log('=== SELECTING PATIENT ===');
    console.log('Patient object:', patient);
    console.log('Patient ID:', patient.patientId);
    
    setSelectedPatient(patient);
    setAppointmentForm(prev => {
      const newForm = { ...prev, patientId: patient.patientId };
      console.log('Updated appointment form:', newForm);
      return newForm;
    });
    loadAppointments(patient.patientId);
    loadHealthRecords(patient.patientId);
    // Only set view to appointments if we're currently in the register view
    // This allows switching between appointments and health records
    setView(currentView => currentView === 'register' ? 'appointments' : currentView);
  };

  return (
    <div className="container" style={{ marginTop: '2rem', marginBottom: '2rem' }}>
      <h1>Patient Portal</h1>
      
      {message.text && (
        <div className={`message message-${message.type}`}>{message.text}</div>
      )}

      <div className="btn-group" style={{ margin: '2rem 0' }}>
        {!hasLinkedPatient && (
          <button 
            onClick={() => setView('register')} 
            className={view === 'register' ? 'btn btn-primary' : 'btn btn-secondary'}
          >
            Register Patient
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
          Health Records
        </button>
      </div>

      {view === 'register' && (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Register New Patient</h2>
          </div>
          <form onSubmit={handleRegisterPatient}>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Name</label>
                <input 
                  type="text" 
                  className="form-input" 
                  value={patientForm.name}
                  onChange={(e) => setPatientForm({...patientForm, name: e.target.value})}
                  required 
                />
              </div>
              <div className="form-group">
                <label className="form-label">Age</label>
                <input 
                  type="number" 
                  className="form-input" 
                  value={patientForm.age}
                  onChange={(e) => setPatientForm({...patientForm, age: e.target.value})}
                  required 
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Gender</label>
                <select 
                  className="form-select"
                  value={patientForm.gender}
                  onChange={(e) => setPatientForm({...patientForm, gender: e.target.value})}
                  required
                >
                  <option value="">Select Gender</option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Contact</label>
                <input 
                  type="text" 
                  className="form-input" 
                  value={patientForm.contact}
                  onChange={(e) => setPatientForm({...patientForm, contact: e.target.value})}
                  required 
                />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Address</label>
              <input 
                type="text" 
                className="form-input" 
                value={patientForm.address}
                onChange={(e) => setPatientForm({...patientForm, address: e.target.value})}
                required 
              />
            </div>
            <div className="form-group">
              <label className="form-label">Medical History</label>
              <textarea 
                className="form-textarea" 
                value={patientForm.medicalHistory}
                onChange={(e) => setPatientForm({...patientForm, medicalHistory: e.target.value})}
              />
            </div>
            <button type="submit" className="btn btn-primary">Register Patient</button>
          </form>
        </div>
      )}

      {view === 'appointments' && (
        <>
          {!selectedPatient ? (
            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Select a Patient</h2>
              </div>
              {patients.map(patient => (
                <div key={patient.patientId} className="list-item" onClick={() => selectPatient(patient)} style={{ cursor: 'pointer' }}>
                  <strong>{patient.name}</strong> - {patient.age} years, {patient.gender}
                  <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)' }}>ID: {patient.patientId}</div>
                </div>
              ))}
            </div>
          ) : (
            <>
              <div className="card">
                <div className="card-header">
                  <h2 className="card-title">Book Appointment</h2>
                  <p className="card-subtitle">Patient: {selectedPatient.name} (ID: {selectedPatient.patientId})</p>
                </div>
                <form onSubmit={handleBookAppointment}>
                  <input type="hidden" value={appointmentForm.patientId} />
                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label">Doctor</label>
                      <select 
                        className="form-select"
                        value={appointmentForm.doctorId}
                        onChange={(e) => setAppointmentForm(prev => ({...prev, doctorId: e.target.value}))}
                        required
                      >
                        <option value="">Select Doctor</option>
                        {doctors.map(doc => (
                          <option key={doc.doctorId} value={doc.doctorId}>
                            Dr. {doc.name} - {doc.specialty}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Date & Time</label>
                      <input 
                        type="datetime-local" 
                        className="form-input" 
                        value={appointmentForm.dateTime}
                        onChange={(e) => setAppointmentForm(prev => ({...prev, dateTime: e.target.value}))}
                        min={new Date().toISOString().slice(0, 16)}
                        required 
                      />
                      <small style={{ color: 'var(--gray-600)', fontSize: '0.75rem', display: 'block', marginTop: '0.25rem' }}>
                        Available slots: Every hour from 9:00 AM to 5:00 PM (e.g., 09:00, 10:00, 11:00... 17:00)
                      </small>
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Reason</label>
                    <textarea 
                      className="form-textarea" 
                      value={appointmentForm.reason}
                      onChange={(e) => setAppointmentForm(prev => ({...prev, reason: e.target.value}))}
                      required
                    />
                  </div>
                  <button type="submit" className="btn btn-primary">Book Appointment</button>
                  <button type="button" onClick={() => setSelectedPatient(null)} className="btn btn-secondary" style={{ marginLeft: '1rem' }}>
                    Back to Patient List
                  </button>
                </form>
              </div>

              <div className="card">
                <div className="card-header">
                  <h2 className="card-title">Appointment History</h2>
                </div>
                {appointments.length === 0 ? (
                  <p style={{ color: 'var(--gray-500)' }}>No appointments found</p>
                ) : (
                  appointments.map(apt => (
                    <div key={apt.appointmentId} className="list-item">
                      <strong>{apt.dateTime}</strong><br />
                      Doctor ID: {apt.doctorId} | Status: {apt.status}<br />
                      Reason: {apt.reason}
                    </div>
                  ))
                )}
              </div>
            </>
          )}
        </>
      )}

      {view === 'records' && (
        <>
          {!selectedPatient ? (
            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Select a Patient</h2>
              </div>
              {patients.map(patient => (
                <div key={patient.patientId} className="list-item" onClick={() => selectPatient(patient)} style={{ cursor: 'pointer' }}>
                  <strong>{patient.name}</strong> - {patient.age} years, {patient.gender}
                  <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)' }}>ID: {patient.patientId}</div>
                </div>
              ))}
            </div>
          ) : (
            <>
              <div className="card">
                <div className="card-header">
                  <h2 className="card-title">Health Records</h2>
                  <p className="card-subtitle">Patient: {selectedPatient.name}</p>
                </div>
                <button type="button" onClick={() => setSelectedPatient(null)} className="btn btn-secondary">
                  Back to Patient List
                </button>
                <div style={{ marginTop: '1.5rem' }}>
                  {healthRecords.length === 0 ? (
                    <p style={{ color: 'var(--gray-500)' }}>No health records found</p>
                  ) : (
                    healthRecords.map(record => (
                      <div key={record.recordId} className="list-item">
                        <strong>Record Date: {new Date(record.date).toLocaleDateString()}</strong><br />
                        {record.symptoms && <><strong>Symptoms:</strong> {record.symptoms}<br /></>}
                        <strong>Diagnosis:</strong> {record.diagnosis}<br />
                        <strong>Treatment:</strong> {record.treatment}<br />
                        {record.prescription && <><strong>Prescription:</strong> {record.prescription}</>}
                      </div>
                    ))
                  )}
                </div>
              </div>
            </>
          )}
        </>
      )}
    </div>
  );
}

export default PatientPortal;
