package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Job
import ru.netology.nework.model.JobModelState
import ru.netology.nework.repository.job.JobRepository
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
    finish = null,
)

@HiltViewModel
@ExperimentalCoroutinesApi
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
    private val appAuth: AppAuth,
) : ViewModel() {

    private val _stateJob = MutableLiveData<JobModelState>()
    val stateJob: LiveData<JobModelState>
        get() = _stateJob

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    val edited = MutableLiveData(empty)

    val userId = MutableLiveData<Int>()

    val data: Flow<List<Job>> = appAuth.data
        .flatMapLatest { authState ->
            repository.data.map { jobs ->
                jobs.map {
                    it.copy(ownedByMe = authState?.id == userId.value)
                }
            }
        }

    fun save() {
        edited.value?.let {
            appAuth.getToken()?.let { token ->
                viewModelScope.launch {
                    try {
                        repository.saveJob(token, it)
                        _jobCreated.value = Unit
                        edited.value = empty
                        _stateJob.value = JobModelState()
                    } catch (e: Exception) {
                        _stateJob.value = JobModelState(error = true)
                    }
                }
            }
        }
    }

    fun getJobById(id: Int) = viewModelScope.launch {
        _stateJob.value = JobModelState(loading = true)
        try {
            repository.getJobsById(id)
            _stateJob.value = JobModelState()
        } catch (e: Exception) {
            _stateJob.value = JobModelState(error = true)
        }
    }

    fun editJob(job: Job) = viewModelScope.launch {
        edited.value = job
    }

    fun cancelEditJob() = viewModelScope.launch {
        edited.value = empty
    }

    fun startDate(startDate: String) {
        edited.value = edited.value?.copy(start = startDate)
    }

    fun finishDate(finishDate: String?) {
        edited.value = edited.value?.copy(finish = finishDate)
    }

    fun changeTextCompanyName(companyName: String) {
        val textCompanyName = companyName.trim()
        if (edited.value?.name == textCompanyName) {
            return
        }
        edited.value = edited.value?.copy(name = textCompanyName)
    }

    fun changeTextPosition(position: String) {
        val textPosition = position.trim()
        if (edited.value?.position == textPosition) {
            return
        }
        edited.value = edited.value?.copy(position = textPosition)
    }

    fun changeTextLink(link: String) {
        val textLink = link.trim()
        if (edited.value?.link == textLink) {
            return
        }
        edited.value = edited.value?.copy(link = textLink.ifBlank { null })
    }

    fun removeById(id: Int) =
        viewModelScope.launch {
            _stateJob.postValue(JobModelState(loading = true))
            try {
                appAuth.getToken()?.let { token ->
                    repository.removeJob(token, id)
                    _stateJob.postValue(JobModelState())
                }
            } catch (e: Exception) {
                _stateJob.postValue(JobModelState(error = true))
            }
        }
}
