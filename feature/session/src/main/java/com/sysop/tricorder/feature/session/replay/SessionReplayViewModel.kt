package com.sysop.tricorder.feature.session.replay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.database.dao.SessionDao
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.database.entity.SessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReplayState(
    val session: SessionEntity? = null,
    val allReadings: List<ReadingEntity> = emptyList(),
    val visibleReadings: List<ReadingEntity> = emptyList(),
    val isPlaying: Boolean = false,
    val speed: Float = 1f,
    val currentTimeMs: Long = 0L,
    val durationMs: Long = 0L,
    val progress: Float = 0f,
)

@HiltViewModel
class SessionReplayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionDao: SessionDao,
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _state = MutableStateFlow(ReplayState())
    val state: StateFlow<ReplayState> = _state.asStateFlow()

    private var playbackJob: Job? = null

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            val session = sessionDao.getSession(sessionId) ?: return@launch
            val readings = sessionDao.getReadingsForSession(sessionId).first()
            val durationMs = if (session.endTime != null) {
                session.endTime - session.startTime
            } else if (readings.isNotEmpty()) {
                readings.last().timestamp - session.startTime
            } else {
                0L
            }

            _state.update {
                it.copy(
                    session = session,
                    allReadings = readings,
                    durationMs = durationMs,
                    currentTimeMs = 0L,
                    progress = 0f,
                    visibleReadings = emptyList(),
                )
            }
        }
    }

    fun togglePlayback() {
        if (_state.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun setSpeed(speed: Float) {
        _state.update { it.copy(speed = speed) }
    }

    fun seekTo(progress: Float) {
        val currentState = _state.value
        val targetMs = (progress * currentState.durationMs).toLong()
        val session = currentState.session ?: return
        val absoluteTime = session.startTime + targetMs

        val visible = currentState.allReadings.filter { it.timestamp <= absoluteTime }

        _state.update {
            it.copy(
                currentTimeMs = targetMs,
                progress = progress,
                visibleReadings = visible,
            )
        }
    }

    private fun play() {
        _state.update { it.copy(isPlaying = true) }

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val tickInterval = 50L // ms per tick

            while (_state.value.isPlaying) {
                val current = _state.value
                if (current.currentTimeMs >= current.durationMs) {
                    _state.update { it.copy(isPlaying = false) }
                    break
                }

                val advanceMs = (tickInterval * current.speed).toLong()
                val newTimeMs = (current.currentTimeMs + advanceMs).coerceAtMost(current.durationMs)
                val newProgress = if (current.durationMs > 0) {
                    newTimeMs.toFloat() / current.durationMs.toFloat()
                } else {
                    0f
                }

                val session = current.session ?: break
                val absoluteTime = session.startTime + newTimeMs
                val visible = current.allReadings.filter { it.timestamp <= absoluteTime }

                _state.update {
                    it.copy(
                        currentTimeMs = newTimeMs,
                        progress = newProgress,
                        visibleReadings = visible,
                    )
                }

                delay(tickInterval)
            }
        }
    }

    private fun pause() {
        _state.update { it.copy(isPlaying = false) }
        playbackJob?.cancel()
        playbackJob = null
    }

    override fun onCleared() {
        playbackJob?.cancel()
        super.onCleared()
    }
}
