package com.jogpal.app.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.jogpal.app.domain.chat.ChatMessage
import com.jogpal.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    partnerUid: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(partnerUid)),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var textMessage by remember { mutableStateOf("") }
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    
    val listState = rememberLazyListState()

    // Scroll to the latest message when it arrives
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = JogpalBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(JogpalPillBgLight)
                                .border(1.dp, JogpalPrimary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = JogpalPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = uiState.partnerProfile?.name ?: "VTH Health Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = JogpalOnBackgroundLight
                            )
                            Text(
                                text = "Active Running Assistant",
                                color = JogpalMutedTextLight,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = JogpalOnBackgroundLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JogpalBackgroundLight)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JogpalBackgroundLight)
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JogpalPrimary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages) { message ->
                        val isMe = message.senderUid == currentUid
                        ChatBubble(message = message, isMe = isMe)
                    }
                }
            }

            // INPUT BAR inspired by Ref UI 1 (Ask me anything...)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = Color(0x1F000000))
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .border(1.dp, JogpalCardBorderLight, RoundedCornerShape(28.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textMessage,
                        onValueChange = { textMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Ask me anything ...",
                                color = JogpalMutedTextLight,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (textMessage.isNotBlank()) {
                                viewModel.sendMessage(textMessage)
                                textMessage = ""
                            }
                        })
                    )

                    // Elevated Send Button (Ref UI 1 ↑ Icon)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, JogpalCardBorderLight, CircleShape)
                            .clickable {
                                if (textMessage.isNotBlank()) {
                                    viewModel.sendMessage(textMessage)
                                    textMessage = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Send",
                            tint = JogpalOnBackgroundLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(JogpalPillBgLight)
                    .border(1.dp, JogpalPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = JogpalPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0F000000))
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isMe) 20.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 20.dp
                    )
                )
                .background(if (isMe) JogpalPillBgLight else Color.White)
                .border(
                    1.dp,
                    if (isMe) JogpalPrimary.copy(alpha = 0.2f) else JogpalCardBorderLight,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.content,
                fontSize = 14.sp,
                color = JogpalOnBackgroundLight,
                lineHeight = 20.sp
            )
        }
    }
}

