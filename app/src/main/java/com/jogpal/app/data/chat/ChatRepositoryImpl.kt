package com.jogpal.app.data.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jogpal.app.domain.chat.ChatMessage
import com.jogpal.app.domain.chat.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ChatRepository {

    private fun getChatRoomId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    override fun getMessages(partnerUid: String): Flow<List<ChatMessage>> = callbackFlow {
        val currentUid = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val chatRoomId = getChatRoomId(currentUid, partnerUid)
        
        val listener = firestore.collection("chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(partnerUid: String, content: String): Result<Unit> {
        return try {
            val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val chatRoomId = getChatRoomId(currentUid, partnerUid)
            
            val message = ChatMessage(
                senderUid = currentUid,
                receiverUid = partnerUid,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            
            firestore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(message)
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
