package com.toka.studyboost.funciones_pantallas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toka.studyboost.MainApplication
import com.toka.studyboost.datos.Flashcard
import com.toka.studyboost.red.RepositorioEstudio
import com.toka.studyboost.red.MockRepositorioEstudio
import com.toka.studyboost.utils.AlgoritmoSM2
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel dedicado al sistema de flashcards con repetición espaciada (SM-2).
 */
class FlashcardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MainApplication
    private val repositorio: RepositorioEstudio = MockRepositorioEstudio(app.database)

    /**
     * Flashcards cuya fecha de revisión ya llegó.
     * Se actualiza automáticamente desde Room cada vez que se califica una tarjeta.
     */
    val tarjetasParaHoy = repositorio.observarFlashcardsParaHoy()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Registra la calificación del usuario (0-5) y actualiza el SM-2.
     *
     * @param flashcard La tarjeta que acaba de responder.
     * @param calidad   0 = no recordé nada, 5 = perfecto.
     */
    fun calificar(flashcard: Flashcard, calidad: Int) {
        viewModelScope.launch {
            val actualizada = AlgoritmoSM2.calcularProximaRevision(flashcard, calidad)
            repositorio.actualizarFlashcard(actualizada)
        }
    }

    fun observarFlashcardsDeSesion(idSesion: String) =
        repositorio.observarFlashcardsDeSesion(idSesion)
}
