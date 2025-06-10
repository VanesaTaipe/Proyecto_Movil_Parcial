package com.example.proyecto_movil_parcial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    // Registro del launcher para la actividad de inicio de sesión de Google
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { firebaseAuthWithGoogle(it) }
        } catch (e: ApiException) {
            android.util.Log.e("SignInActivity", "Google sign in failed: ${e.message}", e)
            Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario ya está autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            android.util.Log.d("SignInActivity", "Usuario ya autenticado, redirigiendo a LoadingActivity")
            // El usuario ya está conectado, ir a LoadingActivity para verificar estado
            startActivity(Intent(this, LoadingActivity::class.java))
            finish()
            return
        }

        // Configurar la UI con Compose
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SimpleGoogleLoginScreen(
                        onGoogleLoginClick = { signIn() }
                    )
                }
            }
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Cerrar sesión primero para asegurar selección de cuenta
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    android.util.Log.d("SignInActivity", "Auth exitoso - Usuario: ${user?.displayName}")
                    android.util.Log.d("SignInActivity", "Es nuevo usuario: $isNewUser")

                    Toast.makeText(this, "Bienvenido ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    // Ir a LoadingActivity con información de si es nuevo usuario
                    val intent = Intent(this, LoadingActivity::class.java)
                    intent.putExtra("isFirstTime", isNewUser)
                    startActivity(intent)
                    finish()
                } else {
                    android.util.Log.e("SignInActivity", "Error de autenticación: ${task.exception?.message}", task.exception)
                    Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Composable
fun SimpleGoogleLoginScreen(
    onGoogleLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Imagen que ocupa toda la parte superior (70% de la pantalla)
        Image(
            painter = painterResource(id = R.drawable.imagelogin),
            contentDescription = "Login illustration",
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f),
            contentScale = ContentScale.Crop
        )

        // Contenido en la parte inferior (30% de la pantalla)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .background(Color.White)
                .padding(horizontal = 32.dp)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título de la app
            Text(
                text = "AiWordFlow",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Descripción
            Text(
                text = "Crea tu diccionario personal,\naprende con IA y mejora tu\nvocabulario creando oraciones",
                fontSize = 20.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Botón de Google Sign In
            GoogleSignInButton(
                onClick = onGoogleLoginClick
            )
        }
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icono de Google
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Iniciar sesión con Google",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun SimpleGoogleLoginPreview() {
    MaterialTheme {
        SimpleGoogleLoginScreen(
            onGoogleLoginClick = { }
        )
    }
}