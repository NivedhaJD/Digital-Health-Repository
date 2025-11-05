/* Minimal frontend JS: compact, matches new HTML IDs and minimal monochrome UI */
const BASE = '';
const api = {
  get: (path) => fetch(path).then(r => r.ok ? r.json() : Promise.reject(r)),
  post: (path, data) => fetch(path, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(data)}).then(r => r.ok ? r.json() : r.text().then(t=>Promise.reject(t)))
};

const el = id => document.getElementById(id);

async function loadDoctorsInto(selectId){
  const sel = el(selectId); if(!sel) return;
  try{ const docs = await api.get('/api/doctors'); sel.innerHTML=''; docs.forEach(d=>{ const o=document.createElement('option'); o.value=d.id||d.doctorId||d.id; o.textContent = `${d.name} ${d.specialty?('— '+d.specialty):''} (${o.value})`; sel.appendChild(o); }); }catch(e){ console.warn('loadDoctors',e); }
}

// Patient registration
if(el('registerPatient')){
  el('registerPatient').addEventListener('click', async ()=>{
    const name = el('patientName').value.trim(); const age = Number(el('patientAge').value)||0; const gender = el('patientGender').value; const contact = el('patientContact').value.trim();
    try{ const res = await api.post('/api/patients/register',{name,age,gender,contact}); el('registerResult').textContent = 'Registered: ' + (res.id||res.patientId||JSON.stringify(res)); }catch(e){ el('registerResult').textContent = 'Error'; }
  });
}

// Book appointment
if(el('bookAppointment')){
  loadDoctorsInto('bookDoctorId');
  el('bookAppointment').addEventListener('click', async ()=>{
    const patientId = el('bookPatientId').value.trim(); const doctorId = el('bookDoctorId').value; const dateTime = el('bookDateTime').value;
    try{ const res = await api.post('/api/appointments/book',{patientId,doctorId,dateTime}); el('bookResult').textContent = 'Booked: ' + (res.id||res.appointmentId||JSON.stringify(res)); }catch(e){ el('bookResult').textContent = 'Error'; }
  });
}

// Load patient appointments
if(el('loadAppointments')){
  el('loadAppointments').addEventListener('click', async ()=>{
    const pid = el('viewPatientId').value.trim(); const wrap = el('appointmentsList'); wrap.innerHTML='';
    try{ const list = await api.get(`/api/appointments?patientId=${encodeURIComponent(pid)}`); if(!list.length) { wrap.textContent='(no appointments)'; return; } list.forEach(a=>{ const d=document.createElement('div'); d.className='item'; d.textContent = `${a.id||a.appointmentId} • ${a.doctorId||a.doctor} • ${a.dateTime||a.appointmentDateTime} • ${a.status||''}`; wrap.appendChild(d); }); }catch(e){ wrap.textContent='Error'; }
  });
}

// Doctor register
if(el('registerDoctor')){
  el('registerDoctor').addEventListener('click', async ()=>{
    const name = el('doctorName').value.trim(); const specialty = el('doctorSpecialty').value.trim();
    try{ const res = await api.post('/api/doctors/register',{name,specialty}); el('doctorResult').textContent = 'Registered: ' + (res.id||res.doctorId||JSON.stringify(res)); loadDoctorsInto('bookDoctorId'); }catch(e){ el('doctorResult').textContent='Error'; }
  });
}

// Doctor load appointments
if(el('loadDoctorAppointments')){
  el('loadDoctorAppointments').addEventListener('click', async ()=>{
    const did = el('viewDoctorId').value.trim(); const wrap = el('doctorAppointments'); wrap.innerHTML='';
    try{ const list = await api.get(`/api/appointments?doctorId=${encodeURIComponent(did)}`); if(!list.length){ wrap.textContent='(no appointments)'; return; } list.forEach(a=>{ const d=document.createElement('div'); d.className='item'; d.textContent = `${a.id||a.appointmentId} • ${a.patientId||a.patient} • ${a.dateTime||a.appointmentDateTime} • ${a.status||''}`; wrap.appendChild(d); }); }catch(e){ wrap.textContent='Error'; }
  });
}

// Add health record
if(el('addHealthRecord')){
  el('addHealthRecord').addEventListener('click', async ()=>{
    const patientId = el('recordPatientId').value.trim(); const doctorId = (el('viewDoctorId')?el('viewDoctorId').value.trim():''); const symptoms = el('symptoms').value.trim(); const diagnosis = el('diagnosis').value.trim(); const prescription = el('prescription')?el('prescription').value.trim():'';
    try{ const res = await api.post('/api/health-records/add',{patientId,doctorId,symptoms,diagnosis,prescription}); el('healthRecordResult').textContent='Saved'; }catch(e){ el('healthRecordResult').textContent='Error'; }
  });
}

// Admin loaders
if(el('loadPatients')){ el('loadPatients').addEventListener('click', async ()=>{ const out=el('patientsList'); out.innerHTML=''; try{ const list = await api.get('/api/patients'); list.forEach(p=>{ const d=document.createElement('div'); d.className='item'; d.textContent = `${p.id||p.patientId} • ${p.name}`; out.appendChild(d); }); }catch(e){ out.textContent='Error'; } }); }
if(el('loadDoctors')){ el('loadDoctors').addEventListener('click', async ()=>{ const out=el('doctorsList'); out.innerHTML=''; try{ const list = await api.get('/api/doctors'); list.forEach(d=>{ const n=document.createElement('div'); n.className='item'; n.textContent = `${d.id||d.doctorId} • ${d.name} • ${d.specialty||''}`; out.appendChild(n); }); }catch(e){ out.textContent='Error'; } }); }
if(el('loadAllAppointments')){ el('loadAllAppointments').addEventListener('click', async ()=>{ const out=el('allAppointments'); out.innerHTML=''; try{ const list = await api.get('/api/appointments'); list.forEach(a=>{ const n=document.createElement('div'); n.className='item'; n.textContent = `${a.id||a.appointmentId} • ${a.patientId||a.patient} • ${a.doctorId||a.doctor} • ${a.dateTime||a.appointmentDateTime}`; out.appendChild(n); }); }catch(e){ out.textContent='Error'; } }); }

document.addEventListener('DOMContentLoaded', ()=>{ if(el('bookDoctorId')) loadDoctorsInto('bookDoctorId'); });
