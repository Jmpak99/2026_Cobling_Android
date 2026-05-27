package com.cobling.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobling.app.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // 편의 프로퍼티
    val userProfileValue: UserProfile? get() = _userProfile.value
    val isPremiumActive: Boolean get() = _userProfile.value?.premium?.isActive == true
    val currentUserId: String get() = auth.currentUser?.uid ?: ""

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            _isSignedIn.value = user != null
            _currentUserEmail.value = user?.email

            if (user != null) {
                viewModelScope.launch {
                    ensureUserDocument(
                        uid = user.uid,
                        email = user.email,
                        nickname = user.displayName
                    )
                    loadUserProfile(user.uid)
                }
            } else {
                _userProfile.value = null
            }
        }
    }

    /**
     * Firestore Trigger 기반 Cloud Functions와 맞추기 위한 안전장치입니다.
     *
     * 서버의 initUserProgress는 users/{uid} 문서가 처음 생성될 때만 실행됩니다.
     * 그래서 이메일 가입, 이메일 로그인, Google 로그인, 앱 재실행 경로 모두에서
     * users/{uid} 문서가 없으면 반드시 생성해 줘야 progress 초기화 트리거가 실행됩니다.
     */
    private suspend fun ensureUserDocument(
        uid: String,
        email: String?,
        nickname: String? = null
    ) {
        val userRef = db.collection("users").document(uid)
        val snap = userRef.get().await()

        val baseData = mutableMapOf<String, Any>(
            "email" to (email ?: ""),
            "lastLogin" to FieldValue.serverTimestamp()
        )

        if (!nickname.isNullOrBlank()) {
            baseData["nickname"] = nickname
        }

        if (!snap.exists()) {
            baseData["createdAt"] = FieldValue.serverTimestamp()
            userRef.set(baseData).await()
        } else {
            // 기존 유저는 onCreate 트리거를 다시 발생시키지 않도록 merge 업데이트만 합니다.
            userRef.set(baseData, SetOptions.merge()).await()
        }
    }

    private suspend fun loadUserProfile(uid: String) {
        try {
            val snap = db.collection("users").document(uid).get().await()

            if (snap.exists()) {
                _userProfile.value = snap.toObject(UserProfile::class.java)
            }
        } catch (e: Exception) {
            _authError.value = e.message
        }
    }

    suspend fun signIn(
        email: String,
        password: String
    ) {
        try {
            _authError.value = null

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return

            ensureUserDocument(
                uid = user.uid,
                email = user.email ?: email
            )

            loadUserProfile(user.uid)
        } catch (e: Exception) {
            _authError.value = e.message
            throw e
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        nickname: String? = null
    ) {
        try {
            _authError.value = null

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return

            ensureUserDocument(
                uid = user.uid,
                email = user.email ?: email,
                nickname = nickname
            )

            loadUserProfile(user.uid)
        } catch (e: Exception) {
            _authError.value = e.message
            throw e
        }
    }

    suspend fun signInWithGoogle(idToken: String) {
        try {
            _authError.value = null

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return

            ensureUserDocument(
                uid = user.uid,
                email = user.email,
                nickname = user.displayName
            )

            loadUserProfile(user.uid)
        } catch (e: Exception) {
            _authError.value = e.message
            throw e
        }
    }

    suspend fun resetPassword(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            _authError.value = e.message
            throw e
        }
    }

    fun signOut() {
        auth.signOut()

        _isSignedIn.value = false
        _currentUserEmail.value = null
        _userProfile.value = null
    }

    suspend fun refreshUserProfileIfNeeded() {
        val uid = auth.currentUser?.uid ?: return
        loadUserProfile(uid)
    }

    suspend fun completeEvolutionIfNeeded() {
        val uid = auth.currentUser?.uid ?: return

        try {
            db.collection("users").document(uid)
                .update(mapOf("character.evolutionPending" to false))
                .await()

            refreshUserProfileIfNeeded()
        } catch (e: Exception) {
            _authError.value = e.message
        }
    }

    suspend fun updateNickname(nickname: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("로그인 필요")

        db.collection("users").document(uid)
            .update("nickname", nickname)
            .await()

        refreshUserProfileIfNeeded()
    }

    suspend fun updateEmail(newEmail: String) {
        val user = auth.currentUser ?: throw Exception("로그인 필요")

        user.updateEmail(newEmail).await()

        db.collection("users").document(user.uid)
            .update("email", newEmail)
            .await()

        _currentUserEmail.value = newEmail
    }

    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser ?: throw Exception("로그인 필요")

        user.updatePassword(newPassword).await()
    }

    /**
     * 기존 탈퇴 함수입니다.
     *
     * 단, Firebase Auth는 민감한 작업에 최근 로그인을 요구하므로
     * 로그인한 지 오래된 사용자는 이 함수에서 오류가 날 수 있습니다.
     *
     * 실제 탈퇴 화면에서는 deleteAccountWithPassword() 사용을 권장합니다.
     */
    suspend fun deleteAccount() {
        val user = auth.currentUser ?: throw Exception("로그인 필요")
        val uid = user.uid

        db.collection("users").document(uid)
            .delete()
            .await()

        user.delete().await()

        signOut()
    }

    /**
     * 이메일/비밀번호 로그인 사용자를 위한 재인증 후 탈퇴 함수입니다.
     *
     * 흐름:
     * 1. 현재 로그인 유저 확인
     * 2. 이메일 + 입력한 비밀번호로 재인증
     * 3. Firestore users/{uid} 문서 삭제
     * 4. Firebase Auth 계정 삭제
     * 5. 로컬 로그인 상태 초기화
     */
    suspend fun deleteAccountWithPassword(password: String) {
        val user = auth.currentUser ?: throw Exception("로그인 필요")

        val email = user.email ?: throw Exception("이메일 정보를 찾을 수 없습니다.")

        val credential = EmailAuthProvider.getCredential(email, password)

        // 최근 로그인 인증 갱신
        user.reauthenticate(credential).await()

        val uid = user.uid

        // Firestore 유저 문서 삭제
        db.collection("users").document(uid)
            .delete()
            .await()

        // Firebase Auth 계정 삭제
        user.delete().await()

        signOut()
    }

    suspend fun saveFcmTokenToUserDoc(token: String) {
        val uid = auth.currentUser?.uid ?: return

        try {
            db.collection("users").document(uid)
                .update("fcmToken", token)
                .await()
        } catch (_: Exception) {
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }
}