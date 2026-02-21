package com.sysop.tricorder.feature.session.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.database.dao.SessionDao
import com.sysop.tricorder.core.database.entity.SessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionWithCount(
    val session: SessionEntity,
    val readingCount: Int,
)

@HiltViewModel
class SessionListViewModel @Inject constructor(
    private val sessionDao: SessionDao,
) : ViewModel() {

    val sessions: StateFlow<List<SessionWithCount>> = sessionDao.getAllSessions()
        .map { sessions ->
            sessions.map { session ->
                SessionWithCount(
                    session = session,
                    readingCount = sessionDao.getReadingCount(session.id),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch {
            sessionDao.deleteSession(session)
        }
    }
}
