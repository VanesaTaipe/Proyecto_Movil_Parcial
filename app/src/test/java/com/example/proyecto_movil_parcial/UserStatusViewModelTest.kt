package com.example.proyecto_movil_parcial

// Librerías de test
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

// Firebase mocks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.OnSuccessListener

// Librerías de MockK
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

// Tu ViewModel y modelo de estados
import com.example.proyecto_movil_parcial.viewmodel.UserStatus
import com.example.proyecto_movil_parcial.viewmodel.UserStatusViewModel


@OptIn(ExperimentalCoroutinesApi::class)
class UserStatusViewModelTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var viewModel: UserStatusViewModel

    @Before
    fun setup() {
        mockAuth = mockk()
        mockUser = mockk()
        mockFirestore = mockk()
        viewModel = UserStatusViewModel(mockAuth, mockFirestore)
    }

    /**
     * Test: Si no hay usuario logueado, debe emitirse UserStatus.NotLoggedIn
     */
    @Test
    fun `user not logged in emits NotLoggedIn`() = runTest {
        every { mockAuth.currentUser } returns null

        viewModel.checkUserStatus()

        viewModel.status.test {
            assert(awaitItem() is UserStatus.NotLoggedIn)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test: Usuario logueado, no hay documento → necesita setup
     */
    @Test
    fun `logged in but no document emits NeedsSetup`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "123"

        val mockDocument = mockk<DocumentSnapshot> {
            every { exists() } returns false
        }

        val docRef = mockk<com.google.android.gms.tasks.Task<DocumentSnapshot>>(relaxed = true)
        val successSlot = slot<OnSuccessListener<DocumentSnapshot>>()

        every { mockFirestore.collection("users").document("123").get() } returns docRef
        every { docRef.addOnSuccessListener(capture(successSlot)) } answers {
            successSlot.captured.onSuccess(mockDocument)
            docRef
        }

        viewModel.checkUserStatus()

        viewModel.status.test {
            assert(awaitItem() is UserStatus.NeedsSetup)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Navega al flujo principal correctamente
    @Test
    fun `logged in with valid maxPalabrasDia emits Ready`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "123"

        val mockDocument = mockk<DocumentSnapshot> {
            every { exists() } returns true
            every { getLong("maxPalabrasDia") } returns 25L
        }

        val docRef = mockk<
                com.google.android.gms.tasks.Task<DocumentSnapshot>
                >(relaxed = true)
        val successSlot = slot<
                OnSuccessListener<DocumentSnapshot>
                >()

        // mock del get()
        every { mockFirestore.collection("users").document("123").get() } returns docRef
        every { docRef.addOnSuccessListener(capture(successSlot)) } answers {
            successSlot.captured.onSuccess(mockDocument)
            docRef
        }

        // Mock del update("ultimoAcceso", Timestamp.now())
        // simula el .update(...) que ocurre dentro del ViewModel,
        // evitando que MockK lance un error por no saber qué hacer con esa llamada.
        every {
            mockFirestore.collection("users").document("123").update("ultimoAcceso", any())
        } returns mockk()
        viewModel.checkUserStatus()

        viewModel.status.test {
            assert(awaitItem() is UserStatus.LoggedIn)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Detecta usuarios incompletos y los redirige
    @Test
    fun `logged in with document missing maxPalabrasDia emits NeedsSetup`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "123"

        val mockDocument = mockk<DocumentSnapshot> {
            every { exists() } returns true
            every { getLong("maxPalabrasDia") } returns null // Falta el campo
        }

        val docRef = mockk<com.google.android.gms.tasks.Task<DocumentSnapshot>>(relaxed = true)
        val successSlot = slot<OnSuccessListener<DocumentSnapshot>>()

        every { mockFirestore.collection("users").document("123").get() } returns docRef
        every { docRef.addOnSuccessListener(capture(successSlot)) } answers {
            successSlot.captured.onSuccess(mockDocument)
            docRef
        }

        viewModel.checkUserStatus()

        viewModel.status.test {
            assert(awaitItem() is UserStatus.NeedsSetup)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test que verifica qué pasa cuando ocurre un error en la red (Firestore falla)
    @Test
    fun `logged in but firestore fails emits Retry`() = runTest {

        // Simulamos que hay un usuario autenticado
        every { mockAuth.currentUser } returns mockUser

        // Le damos un ID ficticio (como si fuera un usuario real)
        every { mockUser.uid } returns "123"

        // Creamos un mock del objeto Task que representa una llamada a Firestore
        val docRef = mockk<com.google.android.gms.tasks.Task<DocumentSnapshot>>(relaxed = true)

        // 🔌 Simulamos que al pedir el documento del usuario, nos devuelve este mock
        every { mockFirestore.collection("users").document("123").get() } returns docRef

        // 💥 Aquí viene la parte clave:
        // Simulamos que Firestore lanza un error → como si la conexión fallara
        every { docRef.addOnFailureListener(any()) } answers {
            // Disparamos manualmente el "callback" de fallo
            firstArg<((Exception) -> Unit)>().invoke(Exception("Firestore error"))
            docRef // 🔁 Devolvemos el mismo objeto simulado
        }

        // Ejecutamos la función que consulta el estado del usuario
        viewModel.checkUserStatus()

        // 🔬 Observamos el flujo del estado del ViewModel usando Turbine
        viewModel.status.test {
            // ✅ Verificamos que el estado emitido sea `Retry` (para volver a intentar)
            assert(awaitItem() is UserStatus.Error)
            // 🛑 Cancelamos la escucha porque ya obtuvimos lo que queríamos
            cancelAndIgnoreRemainingEvents()
        }
    }



}
