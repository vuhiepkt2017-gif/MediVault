package com.example.medivault.data.repository

import com.example.medivault.data.database.Member
import com.example.medivault.data.database.MemberDao
import com.example.medivault.data.database.Medication
import com.example.medivault.data.database.Prescription
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val memberDao: MemberDao) {

    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()

    fun getMemberById(memberId: Long): Flow<Member?> = memberDao.getMemberById(memberId)

    suspend fun insertMember(member: Member): Long = memberDao.insertMember(member)

    suspend fun updateMember(member: Member) = memberDao.updateMember(member)

    suspend fun deleteMember(member: Member) = memberDao.deleteMember(member)

    fun getPrescriptionsByMember(memberId: Long): Flow<List<Prescription>> =
        memberDao.getPrescriptionsByMember(memberId)

    suspend fun insertPrescription(prescription: Prescription): Long =
        memberDao.insertPrescription(prescription)

    suspend fun deletePrescription(prescription: Prescription) =
        memberDao.deletePrescription(prescription)

    fun getMedicationsByMember(memberId: Long): Flow<List<Medication>> =
        memberDao.getMedicationsByMember(memberId)

    suspend fun insertMedication(medication: Medication): Long =
        memberDao.insertMedication(medication)

    suspend fun updateMedication(medication: Medication) =
        memberDao.updateMedication(medication)

    suspend fun deleteMedication(medication: Medication) =
        memberDao.deleteMedication(medication)
}
