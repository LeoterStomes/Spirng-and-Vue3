import request from '@/utils/request'

export function getDoctorDashboard() {
  return request.get('/doctor/dashboard')
}

export function getMyPatients(params = {}) {
  return request.get('/doctor/patients', params)
}

export function bindPatient(keyword) {
  return request.post('/doctor/patients/bind', null, {
    params: { keyword },
    successMsg: '绑定成功'
  })
}

export function unbindPatient(patientId) {
  return request.post(`/doctor/patients/${patientId}/unbind`, null, {
    successMsg: '已解绑'
  })
}

export function getPatientProfile(patientId) {
  return request.get(`/doctor/patients/${patientId}/profile`)
}

export function updatePatientProfile(patientId, data) {
  return request.put(`/doctor/patients/${patientId}/profile`, data, {
    successMsg: '档案已更新'
  })
}

export function getPatientOverview(patientId) {
  return request.get(`/doctor/patients/${patientId}/overview`)
}

export function getPatientDiaries(patientId, params = {}) {
  return request.get(`/doctor/patients/${patientId}/diaries`, params)
}

export function getPatientSessions(patientId, params = {}) {
  return request.get(`/doctor/patients/${patientId}/sessions`, params)
}

export function getPatientReports(patientId, params = {}) {
  return request.get(`/doctor/patients/${patientId}/reports`, params)
}

export function addClinicalNote(patientId, data) {
  return request.post(`/doctor/patients/${patientId}/notes`, data, {
    successMsg: '备注已添加'
  })
}

export function getClinicalNotes(patientId, params = {}) {
  return request.get(`/doctor/patients/${patientId}/notes`, params)
}
