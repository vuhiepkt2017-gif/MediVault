package com.example.medivault.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dob: String, // format: YYYY-MM-DD
    val gender: String, // Male, Female, Other
    val bloodType: String, // A+, B-, etc.
    val allergies: String, // Comma-separated or free text
    val medicalHistory: String,
    val underlyingDiseases: String
)

@Entity(
    tableName = "prescriptions",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memberId"])]
)
data class Prescription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val imagePath: String, // Path to local internal storage file
    val date: String, // Date doctor prescribed
    val notes: String = ""
)

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memberId"])]
)
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val prescriptionId: Long? = null, // link to prescription if scanned
    val name: String,
    val dosage: String, // e.g. "1 pill"
    val frequency: String, // e.g. "Twice daily"
    val status: String = "Active", // Active, Inactive
    val dateAdded: String
)
