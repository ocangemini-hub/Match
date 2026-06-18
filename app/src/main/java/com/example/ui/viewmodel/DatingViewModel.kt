package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatingDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DateProposal
import com.example.data.model.UserProfile
import com.example.data.repository.DatingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class DatingTab {
    DISCOVER, DATES, CHATS, PROFILE
}

class DatingViewModel(
    application: Application,
    private val repository: DatingRepository
) : AndroidViewModel(application) {

    // Tab Navigation
    private val _currentTab = MutableStateFlow(DatingTab.DISCOVER)
    val currentTab = _currentTab.asStateFlow()

    // Seeds and triggers
    init {
        viewModelScope.launch {
            repository.seedInitialData()
        }
    }

    // Discover (Swiping Feed)
    val discoverFeed: StateFlow<List<UserProfile>> = repository.discoverFeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Matches
    val matches: StateFlow<List<UserProfile>> = repository.matches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // My Profile
    val myProfile: StateFlow<UserProfile?> = repository.myProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Date proposals
    val dateProposals: StateFlow<List<DateProposal>> = repository.allDateProposals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Match overlay states
    private val _matchedProfile = MutableStateFlow<UserProfile?>(null)
    val matchedProfile = _matchedProfile.asStateFlow()

    fun clearMatchOverlay() {
        _matchedProfile.value = null
    }

    // Chat partner selection and message stream
    private val _activeChatPartner = MutableStateFlow<UserProfile?>(null)
    val activeChatPartner = _activeChatPartner.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessage>> = _activeChatPartner
        .flatMapLatest { partner ->
            if (partner != null) {
                repository.getChatMessages(partner.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Select tab
    fun selectTab(tab: DatingTab) {
        _currentTab.value = tab
    }

    // Swiping Actions
    fun likeCurrentProfile(profile: UserProfile) {
        viewModelScope.launch {
            val isMatched = repository.likeProfile(profile.id)
            if (isMatched) {
                // Trigger full screen high-polish overlapping match alert
                _matchedProfile.value = profile
            }
        }
    }

    fun rejectCurrentProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.rejectProfile(profile.id)
        }
    }

    // Date Proposal Actions
    fun createDateProposal(title: String, description: String, location: String, dateTime: String, category: String) {
        viewModelScope.launch {
            repository.createDateProposal(title, description, location, dateTime, category)
        }
    }

    fun toggleJoinDate(proposalId: Int) {
        viewModelScope.launch {
            repository.toggleJoinProposal(proposalId)
        }
    }

    // Chat Actions
    fun selectChatPartner(partner: UserProfile?) {
        _activeChatPartner.value = partner
        clearIcebreaker()
        if (partner != null) {
            _currentTab.value = DatingTab.CHATS
        }
    }

    // Icebreaker States
    private val _icebreakerState = MutableStateFlow<String?>(null)
    val icebreakerState = _icebreakerState.asStateFlow()

    private val _isGeneratingIcebreaker = MutableStateFlow(false)
    val isGeneratingIcebreaker = _isGeneratingIcebreaker.asStateFlow()

    fun generateIcebreakerForActivePartner() {
        val partner = _activeChatPartner.value ?: return
        viewModelScope.launch {
            _isGeneratingIcebreaker.value = true
            _icebreakerState.value = null
            _icebreakerState.value = repository.generateIcebreaker(partner)
            _isGeneratingIcebreaker.value = false
        }
    }

    fun clearIcebreaker() {
        _icebreakerState.value = null
    }

    fun sendChatMessage(text: String) {
        val partner = _activeChatPartner.value ?: return
        if (text.isBlank()) return
        clearIcebreaker()
        viewModelScope.launch {
            repository.sendChatMessage(partner.id, text)
        }
    }

    // My Profile update
    fun updateProfile(name: String, age: Int, bio: String, interests: String, occupation: String, zodiacSign: String) {
        val current = myProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = name,
                age = age,
                bio = bio,
                interests = interests,
                occupation = occupation,
                zodiacSign = zodiacSign
            )
            repository.updateMyProfile(updated)
        }
    }
}

class DatingViewModelFactory(
    private val application: Application,
    private val repository: DatingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DatingViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
