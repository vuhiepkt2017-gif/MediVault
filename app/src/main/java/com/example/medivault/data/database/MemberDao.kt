package com.example.medivault.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    // Member Operations
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    fun getMemberById(memberId: Long): Flow<Member?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    // Prescription Operations
    @Query("SELECT * FROM prescriptions WHERE memberId = :memberId ORDER BY date DESC")
    fun getPrescriptionsByMember(memberId: Long): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: Prescription): Long

    @Delete
    suspend fun deletePrescription(prescription: Prescription)

    // Medication Operations
    @Query("SELECT * FROM medications WHERE memberId = :memberId ORDER BY dateAdded DESC")
    fun getMedicationsByMember(memberId: Long): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)
}
