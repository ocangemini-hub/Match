package com.example.data.local

import androidx.room.*
import com.example.data.model.ChatMessage
import com.example.data.model.DateProposal
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface DatingDao {

    // --- User Profile Operations ---
    @Query("SELECT * FROM user_profiles WHERE isMe = 0 ORDER BY id ASC")
    fun getAllPotentialMatches(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE isMe = 0 AND isLiked = 0 AND isRejected = 0 ORDER BY id ASC")
    fun getDiscoverFeed(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE isMatched = 1")
    fun getMatches(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE isMe = 1 LIMIT 1")
    fun getMyProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE isMe = 1 LIMIT 1")
    suspend fun getMyProfileSync(): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getUserProfileById(id: Int): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<UserProfile>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("UPDATE user_profiles SET isLiked = 1, isMatched = :isMatched WHERE id = :id")
    suspend fun likeProfile(id: Int, isMatched: Boolean)

    @Query("UPDATE user_profiles SET isRejected = 1 WHERE id = :id")
    suspend fun rejectProfile(id: Int)

    // --- Date Proposal Operations ---
    @Query("SELECT * FROM date_proposals ORDER BY id DESC")
    fun getAllDateProposals(): Flow<List<DateProposal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDateProposal(proposal: DateProposal)

    @Update
    suspend fun updateDateProposal(proposal: DateProposal)

    @Query("UPDATE date_proposals SET isJoinedByMe = :isJoinedByMe, applicantCount = applicantCount + :countChange WHERE id = :id")
    suspend fun toggleJoinProposal(id: Int, isJoinedByMe: Boolean, countChange: Int)

    // --- Chat Message Operations ---
    @Query("SELECT * FROM chat_messages WHERE chatPartnerId = :partnerId ORDER BY timestamp ASC")
    fun getChatMessages(partnerId: Int): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}
