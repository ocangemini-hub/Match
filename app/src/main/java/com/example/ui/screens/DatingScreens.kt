package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.data.model.DateProposal
import com.example.data.model.UserProfile
import com.example.ui.theme.MatchGold
import com.example.ui.theme.PeachSoft
import com.example.ui.viewmodel.DatingTab
import com.example.ui.viewmodel.DatingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DateVibeAppContent(viewModel: DatingViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val matchedProfile by viewModel.matchedProfile.collectAsStateWithLifecycle()

    // Base Scaffold containing top bar, bottom bar, and inner view routing
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                DatingBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width else -width } + fadeIn() with
                                slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width else width } + fadeOut()
                    },
                    label = "TabContentTransition"
                ) { targetTab ->
                    when (targetTab) {
                        DatingTab.DISCOVER -> DiscoverScreen(viewModel)
                        DatingTab.DATES -> DatesScreen(viewModel)
                        DatingTab.CHATS -> ChatsScreen(viewModel)
                        DatingTab.PROFILE -> ProfileScreen(viewModel)
                    }
                }
            }
        }

        // Mutual Match Screen Overlay
        AnimatedVisibility(
            visible = matchedProfile != null,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(animationSpec = spring(dampingRatio = 0.6f)),
            exit = fadeOut(animationSpec = tween(400)),
            modifier = Modifier.fillMaxSize()
        ) {
            matchedProfile?.let { partner ->
                MatchOverlayScreen(
                    partner = partner,
                    onClose = { viewModel.clearMatchOverlay() },
                    onStartChat = {
                        viewModel.clearMatchOverlay()
                        viewModel.selectChatPartner(partner)
                    }
                )
            }
        }
    }
}

// Custom Bottom Navigation Bar
@Composable
fun DatingBottomNavigation(
    currentTab: DatingTab,
    onTabSelected: (DatingTab) -> Unit
) {
    NavigationBar(
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = currentTab == DatingTab.DISCOVER,
            onClick = { onTabSelected(DatingTab.DISCOVER) },
            label = { Text("Eşleş") },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Swipe") },
            modifier = Modifier.testTag("tab_discover")
        )
        NavigationBarItem(
            selected = currentTab == DatingTab.DATES,
            onClick = { onTabSelected(DatingTab.DATES) },
            label = { Text("Date Planları") },
            icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Dates") },
            modifier = Modifier.testTag("tab_dates")
        )
        NavigationBarItem(
            selected = currentTab == DatingTab.CHATS,
            onClick = { onTabSelected(DatingTab.CHATS) },
            label = { Text("Mesajlar") },
            icon = { Icon(Icons.Filled.Chat, contentDescription = "Chats") },
            modifier = Modifier.testTag("tab_chats")
        )
        NavigationBarItem(
            selected = currentTab == DatingTab.PROFILE,
            onClick = { onTabSelected(DatingTab.PROFILE) },
            label = { Text("Profilim") },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            modifier = Modifier.testTag("tab_profile")
        )
    }
}

// ==================== SCREEN 1: DISCOVER/SWIPE ====================
@Composable
fun DiscoverScreen(viewModel: DatingViewModel) {
    val feed by viewModel.discoverFeed.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showFilterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Identity Header (Mockup-inspired Top Navigation)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.selectTab(com.example.ui.viewmodel.DatingTab.PROFILE) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profilim",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DateVibe",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "KEŞFET",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showFilterDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Filtreler",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Filter Dialog
        if (showFilterDialog) {
            Dialog(onDismissRequest = { showFilterDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Arama Kriterleri",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        var ageMin by remember { mutableStateOf(18f) }
                        var ageMax by remember { mutableStateOf(35f) }
                        Column {
                            Text(
                                text = "Yaş Aralığı: ${ageMin.toInt()} - ${ageMax.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = ageMax,
                                onValueChange = { ageMax = it.coerceAtLeast(ageMin) },
                                valueRange = 18f..60f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        var maxDistance by remember { mutableStateOf(25f) }
                        Column {
                            Text(
                                text = "Maksimum Mesafe: ${maxDistance.toInt()} km",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = maxDistance,
                                onValueChange = { maxDistance = it },
                                valueRange = 5f..100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        var matchZodiacByMe by remember { mutableStateOf(true) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Burç Uyumu Eşleşmesinde Öncelik",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = matchZodiacByMe,
                                onCheckedChange = { matchZodiacByMe = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showFilterDialog = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Filtreleri Uygula")
                        }
                    }
                }
            }
        }

        if (feed.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Yakınında Yeni Kimse Kalmadı!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Biraz bekleyebilir, ya da Date Planları sekmesine giderek kendi buluşma fikrini ilan edebilirsin! ✨",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.selectTab(DatingTab.DATES) },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buluşma Planı Oluştur")
                    }
                }
            }
        } else {
            // Display top card of the deck
            val activeUser = feed.first()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(4.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // User Profile Picture loaded with Coil
                        AsyncImage(
                            model = activeUser.avatarUrl,
                            contentDescription = activeUser.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Bottom visual shadow overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.2f),
                                            Color.Black.copy(alpha = 0.85f)
                                        ),
                                        startY = 200f
                                    )
                                )
                        )

                        // Top Badges
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = activeUser.location,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Bottom Details Card
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${activeUser.name}, ${activeUser.age}",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = activeUser.zodiacSign,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = activeUser.occupation,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Text(
                                text = activeUser.bio,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tag list
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                mainAxisSpacing = 6.dp,
                                crossAxisSpacing = 6.dp
                            ) {
                                activeUser.interests.split(", ").forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                             text = "#$tag",
                                             color = Color.White,
                                             fontSize = 11.sp,
                                             fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Swipe Controller Buttons (Match-Dislike)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reject Button
                val rejectInteract = remember { MutableInteractionSource() }
                val rejectPressed by rejectInteract.collectIsPressedAsState()
                val rejectScale by animateFloatAsState(if (rejectPressed) 0.92f else 1f, label = "RejectScale")

                IconButton(
                    onClick = { viewModel.rejectCurrentProfile(activeUser) },
                    interactionSource = rejectInteract,
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer(scaleX = rejectScale, scaleY = rejectScale)
                        .shadow(4.dp, CircleShape)
                        .border(1.dp, Color(0xFFCAC4D0), CircleShape)
                        .background(Color.White, CircleShape)
                        .testTag("reject_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Reject",
                        tint = Color(0xFFB3261E),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Favorite Button
                val likeInteract = remember { MutableInteractionSource() }
                val likePressed by likeInteract.collectIsPressedAsState()
                val likeScale by animateFloatAsState(if (likePressed) 0.92f else 1f, label = "LikeScale")

                IconButton(
                    onClick = { viewModel.likeCurrentProfile(activeUser) },
                    interactionSource = likeInteract,
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer(scaleX = likeScale, scaleY = likeScale)
                        .shadow(6.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("like_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Like",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Star/Super Like Button
                val starInteract = remember { MutableInteractionSource() }
                val starPressed by starInteract.collectIsPressedAsState()
                val starScale by animateFloatAsState(if (starPressed) 0.92f else 1f, label = "StarScale")

                IconButton(
                    onClick = { viewModel.likeCurrentProfile(activeUser) },
                    interactionSource = starInteract,
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer(scaleX = starScale, scaleY = starScale)
                        .shadow(4.dp, CircleShape)
                        .border(1.dp, Color(0xFFCAC4D0), CircleShape)
                        .background(Color.White, CircleShape)
                        .testTag("star_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Super Like",
                        tint = Color(0xFF0061A4),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// FlowRow utility for wrapping tag components
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        var rowWidth = 0
        var rowHeight = 0
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()

        placeables.forEach { placeable ->
            val spacing = mainAxisSpacing.roundToPx()
            if (rowWidth + placeable.width + spacing > layoutWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                rowWidth = 0
            }
            currentRow.add(placeable)
            rowWidth += placeable.width + spacing
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        var totalHeight = 0
        rows.forEachIndexed { index, row ->
            val maxRowHeight = row.maxOf { it.height }
            totalHeight += maxRowHeight + if (index < rows.size - 1) crossAxisSpacing.roundToPx() else 0
        }

        layout(layoutWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val maxRowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacing.roundToPx()
                }
                y += maxRowHeight + crossAxisSpacing.roundToPx()
            }
        }
    }
}

// ==================== SCREEN 2: DATES / BULUŞMALAR ====================
@Composable
fun DatesScreen(viewModel: DatingViewModel) {
    val datePlans by viewModel.dateProposals.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Dialog state holders
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locName by remember { mutableStateOf("") }
    var scheduleTime by remember { mutableStateOf("") }
    var categorySelection by remember { mutableStateOf("Yemek/İçecek") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("create_date_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Yeni Plan")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "Yakındaki Date Planları",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Beğendiğin aktivitelere katılım isteği gönder, sohbeti başlat!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (datePlans.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz buralarda plan yok. İlk planı sen yapmaya ne dersin?")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(datePlans) { plan ->
                        DateProposalItem(
                            plan = plan,
                            onJoinToggle = { viewModel.toggleJoinDate(plan.id) }
                        )
                    }
                }
            }
        }

        // Add Date Plan Dialog modal
        if (showCreateDialog) {
            Dialog(onDismissRequest = { showCreateDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Yeni Date İlanı Ver",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Plan Başlığı (örn. Moda Sahilde Kahve)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Detaylar & Ne Yapacaksınız?") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = locName,
                            onValueChange = { locName = it },
                            label = { Text("Konum / Mekan") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = scheduleTime,
                            onValueChange = { scheduleTime = it },
                            label = { Text("Zaman (örn: Cumartesi, 18:00)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category Dropdown simulated via Select Buttons
                        Text("Kategori Seç", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        val catList = listOf("Yemek/İçecek", "Kültür/Sanat", "Açık Hava", "Spor", "Eğlence")
                        FlowRow(
                            mainAxisSpacing = 6.dp,
                            crossAxisSpacing = 6.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            catList.forEach { cat ->
                                val isChosen = categorySelection == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { categorySelection = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showCreateDialog = false }) {
                                Text("Vazgeç")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (title.isNotBlank() && locName.isNotBlank() && scheduleTime.isNotBlank()) {
                                        viewModel.createDateProposal(
                                            title = title,
                                            description = description,
                                            location = locName,
                                            dateTime = scheduleTime,
                                            category = categorySelection
                                        )
                                        // Reset fields
                                        title = ""
                                        description = ""
                                        locName = ""
                                        scheduleTime = ""
                                        showCreateDialog = false
                                    }
                                },
                                modifier = Modifier.testTag("submit_date_button")
                            ) {
                                Text("Yayınla")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Buluşma İlanı Kartı
@Composable
fun DateProposalItem(
    plan: DateProposal,
    onJoinToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Organizer Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = plan.organizerAvatar,
                    contentDescription = plan.organizerName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = plan.organizerName + if (plan.isCreatedByMe) " (Sen)" else "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Plan Sahibi",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = plan.category,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body
            Text(
                text = plan.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = plan.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer (Time, Location and Join Action)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(plan.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(plan.dateTime, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (!plan.isCreatedByMe) {
                    Button(
                        onClick = onJoinToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (plan.isJoinedByMe) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            contentColor = if (plan.isJoinedByMe) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = if (plan.isJoinedByMe) Icons.Filled.CheckCircle else Icons.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (plan.isJoinedByMe) "Katıldın!" else "İstek At",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${plan.applicantCount} İstek Var",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 3: CHATS / MESAJLAR ====================
@Composable
fun ChatsScreen(viewModel: DatingViewModel) {
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val activePartner by viewModel.activeChatPartner.collectAsStateWithLifecycle()

    if (activePartner == null) {
        // Inbox Match Selection List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "Mesajkutum",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Eşleştiğin kişilerle dilediğin gibi sohbet et.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (matches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Henüz Eşleşme Yok",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Yeni insanlarla eşleşmek için swiping kartlarını beğenebilirsin! ❤️",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Horizontal Match Row
                Text(
                    "Yeni Eşleşmelerin",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    items(matches) { match ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.selectChatPartner(match) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .border(
                                        2.dp,
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        ),
                                        CircleShape
                                    )
                                    .padding(3.dp)
                            ) {
                                AsyncImage(
                                    model = match.avatarUrl,
                                    contentDescription = match.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Sohbetlerin",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Conversation List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(matches) { match ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectChatPartner(match) }
                                .padding(paddingValues = PaddingValues(vertical = 8.dp, horizontal = 4.dp))
                        ) {
                            AsyncImage(
                                model = match.avatarUrl,
                                contentDescription = match.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = match.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Sohbeti sürdürün...",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Individual Chat Thread Screen
        ChatThreadScreen(
            partner = activePartner!!,
            viewModel = viewModel,
            onBack = { viewModel.selectChatPartner(null) }
        )
    }
}

// Thread Conversation view
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatThreadScreen(
    partner: UserProfile,
    viewModel: DatingViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val icebreakerText by viewModel.icebreakerState.collectAsStateWithLifecycle()
    val isGeneratingIcebreaker by viewModel.isGeneratingIcebreaker.collectAsStateWithLifecycle()

    var typedText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // Scroll to bottom when conversation receives messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Chat Partner Header
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Geri Git")
                }
                Spacer(modifier = Modifier.width(4.dp))
                AsyncImage(
                    model = partner.avatarUrl,
                    contentDescription = partner.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = partner.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Aktif Sohbet",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                // AI Icebreaker Trigger
                IconButton(
                    onClick = { viewModel.generateIcebreakerForActivePartner() },
                    modifier = Modifier.testTag("ai_icebreaker_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "AI Icebreaker",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Active Gemini Icebreaker container
        AnimatedVisibility(
            visible = icebreakerText != null || isGeneratingIcebreaker,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Buz Kırıcı Önerisi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { viewModel.clearIcebreaker() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isGeneratingIcebreaker) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini buzları kıracak en çekici cümleyi hazırlıyor...", fontSize = 12.sp)
                        }
                    } else {
                        Text(
                            text = icebreakerText ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                typedText = icebreakerText ?: ""
                                viewModel.clearIcebreaker()
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Cümleyi Kullan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Messages Box List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg = msg)
                }
            }
        }

        // Typing Bar Input
        Surface(
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    placeholder = { Text("Bir mesaj yazın...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (typedText.isNotBlank()) {
                                viewModel.sendChatMessage(typedText)
                                typedText = ""
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (typedText.isNotBlank()) {
                            viewModel.sendChatMessage(typedText)
                            typedText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("send_message_button"),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Gönder")
                }
            }
        }
    }
}

// Standard chat bubble representation
@Composable
fun ChatBubble(msg: ChatMessage) {
    val bubbleColor = if (msg.isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (msg.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (msg.isFromMe) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .align(alignment)
                .widthIn(max = 280.dp)
                .shadow(1.dp, shape),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = msg.messageText,
                    color = textColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ==================== SCREEN 4: PROFILE / PROFILEM ====================
@Composable
fun ProfileScreen(viewModel: DatingViewModel) {
    val currentProfile by viewModel.myProfile.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }

    // Forms local states
    var nameEdit by remember { mutableStateOf("") }
    var ageEdit by remember { mutableStateOf("") }
    var bioEdit by remember { mutableStateOf("") }
    var interpEdit by remember { mutableStateOf("") }
    var jobEdit by remember { mutableStateOf("") }
    var zodiacEdit by remember { mutableStateOf("") }

    // Synchronize editing variables once profile loads
    LaunchedEffect(currentProfile) {
        currentProfile?.let {
            nameEdit = it.name
            ageEdit = it.age.toString()
            bioEdit = it.bio
            interpEdit = it.interests
            jobEdit = it.occupation
            zodiacEdit = it.zodiacSign
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Kişisel Profilim",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "Profilindeki bilgileri güncel tutarak eşleşme oranını artır.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )

        currentProfile?.let { profile ->
            // Avatar with Glowing circular border
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .border(
                        3.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        CircleShape
                    )
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = "My Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${profile.name}, ${profile.age}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${profile.occupation} • ${profile.zodiacSign} Burcu",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Karizma Skoru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("%98", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Buluşmalar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("6 İlan", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditing) {
                // Read Only View fields
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Hakkımda", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Text(profile.bio, fontSize = 14.sp)

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                        Text("İlgi Alanlarım", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        FlowRow(
                            mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp, modifier = Modifier.fillMaxWidth()
                        ) {
                            profile.interests.split(", ").forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { isEditing = true },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("edit_profile_button")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Profili Düzenle")
                }
            } else {
                // Editing Form Fields Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Profil Bilgilerini Düzenle", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        OutlinedTextField(
                            value = nameEdit,
                            onValueChange = { nameEdit = it },
                            label = { Text("Adın") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = ageEdit,
                            onValueChange = { ageEdit = it },
                            label = { Text("Yaşın") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = jobEdit,
                            onValueChange = { jobEdit = it },
                            label = { Text("Mesleğin") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = zodiacEdit,
                            onValueChange = { zodiacEdit = it },
                            label = { Text("Burcun") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = bioEdit,
                            onValueChange = { bioEdit = it },
                            label = { Text("Biyografi") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                        OutlinedTextField(
                            value = interpEdit,
                            onValueChange = { interpEdit = it },
                            label = { Text("İlgi Alanları (Virgülle Ayırın)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { isEditing = false }) {
                                Text("Vazgeç")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val parsedAge = ageEdit.toIntOrNull() ?: profile.age
                                    if (nameEdit.isNotBlank()) {
                                        viewModel.updateProfile(
                                            name = nameEdit,
                                            age = parsedAge,
                                            bio = bioEdit,
                                            interests = interpEdit,
                                            occupation = jobEdit,
                                            zodiacSign = zodiacEdit
                                        )
                                        isEditing = false
                                    }
                                },
                                modifier = Modifier.testTag("save_profile_button")
                            ) {
                                Text("Kaydet")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== SCREEN OVERLAY: MATCH ANIMATED BANNER ====================
@Composable
fun MatchOverlayScreen(
    partner: UserProfile,
    onClose: () -> Unit,
    onStartChat: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(enabled = true, onClick = {}), // Absorb taps click blocker
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Glowing Sparkles Header
            Icon(
                imageVector = Icons.Filled.BlurOn,
                contentDescription = null,
                tint = MatchGold,
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        // Soft scale pulse
                        scaleX = 1.1f
                        scaleY = 1.1f
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Eşleştiniz! 🎉",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MatchGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sen ve ${partner.name} birbirinizi beğendiniz. Harika bir sohbete başlamaya ne dersin?",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Overlap circular pictures
            Box(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Partner image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer { translationX = -45f }
                        .border(3.dp, MatchGold, CircleShape)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = partner.avatarUrl,
                        contentDescription = partner.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Current representative user image (Can)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer { translationX = 45f }
                        .border(3.dp, Color.White, CircleShape)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&auto=format&fit=crop&q=80",
                        contentDescription = "Me",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStartChat,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Chat, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hemen Mesaj At", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Kaydırmaya Devam Et",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
