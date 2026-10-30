package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.MediaRepository
import com.example.model.MediaItem
import com.example.model.Playlist
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class MediaRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addMediaItem_validPayload_createsDocumentAndReturnsId() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = MediaRepository(firestore)

        val item = MediaItem(
            id = "test_" + UUID.randomUUID().toString().replace("-", ""),
            userId = aliceUid,
            title = "Bohemian Rhapsody",
            type = "song",
            artistOrAuthor = "Queen",
            category = "Music"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addMediaItem(aliceUid, item)
        }
        assertTrue("Expected addMediaItem to succeed", result.isSuccess)
        val itemId = result.getOrThrow()
        assertNotNull(itemId)
        assertTrue(itemId.isNotEmpty())
    }

    @Test
    fun observeMediaItems_authenticatedOwner_emitsItem() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = MediaRepository(firestore)

        val testId = "test_" + UUID.randomUUID().toString().replace("-", "")
        val item = MediaItem(
            id = testId,
            userId = aliceUid,
            title = "YouTube Video: Kotlin Conf",
            type = "youtube",
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addMediaItem(aliceUid, item)
        }
        assertTrue(result.isSuccess)

        val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMediaItems(aliceUid).first { list ->
                list.any { it.id == testId }
            }
        }
        assertTrue(emittedList.any { it.id == testId })
    }

    @Test
    fun createPlaylist_validPayload_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = MediaRepository(firestore)

        val plId = "pl_" + UUID.randomUUID().toString().replace("-", "")
        val playlist = Playlist(
            id = plId,
            userId = aliceUid,
            name = "Chill Vibes",
            description = "Relaxing songs and podcasts"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createPlaylist(aliceUid, playlist)
        }
        assertTrue(result.isSuccess)
        assertEquals(plId, result.getOrThrow())
    }

    @Test
    fun observeMediaItems_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = MediaRepository(firestore)

        var failed = false
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeMediaItems("unauth_user").first()
            }
        } catch (e: Exception) {
            val rootException = e.unwrapFirestoreException()
            if (rootException?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                failed = true
            }
        }
        assertTrue("Expected permission denied exception when unauthenticated", failed)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
