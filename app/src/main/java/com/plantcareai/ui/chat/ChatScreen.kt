package com.plantcareai.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plantcareai.R
import com.plantcareai.ai.ModelState
import com.plantcareai.ui.chat.components.ChatInput
import com.plantcareai.ui.chat.components.EmptyChat
import com.plantcareai.ui.chat.components.ImagePickerSheet
import com.plantcareai.ui.chat.components.MessageBubble
import com.plantcareai.ui.history.ConversationHistoryDrawer
import com.plantcareai.ui.history.HistoryViewModel
import com.plantcareai.util.ImageUtils
import kotlinx.coroutines.launch
import java.io.File

/**
 * Main chat screen composable.
 *
 * Provides the full ChatGPT-style interface with:
 * - Navigation drawer for conversation history
 * - Top app bar with new chat and settings actions
 * - Scrollable message list
 * - Chat input with image attachment support
 * - Camera and gallery integration
 * - Model loading state indicator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel()
) {
    val uiState by chatViewModel.uiState.collectAsState()
    val conversations by historyViewModel.conversations.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()

    // Camera file handling
    var cameraImageFile by remember { mutableStateOf<File?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { chatViewModel.attachImage(it) }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageFile?.let { file ->
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.let { chatViewModel.attachCameraImage(it) }
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = ImageUtils.createTempImageFile(context)
            cameraImageFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.camera_permission_rationale),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Start a new conversation if none exists
    LaunchedEffect(Unit) {
        if (uiState.currentConversationId == null) {
            chatViewModel.startNewConversation()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationHistoryDrawer(
                conversations = conversations,
                currentConversationId = uiState.currentConversationId,
                onConversationClick = { conversation ->
                    chatViewModel.loadConversation(conversation.id)
                    scope.launch { drawerState.close() }
                },
                onDeleteConversation = { conversation ->
                    historyViewModel.deleteConversation(conversation)
                },
                onNewChat = {
                    chatViewModel.startNewConversation()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "PlantCare AI",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.conversation_history)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { chatViewModel.startNewConversation() }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.new_chat)
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                ChatInput(
                    inputText = uiState.inputText,
                    attachedImage = uiState.attachedImage,
                    isGenerating = uiState.isGenerating,
                    onInputChange = chatViewModel::updateInput,
                    onAttachClick = { chatViewModel.toggleImagePicker(true) },
                    onRemoveImage = chatViewModel::removeAttachedImage,
                    onSendClick = chatViewModel::sendMessage
                )
            },
            modifier = modifier
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Model loading indicator
                when (val state = uiState.modelState) {
                    is ModelState.Loading -> {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }
                    is ModelState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    else -> { /* Clean ChatGPT-style interface for Ready and DemoMode */ }
                }

                // Messages
                if (uiState.messages.isEmpty() && uiState.currentConversationId != null) {
                    EmptyChat(
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { message ->
                            MessageBubble(message = message)
                        }
                    }
                }
            }
        }
    }

    // Image picker bottom sheet
    if (uiState.showImagePicker) {
        ImagePickerSheet(
            onDismiss = { chatViewModel.toggleImagePicker(false) },
            onCameraClick = {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val file = ImageUtils.createTempImageFile(context)
                    cameraImageFile = file
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    cameraLauncher.launch(uri)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onGalleryClick = {
                galleryLauncher.launch("image/*")
            }
        )
    }
}
