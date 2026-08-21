package com.jogpal.app.core.common

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

object FirebaseErrorMapper {
    fun map(exception: Throwable?): String {
        return when (exception) {
            is FirebaseAuthWeakPasswordException -> "Password is too weak."
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
            is FirebaseAuthUserCollisionException -> "An account already exists with this email."
            is FirebaseAuthInvalidUserException -> "No account found with this email."
            is FirebaseException -> "A network error occurred. Please try again."
            else -> "An unexpected error occurred. Please try again."
        }
    }
}
