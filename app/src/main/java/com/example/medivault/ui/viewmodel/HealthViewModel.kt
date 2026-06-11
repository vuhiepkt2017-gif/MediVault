package com.example.medivault.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medivault.data.database.AppDatabase
import com.example.medivault.data.database.Member
import com.example.medivault.data.database.Medication
import com.example.medivault.data.database.Prescription
import com.example.medivault.data.repository.HealthRepository
import com.example.medivault.utils.OcrAnalyzer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MedicineDetails(
    val name: String,
    val activeIngredient: String,
    val indication: String,
    val sideEffects: String,
    val usageInstructions: String
)

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HealthRepository = HealthRepository(AppDatabase.getDatabase(application).memberDao())
    val allMembers: StateFlow<List<Member>> = repository.allMembers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _selectedMemberId = MutableStateFlow<Long?>(null)
    val selectedMemberId: StateFlow<Long?> = _selectedMemberId

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMember: StateFlow<Member?> = _selectedMemberId
        .flatMapLatest { id ->
            if (id != null) repository.getMemberById(id) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMemberPrescriptions: StateFlow<List<Prescription>> = _selectedMemberId
        .flatMapLatest { id ->
            if (id != null) repository.getPrescriptionsByMember(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMemberMedications: StateFlow<List<Medication>> = _selectedMemberId
        .flatMapLatest { id ->
            if (id != null) repository.getMedicationsByMember(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<MedicineDetails>>(emptyList())
    val searchResults: StateFlow<List<MedicineDetails>> = _searchResults

    private val _isOcrProcessing = MutableStateFlow(false)
    val isOcrProcessing: StateFlow<Boolean> = _isOcrProcessing

    // Mock medicine database for lookup
    private val mockMedicineDb = listOf(
        MedicineDetails("Paracetamol", "Paracetamol (Acetaminophen) 500mg", "Giảm đau nhẹ đến vừa, hạ sốt (nhức đầu, đau răng, cảm cúm, sốt).", "Tổn thương gan nếu dùng quá liều hoặc dùng chung với bia rượu.", "Uống 1-2 viên mỗi 4-6 giờ khi cần. Không quá 8 viên/ngày."),
        MedicineDetails("Amoxicillin", "Amoxicillin 500mg (Kháng sinh)", "Điều trị các bệnh nhiễm trùng đường hô hấp, tai mũi họng, da, đường tiết niệu.", "Tiêu chảy, buồn nôn, nổi mẩn đỏ dị ứng da.", "Uống ngày 2-3 lần sau ăn, mỗi lần 1 viên theo chỉ định bác sĩ."),
        MedicineDetails("Ibuprofen", "Ibuprofen 400mg (Kháng viêm phi steroid)", "Giảm đau trung bình (đau khớp, đau răng, đau bụng kinh), giảm viêm sưng.", "Đau dạ dày, viêm loét dạ dày tá tràng, ợ nóng.", "Uống ngày 2-3 lần sau khi ăn no, mỗi lần 1 viên."),
        MedicineDetails("Cetirizine", "Cetirizine Hydrochloride 10mg", "Điều trị viêm mũi dị ứng, hắt hơi, sổ mũi, nổi mề đay, ngứa dị ứng.", "Buồn ngủ nhẹ, khô miệng, mệt mỏi.", "Uống 1 viên vào buổi tối trước khi đi ngủ."),
        MedicineDetails("Amlodipine", "Amlodipine 5mg", "Điều trị tăng huyết áp, đau thắt ngực ổn định.", "Phù cổ chân, nhức đầu, đỏ bừng mặt, chóng mặt.", "Uống 1 viên duy nhất vào một giờ cố định trong ngày (thường buổi sáng)."),
        MedicineDetails("Metformin", "Metformin Hydrochloride 850mg", "Kiểm soát đường huyết ở bệnh nhân tiểu đường tuýp 2.", "Rối loạn tiêu hóa (đầy hơi, tiêu chảy, chán ăn).", "Uống 1 viên ngay trong hoặc sau bữa ăn tối.")
    )

    fun selectMember(memberId: Long) {
        _selectedMemberId.value = memberId
    }

    fun addMember(
        name: String,
        dob: String,
        gender: String,
        bloodType: String,
        allergies: String,
        medicalHistory: String,
        underlyingDiseases: String
    ) {
        viewModelScope.launch {
            val newMember = Member(
                name = name,
                dob = dob,
                gender = gender,
                bloodType = bloodType,
                allergies = allergies,
                medicalHistory = medicalHistory,
                underlyingDiseases = underlyingDiseases
            )
            repository.insertMember(newMember)
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            if (_selectedMemberId.value == member.id) {
                _selectedMemberId.value = null
            }
            repository.deleteMember(member)
        }
    }

    fun addMedication(name: String, dosage: String, frequency: String) {
        val memberId = _selectedMemberId.value ?: return
        viewModelScope.launch {
            val newMed = Medication(
                memberId = memberId,
                name = name,
                dosage = dosage,
                frequency = frequency,
                dateAdded = LocalDate.now().toString()
            )
            repository.insertMedication(newMed)
        }
    }

    fun addPrescription(imagePath: String, date: String, notes: String) {
        val memberId = _selectedMemberId.value ?: return
        viewModelScope.launch {
            val newPrescription = Prescription(
                memberId = memberId,
                imagePath = imagePath,
                date = date,
                notes = notes
            )
            repository.insertPrescription(newPrescription)
        }
    }

    fun searchMedicine(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        val results = mockMedicineDb.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.activeIngredient.contains(query, ignoreCase = true)
        }
        _searchResults.value = results
    }

    fun processOcr(bitmap: Bitmap) {
        _isOcrProcessing.value = true
        OcrAnalyzer.analyzeImage(
            bitmap = bitmap,
            onSuccess = { textLines ->
                _isOcrProcessing.value = false
                // Match text with mock database names
                val detectedMedicines = mutableListOf<MedicineDetails>()
                for (line in textLines) {
                    val words = line.split("\\s+".toRegex())
                    for (word in words) {
                        val cleanedWord = word.replace("[^a-zA-Z]".toRegex(), "")
                        val matched = mockMedicineDb.firstOrNull {
                            it.name.equals(cleanedWord, ignoreCase = true)
                        }
                        if (matched != null && !detectedMedicines.contains(matched)) {
                            detectedMedicines.add(matched)
                        }
                    }
                }
                _searchResults.value = detectedMedicines
                if (detectedMedicines.isNotEmpty()) {
                    _searchQuery.value = detectedMedicines.first().name
                } else {
                    _searchQuery.value = "Không tìm thấy tên thuốc"
                }
            },
            onFailure = {
                _isOcrProcessing.value = false
                _searchQuery.value = "Lỗi nhận dạng ảnh"
            }
        )
    }
}
