package com.app.api.fastapi

data class HospitalResponse(
    var hospitalName: String? = null,
    var address: String? = null,
    var emergencyMedicalInstitutionType: String? = null,
    var phoneNumber1: String? = null,
    var phoneNumber3: String? = null,
    var request: String? = null,
    var distance: Double = 0.0
)
