package io.silv.checkers.screens

import android.app.Activity
import android.content.IntentSender
import android.provider.Settings.Global.getString
import android.util.Log
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.lifecycle.ViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import io.silv.api.clientId
import io.silv.checkers.MainActivity
import io.silv.checkers.R


const val TAG = "OneTap"

class AuthScreenState(activity: Activity) {

    val oneTapClient = Identity.getSignInClient(activity)

    val signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setNonce(null)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(clientId)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()
}

@Composable
fun rememberAuthScreenState(
    activity: Activity
) = remember {
    AuthScreenState(activity)
}

@Composable
fun AuthScreen(
    nonce: String? = null,
    credentials: String = clientId,
    tokenReceived: (token: String, credentials: SignInCredential) -> Unit
) {

    val ctx = LocalContext.current
    val activity = LocalContext.current as Activity

    val state = rememberAuthScreenState(activity = activity)

    var inProgress by remember {
        mutableStateOf(false)
    }

    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK) {
                val oneTapClient = Identity.getSignInClient(activity)
                val credentials = oneTapClient.getSignInCredentialFromIntent(result.data)
                val tokenId = credentials.googleIdToken
                if (tokenId != null) {
                    tokenReceived(tokenId, credentials)
                }
            } else {

            }
        } catch (e: ApiException) {
            Log.e(TAG, "${e.message}")
        } finally {
            inProgress = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                        state.oneTapClient
                            .beginSignIn(state.signInRequest)
                            .addOnSuccessListener { result ->
                                try {
                                    inProgress = true
                                    activityLauncher.launch(
                                        IntentSenderRequest
                                            .Builder(
                                                result.pendingIntent.intentSender
                                            )
                                            .build()
                                    )
                                } catch (e: Exception) {
                                    inProgress = false
                                    Log.e(TAG, "${e.message}")
                                }
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "${it.message}")
                            }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = stringResource(id = R.string.google),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
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
                visible = inProgress,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.width(150.dp).height(80.dp)
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                )
            }
        }
    }
}