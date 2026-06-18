package com.example.data.repository

import android.util.Log
import com.example.data.local.DatingDao
import com.example.data.model.ChatMessage
import com.example.data.model.DateProposal
import com.example.data.model.UserProfile
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DatingRepository(private val datingDao: DatingDao) {

    val allPotentialMatches: Flow<List<UserProfile>> = datingDao.getAllPotentialMatches()
    val discoverFeed: Flow<List<UserProfile>> = datingDao.getDiscoverFeed()
    val matches: Flow<List<UserProfile>> = datingDao.getMatches()
    val myProfile: Flow<UserProfile?> = datingDao.getMyProfile()
    val allDateProposals: Flow<List<DateProposal>> = datingDao.getAllDateProposals()

    fun getChatMessages(partnerId: Int): Flow<List<ChatMessage>> = datingDao.getChatMessages(partnerId)

    suspend fun getMyProfileSync(): UserProfile? = datingDao.getMyProfileSync()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun seedInitialData() = withContext(Dispatchers.IO) {
        val currentMyProfile = datingDao.getMyProfileSync()
        if (currentMyProfile == null) {
            // Seed current user proflie
            val myProfile = UserProfile(
                id = 999,
                name = "Can",
                age = 26,
                gender = "Erkek",
                bio = "Müzik, teknoloji ve üçüncü nesil kahve severim. Şehirde yeni keşifler yapacak ve samimi sohbetler edecek birilerini arıyorum. ☕️🎸",
                interests = "Kahve, Konser, Doğa, Akustik, Sinema",
                avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&auto=format&fit=crop&q=80",
                location = "Kadıköy, İstanbul",
                isMe = true,
                occupation = "Yazılım Geliştirici",
                zodiacSign = "Terazi"
            )
            datingDao.insertProfile(myProfile)

            // Seed other user profiles
            val mockProfiles = listOf(
                UserProfile(
                    id = 1,
                    name = "Selin",
                    age = 24,
                    gender = "Kadın",
                    bio = "Film eleştirmeni ve kahve bağımlısı. Pazar sabahları yürüyüş yapmayı ve plak dinlemeyi çok severim. ☕️🎨🎥",
                    interests = "Sinema, Plaklar, Kahve, Yürüyüş",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                    location = "Moda, İstanbul",
                    isMe = false,
                    occupation = "Sanat Yönetmeni",
                    zodiacSign = "İkizler"
                ),
                UserProfile(
                    id = 2,
                    name = "Derin",
                    age = 25,
                    gender = "Kadın",
                    bio = "Fotoğrafçılıkla ilgileniyorum. Yeni nesil kafeler keşfetmek ve günbatımı fotoğrafları çekmek benim için bir tutku. 🌅📸",
                    interests = "Fotoğraf, Tasarım, Doğa, Seyahat",
                    avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&auto=format&fit=crop&q=80",
                    location = "Beşiktaş, İstanbul",
                    isMe = false,
                    occupation = "Mimar",
                    zodiacSign = "Yay"
                ),
                UserProfile(
                    id = 3,
                    name = "Melis",
                    age = 23,
                    gender = "Kadın",
                    bio = "Konser aşığı, festival insanı. Yeni yerler ve dünya mutfakları keşfetmek en sevdiğim aktivite. Beraber rock konserine gidelim mi? 🎸⚡️",
                    interests = "Konser, Seyahat, Gurme, Dans",
                    avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80",
                    location = "Kadıköy, İstanbul",
                    isMe = false,
                    occupation = "Pazarlama Uzmanı",
                    zodiacSign = "Koç"
                ),
                UserProfile(
                    id = 4,
                    name = "Mert",
                    age = 27,
                    gender = "Erkek",
                    bio = "Bisiklet sürmeyi, kamp yapmayı ve doğada kaybolmayı severim. Şehir hayatının gürültüsünden uzaklaşacak kafa dengi arkadaşlar arıyorum. ⛺️🚲🌲",
                    interests = "Kamp, Bisiklet, Doğa, Gurme",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                    location = "Sarıyer, İstanbul",
                    isMe = false,
                    occupation = "Yazılım Mühendisi",
                    zodiacSign = "Aslan"
                ),
                UserProfile(
                    id = 5,
                    name = "Ceren",
                    age = 26,
                    gender = "Kadın",
                    bio = "Yoga eğitmeniyim 🧘‍♀️. Minimalist yaşam tarzı, meditasyon ve sağlıklı beslenme hayatımın merkezi. Kitap okumak ve doğada sessizlik bana huzur veriyor.",
                    interests = "Yoga, Meditasyon, Kitaplar, Vegan",
                    avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&auto=format&fit=crop&q=80",
                    location = "Üsküdar, İstanbul",
                    isMe = false,
                    occupation = "Yoga Eğitmeni",
                    zodiacSign = "Başak"
                ),
                UserProfile(
                    id = 6,
                    name = "Esra",
                    age = 26,
                    gender = "Kadın",
                    bio = "Tiyatro, modern dans ve müze gezileri vazgeçilmezim. Kültürel aktiviteleri birlikte paylaşabileceğimiz, derin sohbetler edecek bir yol arkadaşı. 🎭🏛️🍷",
                    interests = "Tiyatro, Sanat, Müze, Şarap",
                    avatarUrl = "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?w=400&auto=format&fit=crop&q=80",
                    location = "Beyoğlu, İstanbul",
                    isMe = false,
                    occupation = "Küratör",
                    zodiacSign = "Kova"
                )
            )
            datingDao.insertProfiles(mockProfiles)

            // Seed default date proposals
            val mockProposals = listOf(
                DateProposal(
                    id = 1,
                    organizerId = 1,
                    organizerName = "Selin",
                    organizerAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                    title = "Moda Sahili Plak & Kahve",
                    description = "Sakin bir kafede buluşup en sevdiğimiz caz plaklarını inceleyelim, sonrasında Moda sahilinde kahve eşliğinde yürüyüp günbatımını izleyelim! 🎉",
                    location = "Kadıköy, İstanbul",
                    dateTime = "Cumartesi, 16:00",
                    category = "Yemek/İçecek",
                    applicantCount = 2
                ),
                DateProposal(
                    id = 2,
                    organizerId = 6,
                    organizerName = "Esra",
                    organizerAvatar = "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?w=400&auto=format&fit=crop&q=80",
                    title = "İKSV Modern Sanat Sergisi",
                    description = "Arter veya Salt Beyoğlu'ndaki güncel sergiyi beraber gezdikten sonra bir şeyler içip çağdaş sanat üzerine keyifli bir sohbet yapalım. Kültür dolu bir gün olsun!",
                    location = "Beyoğlu, İstanbul",
                    dateTime = "Pazar, 14:00",
                    category = "Kültür/Sanat",
                    applicantCount = 1
                ),
                DateProposal(
                    id = 3,
                    organizerId = 4,
                    organizerName = "Mert",
                    organizerAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                    title = "Belgrad Ormanı Doğa Koşusu",
                    description = "Sabah erken saatte Belgrad Ormanı'nda 6k hafif tempo koşu/yürüyüş yapalım. Ardından termosumdaki taze demlenmiş filtre kahveyi paylaşırız!",
                    location = "Sarıyer, İstanbul",
                    dateTime = "Pazar, 09:30",
                    category = "Açık Hava",
                    applicantCount = 0
                )
            )
            for (p in mockProposals) {
                datingDao.insertDateProposal(p)
            }
        }
    }

    suspend fun updateMyProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        datingDao.insertProfile(profile)
    }

    suspend fun likeProfile(id: Int): Boolean = withContext(Dispatchers.IO) {
        // 70% chance of a mutual match in app demo
        val isMatched = (1..100).random() <= 70
        datingDao.likeProfile(id, isMatched)
        if (isMatched) {
            // Send matching welcoming message
            val partner = datingDao.getUserProfileById(id)
            if (partner != null) {
                datingDao.insertMessage(
                    ChatMessage(
                        chatPartnerId = id,
                        senderName = partner.name,
                        messageText = "Selam Can! Seni görmek harika 😊 Profilin çok ilgimi çekti, ne zaman kahve içiyoruz?",
                        isFromMe = false
                    )
                )
            }
        }
        isMatched
    }

    suspend fun rejectProfile(id: Int) = withContext(Dispatchers.IO) {
        datingDao.rejectProfile(id)
    }

    suspend fun toggleJoinProposal(proposalId: Int): Boolean = withContext(Dispatchers.IO) {
        // Fetch proposal to check state
        val list = allDateProposals.first()
        val p = list.firstOrNull { it.id == proposalId }
        if (p != null) {
            val nextState = !p.isJoinedByMe
            val countDiff = if (nextState) 1 else -1
            datingDao.toggleJoinProposal(proposalId, nextState, countDiff)
            return@withContext nextState
        }
        false
    }

    suspend fun createDateProposal(title: String, description: String, location: String, dateTime: String, category: String) = withContext(Dispatchers.IO) {
        val myData = getMyProfileSync() ?: return@withContext
        val proposal = DateProposal(
            organizerId = myData.id,
            organizerName = myData.name,
            organizerAvatar = myData.avatarUrl,
            title = title,
            description = description,
            location = location,
            dateTime = dateTime,
            category = category,
            isCreatedByMe = true,
            applicantCount = 0
        )
        datingDao.insertDateProposal(proposal)
    }

    suspend fun sendChatMessage(partnerId: Int, text: String): ChatMessage = withContext(Dispatchers.IO) {
        val myData = getMyProfileSync()
        val partner = datingDao.getUserProfileById(partnerId)
        val myName = myData?.name ?: "Can"

        // Save our message
        val myMsg = ChatMessage(
            chatPartnerId = partnerId,
            senderName = myName,
            messageText = text,
            isFromMe = true
        )
        datingDao.insertMessage(myMsg)

        // Trigger AI or Mock Response
        if (partner != null) {
            triggerSimulatedReply(partner, text)
        }
        myMsg
    }

    private suspend fun triggerSimulatedReply(partner: UserProfile, lastUserMessage: String) {
        // Run asynchronously in background after 1.5s delay
        delay(1500)
        val replyText = generateReplyText(partner, lastUserMessage)
        datingDao.insertMessage(
            ChatMessage(
                chatPartnerId = partner.id,
                senderName = partner.name,
                messageText = replyText,
                isFromMe = false
            )
        )
    }

    private suspend fun generateReplyText(partner: UserProfile, lastUserMessage: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        // Return simulated reply if Gemini API key is missing or is the default placeholder
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackReply(partner, lastUserMessage)
        }

        try {
            val systemPrompt = """
                Sen '${partner.name}' adında, ${partner.age} yaşında bir kullanıcısın. 
                Mesleğin: ${partner.occupation}. 
                Burcun: ${partner.zodiacSign}.
                Kişilik ve İlgi Alanların: ${partner.bio}. 
                Bu bir sosyal eşleşme ve dating uygulamasıdır. Karşındaki kullanıcı adı 'Can'. Can sana bir mesaj attı. 
                Ona sıcak, samimi, cana yakın, Türkçe bir cevap yaz. Cevabın tamamen senin canlandırdığın karakterin kişiliği, mesleği ve tarzına uygun olsun.
                Lütfen cevabı 2 cümleyi geçmeyecek şekilde yaz. Sohbeti devam ettirecek sıcak bir soru da sorabilirsin.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", lastUserMessage)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val resBody = response.body?.string()
                if (!resBody.isNullOrEmpty()) {
                    val candidates = JSONObject(resBody).getJSONArray("candidates")
                    val text = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    return@withContext text.trim()
                }
            }
            Log.e("DatingRepository", "Gemini call failed: ${response.code} ${response.message}")
        } catch (e: Exception) {
            Log.e("DatingRepository", "Error in Gemini call", e)
        }

        return@withContext getLocalFallbackReply(partner, lastUserMessage)
    }

    private fun getLocalFallbackReply(partner: UserProfile, userMessage: String): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("selam") || lower.contains("merhaba") -> {
                listOf(
                    "Sana da merhaba Can! Harika bir gün geçiriyor musun?",
                    "Selam canım! Sohbetine çok sevindim, günün nasıl gidiyor?",
                    "Selam Can! Profilini gördüğümden beri yazmanı bekliyordum 😊"
                ).random()
            }
            lower.contains("buluşalım") || lower.contains("date") || lower.contains("kahve") || lower.contains("içelim") -> {
                listOf(
                    "Harika fikir! Kadıköy Moda sahilinde çok tatlı bir yer biliyorum, ne zaman müsaitsin?",
                    "Hafta sonu çok güzel bir plan olabilir! Cumartesi günü kahve içelim mi?",
                    "Kesinlikle evet! Bu ara yeni kafeler denemek istiyordum, eşlik etmene sevinirim 😊"
                ).random()
            }
            lower.contains("nasılsın") || lower.contains("nasıl gidiyor") -> {
                listOf(
                    "Ben gayet iyiyim, teşekkür ederim! Sen nasılsın, neler yapıyorsun?",
                    "Harikayım! Yeni müzikler keşfediyordum tam, sen de müzik seviyorsun değil mi? 🎶",
                    "İyiyim Can, her şey yolunda! Senin günün nasıl geçti?"
                ).random()
            }
            else -> {
                listOf(
                    "Çok samimi geliyorsun Can! Burcumun özelliklerini yansıtıyorum sanırım, sohbet etmeyi severim 😊",
                    "İlgi alanların çok dikkatimi çekti, ortak noktalarımız olması harika!",
                    "Bunu duyduğuma sevindim! Seninle tanışmak gerçekten heyecan verici.",
                    "Hafta sonu için planın nedir? Beraber bir şeyler yapsak süper olurdu."
                ).random()
            }
        }
    }

    suspend fun generateIcebreaker(partner: UserProfile): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            delay(1000)
            return@withContext listOf(
                "Hey ${partner.name}! İlgi alanlarında ${partner.interests.split(", ").randomOrNull() ?: "kahve"} yazıyor. En sevdiğin yer neresi? 😊",
                "Selam ${partner.name}! ${partner.zodiacSign} burcu olduğunu gördüm. Burcunun özelliklerini yansıtır mısın? 🔮",
                "Merhaba ${partner.name}! ${partner.occupation} olmak hep istediğim bir meslekti. Mesleğin hakkında konuşmak isterim! ✨"
            ).random()
        }

        try {
            val prompt = """
                Sen bir dating ve flört asistanısın. Kullanıcı adı Can, partnerinin adı ise ${partner.name}.
                Partnerin ${partner.age} yaşında, mesleği ${partner.occupation}, burcu ise ${partner.zodiacSign}.
                İlgi alanları: ${partner.interests}. Hakkında: ${partner.bio}.
                Can'in partnerine gönderebileceği, onun ilgisini çekecek, eğlenceli, yaratıcı, samimi ve Türkçe bir sohbete başlama mesajı (icebreaker) üret. 
                Mesaj tek cümlelik, flörtöz ve tatlı olsun. Açıklama veya tırnak işaretleri eklemeden doğrudan mesajın kendisini yaz.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val resBody = response.body?.string()
                if (!resBody.isNullOrEmpty()) {
                    val candidates = JSONObject(resBody).getJSONArray("candidates")
                    val text = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    return@withContext text.trim()
                }
            }
        } catch (e: Exception) {
            Log.e("DatingRepository", "Error generating icebreaker", e)
        }

        return@withContext "Hey ${partner.name}! Neler yapıyorsun? İlgi alanlarımız benziyor gibi 😊"
    }
}
