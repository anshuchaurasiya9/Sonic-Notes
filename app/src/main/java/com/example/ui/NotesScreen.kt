package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Note
import com.example.ui.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.GeminiTranslator
import com.example.data.LanguageOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    var isAddingNote by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val categories = listOf("All", "Personal", "Work", "Ideas", "Tasks", "Finance", "Reading")
    val isDark = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header Title and Sort icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort notes"
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sort by Date") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    viewModel.selectSortOption(SortOption.LAST_UPDATED)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortBy == SortOption.LAST_UPDATED) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected"
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Title") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.SortByAlpha,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    viewModel.selectSortOption(SortOption.TITLE)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortBy == SortOption.TITLE) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected"
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Tag") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Label,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    viewModel.selectSortOption(SortOption.CATEGORY)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortBy == SortOption.CATEGORY) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Search Bar Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    placeholder = { Text("Search your notes...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable category filter chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(categories) { categoryName ->
                        val isSelected = selectedCategory == categoryName
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectCategory(categoryName)
                                focusManager.clearFocus()
                            },
                            label = { Text(categoryName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.testTag("category_chip_$categoryName")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNote = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_note_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new note",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (notes.isEmpty()) {
                // Empty state illustration
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .testTag("empty_state"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NoteAlt,
                        contentDescription = "No Notes",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No results found" else "Your idea canvas is empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "Try searching with different keywords."
                        } else {
                            "Tap the '+' button below to write your first persistent colorful note."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                // Responsive adaptive notes grid (scales column count beautifully based on width)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("notes_grid"),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { noteToEdit = note },
                            onPinToggle = { viewModel.togglePin(note) },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }

    // Modal dialog overlays for creating/editing to ensure highly accessible input flows
    if (isAddingNote) {
        NoteEditorDialog(
            note = null,
            onDismiss = { isAddingNote = false },
            onSave = { title, content, category, colorIdx, audioPath, audioDur ->
                viewModel.addNote(title, content, category, colorIdx, audioPath, audioDur)
                isAddingNote = false
            },
            onDelete = null,
            isDark = isDark
        )
    }

    noteToEdit?.let { note ->
        NoteEditorDialog(
            note = note,
            onDismiss = { noteToEdit = null },
            onSave = { title, content, category, colorIdx, audioPath, audioDur ->
                viewModel.updateNote(
                    note.copy(
                        title = title,
                        content = content,
                        category = category,
                        colorIndex = colorIdx,
                        audioFilePath = audioPath,
                        audioDurationMs = audioDur
                    )
                )
                noteToEdit = null
            },
            onDelete = {
                viewModel.deleteNote(note)
                noteToEdit = null
            },
            isDark = isDark
        )
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val noteBgColor = NoteColors.getColor(note.colorIndex, isDark)
    val cardBorderColor = if (note.colorIndex == 0) {
        MaterialTheme.colorScheme.outline
    } else {
        noteBgColor.copy(alpha = 0.5f)
    }
    
    val surfaceColor = if (note.colorIndex == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        noteBgColor
    }

    val onSurfaceColor = if (note.colorIndex == 0) {
        MaterialTheme.colorScheme.onSurface
    } else {
        if (isDark) Color.White else Color(0xFF1C1F2E)
    }

    val timestampStringByDate = remember(note.lastUpdatedTimestamp) {
        val date = Date(note.lastUpdatedTimestamp)
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(date)
    }

    val cardShape = if (note.isPinned) RoundedCornerShape(28.dp) else RoundedCornerShape(16.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}")
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = cardBorderColor,
                shape = cardShape
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and pin status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title.ifEmpty { "Untitled note" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = onSurfaceColor.copy(alpha = if (note.title.isEmpty()) 0.45f else 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onPinToggle() },
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("pin_button_${note.id}")
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (note.isPinned) "Unpin note" else "Pin note",
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else onSurfaceColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body Content Preview
            Text(
                text = note.content.ifEmpty { "No description contents" },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = onSurfaceColor.copy(alpha = if (note.content.isEmpty()) 0.45f else 0.75f),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )

            // If card has audio recording, show a gorgeous audio player widget
            if (note.audioFilePath != null) {
                Spacer(modifier = Modifier.height(10.dp))
                var isPlaying by remember { mutableStateOf(false) }
                var playProgress by remember { mutableStateOf(0f) }
                var playTimeLabel by remember { mutableStateOf("") }
                val context = LocalContext.current
                val cardPlayer = remember { AudioRecorderManager(context) }

                DisposableEffect(note.id) {
                    onDispose {
                        cardPlayer.stopPlaying()
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(onSurfaceColor.copy(alpha = 0.05f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                cardPlayer.stopPlaying()
                                isPlaying = false
                            } else {
                                val path = note.audioFilePath
                                if (path != null) {
                                    isPlaying = true
                                    cardPlayer.startPlaying(path,
                                        onPlayProgress = { cur, dur ->
                                            playProgress = if (dur > 0) cur.toFloat() / dur.toFloat() else 0f
                                            val curSecs = cur / 1000
                                            playTimeLabel = String.format("%02d:%02d", curSecs / 60, curSecs % 60)
                                        },
                                        onCompletion = {
                                            isPlaying = false
                                            playProgress = 0f
                                            playTimeLabel = ""
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (isPlaying) "Pause Recording" else "Play Recording",
                            tint = onSurfaceColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Voice Memo",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = onSurfaceColor.copy(alpha = 0.8f)
                            )
                            val durationSec = note.audioDurationMs / 1000
                            val durText = String.format("%02d:%02d", durationSec / 60, durationSec % 60)
                            Text(
                                text = if (isPlaying && playTimeLabel.isNotEmpty()) "$playTimeLabel / $durText" else durText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = onSurfaceColor.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (isPlaying) playProgress else 0.0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = onSurfaceColor.copy(alpha = 0.1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metadata footer (Tag chip + date)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timestampStringByDate,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = onSurfaceColor.copy(alpha = 0.5f)
                )

                if (note.category.isNotEmpty() && note.category != "All") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (note.colorIndex == 0) {
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                } else {
                                    onSurfaceColor.copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = note.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = if (note.colorIndex == 0) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                onSurfaceColor.copy(alpha = 0.75f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteEditorDialog(
    note: Note?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: String, colorIndex: Int, audioFilePath: String?, audioDurationMs: Long) -> Unit,
    onDelete: (() -> Unit)?,
    isDark: Boolean
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "Personal") }
    var colorIndex by remember { mutableStateOf(note?.colorIndex ?: 0) }

    // Audio / Voice states
    var audioFilePath by remember { mutableStateOf(note?.audioFilePath) }
    var audioDurationMs by remember { mutableStateOf(note?.audioDurationMs ?: 0L) }
    var isRecordingVoiceMemo by remember { mutableStateOf(false) }
    var isHearingStt by remember { mutableStateOf(false) }
    var speechError by remember { mutableStateOf<String?>(null) }
    
    var isTranslating by remember { mutableStateOf(false) }
    var showTranslateMenu by remember { mutableStateOf(false) }
    var showSpeechLangMenu by remember { mutableStateOf(false) }
    var selectedSpeechLang by remember { mutableStateOf(GeminiTranslator.LANGUAGES[0]) }
    var isPlayingEditorAudio by remember { mutableStateOf(false) }
    var editorAudioProgress by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioRecorderManager = remember { AudioRecorderManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioRecorderManager.stopPlaying()
            audioRecorderManager.stopRecording()
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechError = "Microphone granted. Tap button again to start."
        } else {
            speechError = "Microphone permission is required for voice features"
        }
    }

    val speechRecognizerHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onResult = { result ->
                if (content.isNotBlank()) {
                    content += " $result"
                } else {
                    content = result
                }
            },
            onPartialResult = { _ -> },
            onStateChange = { active ->
                isHearingStt = active
            },
            onError = { msg ->
                if (!msg.contains("timeout", ignoreCase = true)) {
                    speechError = msg
                }
            }
        )
    }

    var secondsElapsed by remember { mutableStateOf(0) }
    LaunchedEffect(isRecordingVoiceMemo) {
        if (isRecordingVoiceMemo) {
            secondsElapsed = 0
            while (isRecordingVoiceMemo) {
                delay(1000)
                secondsElapsed++
            }
        }
    }
    val recordingTimeText = remember(secondsElapsed) {
        String.format("%02d:%02d", secondsElapsed / 60, secondsElapsed % 60)
    }

    val categories = listOf("Personal", "Work", "Ideas", "Tasks", "Finance", "Reading")

    // Dynamically animated background color matching selection for interactive premium look
    val dialogBgColor = NoteColors.getColor(colorIndex, isDark)
    val animatedBgColor by animateColorAsState(
        targetValue = if (colorIndex == 0) MaterialTheme.colorScheme.surface else dialogBgColor,
        animationSpec = spring(),
        label = "dialogBgColor"
    )

    val contentColor = if (colorIndex == 0) {
        MaterialTheme.colorScheme.onSurface
    } else {
        if (isDark) Color.White else Color(0xFF1C1F2E)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp)
                .testTag("note_editor_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = animatedBgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Editor Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (note == null) "New Note" else "Edit Note",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = contentColor
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Editing",
                            tint = contentColor.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Title Input Box
                    TransparentTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Title",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        contentColor = contentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description Content Box
                    TransparentTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = "Write something inspiring...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        contentColor = contentColor,
                        singleLine = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .testTag("editor_content_input")
                    )

                    // --- Voice & Language Studio ---
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = contentColor.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = contentColor.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Voice Studio",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = contentColor
                                    )
                                }
                                
                                Box {
                                    TextButton(
                                        onClick = { showSpeechLangMenu = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = selectedSpeechLang.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    
                                    DropdownMenu(
                                        expanded = showSpeechLangMenu,
                                        onDismissRequest = { showSpeechLangMenu = false }
                                    ) {
                                        GeminiTranslator.LANGUAGES.forEach { lang ->
                                            DropdownMenuItem(
                                                text = { Text(lang.name) },
                                                onClick = {
                                                    selectedSpeechLang = lang
                                                    showSpeechLangMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val permissionCheck = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        )
                                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                            if (isHearingStt) {
                                                speechRecognizerHelper.stopListening()
                                                isHearingStt = false
                                            } else {
                                                speechError = null
                                                speechRecognizerHelper.startListening(selectedSpeechLang.bcp47)
                                                isHearingStt = true
                                            }
                                        } else {
                                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isHearingStt) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = if (isHearingStt) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isHearingStt) Icons.Default.MicNone else Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isHearingStt) "Stop" else "Live Dictate",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Button(
                                    onClick = {
                                        val permissionCheck = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        )
                                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                            if (isRecordingVoiceMemo) {
                                                val duration = audioRecorderManager.stopRecording()
                                                if (duration > 0) {
                                                    audioDurationMs = duration
                                                }
                                                isRecordingVoiceMemo = false
                                                speechRecognizerHelper.stopListening()
                                                isHearingStt = false
                                            } else {
                                                speechError = null
                                                val file = audioRecorderManager.startRecording()
                                                if (file != null) {
                                                    audioFilePath = file.absolutePath
                                                    isRecordingVoiceMemo = true
                                                    speechRecognizerHelper.startListening(selectedSpeechLang.bcp47)
                                                } else {
                                                    speechError = "Mic not available"
                                                }
                                            }
                                        } else {
                                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRecordingVoiceMemo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = if (isRecordingVoiceMemo) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isRecordingVoiceMemo) Icons.Default.FiberManualRecord else Icons.Default.KeyboardVoice,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isRecordingVoiceMemo) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isRecordingVoiceMemo) "Recording..." else "Record Memo",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            if (isRecordingVoiceMemo) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Recording audio memo... $recordingTimeText",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Red
                                    )
                                }
                            }

                            if (isHearingStt && !isRecordingVoiceMemo) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Listening... Speak or start writing",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = contentColor.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            speechError?.let { err ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            audioFilePath?.let { filePath ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(contentColor.copy(alpha = 0.05f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isPlayingEditorAudio) {
                                                audioRecorderManager.stopPlaying()
                                                isPlayingEditorAudio = false
                                            } else {
                                                isPlayingEditorAudio = true
                                                audioRecorderManager.startPlaying(filePath,
                                                    onPlayProgress = { cur, dur ->
                                                        editorAudioProgress = if (dur > 0) cur.toFloat() / dur.toFloat() else 0f
                                                    },
                                                    onCompletion = {
                                                        isPlayingEditorAudio = false
                                                        editorAudioProgress = 0f
                                                    }
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingEditorAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = contentColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Voice Memo Attachment",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = contentColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LinearProgressIndicator(
                                            progress = { if (isPlayingEditorAudio) editorAudioProgress else 0f },
                                            modifier = Modifier.fillMaxWidth().height(3.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = contentColor.copy(alpha = 0.1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            audioRecorderManager.stopPlaying()
                                            isPlayingEditorAudio = false
                                            audioFilePath = null
                                            audioDurationMs = 0L
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove recording",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = contentColor.copy(alpha = 0.1f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    Button(
                                        onClick = { showTranslateMenu = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = !isTranslating
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Translate,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Translate (Gemini)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showTranslateMenu,
                                        onDismissRequest = { showTranslateMenu = false }
                                    ) {
                                        GeminiTranslator.LANGUAGES.forEach { lang ->
                                            DropdownMenuItem(
                                                text = { Text(lang.name) },
                                                onClick = {
                                                    showTranslateMenu = false
                                                    scope.launch {
                                                        isTranslating = true
                                                        val translated = GeminiTranslator.translate(title, content, lang.name)
                                                        title = translated.first
                                                        content = translated.second
                                                        isTranslating = false
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                if (isTranslating) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Translating...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = contentColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Tag Selector Label
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Scrolling category list
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = category == cat
                            val chipBg = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                contentColor.copy(alpha = 0.08f)
                            }
                            val chipTextCol = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                contentColor.copy(alpha = 0.8f)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(chipBg)
                                    .clickable { category = cat }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("editor_category_chip_$cat")
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = chipTextCol
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic Note Card Color Selector Palette
                    Text(
                        text = "Card Background",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                    ) {
                        items(NoteColors.list) { colorPalette ->
                            val isSelected = colorIndex == colorPalette.index
                            val colorRepr = if (colorPalette.index == 0) {
                                MaterialTheme.colorScheme.outlineVariant
                            } else {
                                if (isDark) colorPalette.dark else colorPalette.light
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorRepr)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            contentColor.copy(alpha = 0.3f)
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { colorIndex = colorPalette.index }
                                    .testTag("color_choice_${colorPalette.index}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (colorPalette.index == 0) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            if (isDark) Color.White else Color(0xFF1C1F2E)
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Tools Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left action delete (only visible when editing existing)
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("editor_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Note",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    // Right action buttons (Dismiss and Saved)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = contentColor.copy(alpha = 0.8f))
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (title.isNotBlank() || content.isNotBlank()) {
                                    onSave(title, content, category, colorIndex, audioFilePath, audioDurationMs)
                                } else {
                                    onDismiss()
                                }
                            },
                            enabled = title.isNotBlank() || content.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("editor_save_button")
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransparentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    contentColor: Color,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = contentColor.copy(alpha = 0.4f),
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = contentColor,
            fontSize = fontSize,
            fontWeight = fontWeight
        ),
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = modifier
    )
}
