package io.silv.checkers.screens

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import io.silv.api.webclientId
import io.silv.checkers.R


const val TAG = "OneTap"


@Composable
fun AuthScreen(
    modifier: Modifier,
    signInAnonymously: () -> Unit,
    tokenReceived: (token: String) -> Unit
) {


    val oneTapSignInState = rememberOneTapSignInState()
    val ctx = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0xff27272a),
                        Color(0xff18181b),
                    )
                )
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OneTapSignInWithGoogle(
            state = oneTapSignInState,
            clientId = webclientId,
            onTokenIdReceived = tokenReceived,
            onDialogDismissed = {
                oneTapSignInState.close()
                Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show()
            }
        )
        Text(
            text = stringResource(R.string.sign_in),
            modifier = Modifier.fillMaxWidth(0.9f),
            textAlign = TextAlign.Center,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val shape = RoundedCornerShape(32.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(shape)
                    .border(1.dp, Color.LightGray, shape)
                    .clickable {
                        signInAnonymously()
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "anonymous sign in",
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.sign_in_anon),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(shape)
                    .border(1.dp, Color.LightGray, shape)
                    .clickable {
                        oneTapSignInState.open()
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = stringResource(id = R.string.google),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.google_sign_in),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.google_dots_loading)
            )
            val progress by rememberInfiniteTransition().animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                )
            )
            AnimatedVisibility(
                visible = oneTapSignInState.opened,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .width(150.dp)
                    .height(80.dp)
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                )
            }
        }
    }
}

class OneTapSignInState {
    var opened by mutableStateOf(false)
        private set

    fun open() {
        opened = true
    }

    internal fun close() {
        opened = false
    }
}

@Composable
fun rememberOneTapSignInState(): OneTapSignInState {
    return remember { OneTapSignInState() }
}


/**
 * Composable that allows you to easily integrate One-Tap Sign in with Google
 * in your project.
 * @param state - One-Tap Sign in State.
 * @param clientId - CLIENT ID (Web) of your project, that you can obtain from
 * a Google Cloud Platform.
 * @param nonce - Optional nonce that can be used when generating a Google Token ID.
 * @param onTokenIdReceived - Lambda that will be triggered after a successful
 * authentication. Returns a Token ID.
 * @param onDialogDismissed - Lambda that will be triggered when One-Tap dialog
 * disappears. Returns a message in a form of a string.
 * */
@Composable
fun OneTapSignInWithGoogle(
    state: OneTapSignInState,
    clientId: String = webclientId,
    nonce: String? = null,
    onTokenIdReceived: (String) -> Unit,
    onDialogDismissed: (String) -> Unit,
) {
    val activity = LocalContext.current as Activity
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK) {
                val oneTapClient = Identity.getSignInClient(activity)
                val credentials = oneTapClient.getSignInCredentialFromIntent(result.data)
                val tokenId = credentials.googleIdToken
                if (tokenId != null) {
                    onTokenIdReceived(tokenId)
                    state.close()
                }
            } else {
                Log.d(TAG, result.data.toString())
                onDialogDismissed("Dialog Closed.")
                state.close()
            }
        } catch (e: ApiException) {
            Log.e(TAG, "${e.message}")
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    onDialogDismissed("Dialog Canceled.")
                    state.close()
                }
                CommonStatusCodes.NETWORK_ERROR -> {
                    onDialogDismissed("Network Error.")
                    state.close()
                }
                else -> {
                    onDialogDismissed(e.message.toString())
                    state.close()
                }
            }
        }
    }

    LaunchedEffect(key1 = state.opened) {
        if (state.opened) {
            signIn(
                activity = activity,
                clientId = clientId,
                nonce = nonce,
                launchActivityResult = { intentSenderRequest ->
                    activityLauncher.launch(intentSenderRequest)
                },
                onError = {
                    onDialogDismissed(it)
                    state.close()
                }
            )
        }
    }
}

private fun signIn(
    activity: Activity,
    clientId: String,
    nonce: String?,
    launchActivityResult: (IntentSenderRequest) -> Unit,
    onError: (String) -> Unit
) {
    val oneTapClient = Identity.getSignInClient(activity)
    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setNonce(nonce)
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(true)
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()

    oneTapClient.beginSignIn(signInRequest)
        .addOnSuccessListener { result ->
            try {
                launchActivityResult(
                    IntentSenderRequest.Builder(
                        result.pendingIntent.intentSender
                    ).build()
                )
            } catch (e: Exception) {
                onError(e.message.toString())
            }
        }
        .addOnFailureListener {
            signUp(
                activity = activity,
                clientId = clientId,
                nonce = nonce,
                launchActivityResult = launchActivityResult,
                onError = onError
            )
            Log.e(TAG, "${it.message} sign in")
        }
}

private fun signUp(
    activity: Activity,
    clientId: String,
    nonce: String?,
    launchActivityResult: (IntentSenderRequest) -> Unit,
    onError: (String) -> Unit
) {
    val oneTapClient = Identity.getSignInClient(activity)
    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setNonce(nonce)
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .build()

    oneTapClient.beginSignIn(signInRequest)
        .addOnSuccessListener { result ->
            try {
                launchActivityResult(
                    IntentSenderRequest.Builder(
                        result.pendingIntent.intentSender
                    ).build()
                )
            } catch (e: Exception) {
                onError(e.message.toString())
                Log.e(TAG, "${e.message}")
            }
        }
        .addOnFailureListener {
            onError("Google Account not Found.")
            Log.e(TAG, "sign up ${it.message}")
        }
}
