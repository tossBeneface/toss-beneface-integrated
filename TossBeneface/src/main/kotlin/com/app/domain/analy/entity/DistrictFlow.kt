package com.app.domain.analy.entity

import com.app.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
class DistrictFlow(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column
    var storeGenderScript: String? = null,

    @Column
    var storeAgeScript: String? = null,

    @Column
    var storeTimeScript: String? = null,

    @Column
    var storeDayScript: String? = null,

    @Column
    var districtGenderScript: String? = null,

    @Column
    var districtAgeScript: String? = null,

    @Column
    var districtTimeScript: String? = null,

    @Column
    var districtDayScript: String? = null
) : BaseEntity()
