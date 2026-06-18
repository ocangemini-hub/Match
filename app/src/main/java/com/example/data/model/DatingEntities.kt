package com.example.data.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int,
    val name: String,
    val age: Int,
    val gender: String, // "Kadın", "Erkek", "Diğer"
    val bio: String,
    val interests: String, // Comma separated, e.g. "Kahve, Dans, Doğa, Kitaplar"
    val avatarUrl: String, // coil URL or placeholder icon identifier
    val location: String, // e.g. "Beşiktaş, İstanbul"
    val isMe: Boolean = false,
    val isLiked: Boolean = false,
    val isRejected: Boolean = false,
    val isMatched: Boolean = false,
    val occupation: String = "Serbest",
    val zodiacSign: String = "Terazi"
)

@Entity(tableName = "date_proposals")
data class DateProposal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizerId: Int,
    val organizerName: String,
    val organizerAvatar: String,
    val title: String, // e.g. "Kahve & Kitap Buluşması"
    val description: String, // e.g. "Kadıköy'de sessiz bir kafede buluşup en sevdiğimiz kitaplardan bahsedelim."
    val location: String, // "Kadıköy, İstanbul"
    val dateTime: String, // "Cumartesi, 14:00"
    val category: String, // "Yemek/İçecek", "Kültür/Sanat", "Açık Hava", "Spor", "Eğlence"
    val isCreatedByMe: Boolean = false,
    val isJoinedByMe: Boolean = false,
    val applicantCount: Int = 0,
    val status: String = "ACTIVE" // "ACTIVE", "ACCEPTED", "COMPLETED"
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatPartnerId: Int, // UserProfile.id we are chatting with
    val senderName: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean
)
