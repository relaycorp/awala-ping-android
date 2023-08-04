package tech.relaycorp.ping.ui.peers

import android.net.Uri
import kotlinx.coroutines.flow.*
import tech.relaycorp.ping.common.PublishFlow
import tech.relaycorp.ping.common.element
import tech.relaycorp.ping.data.ReadFile
import tech.relaycorp.ping.domain.AddPublicPeer
import tech.relaycorp.ping.domain.InvalidConnectionParams
import tech.relaycorp.ping.ui.BaseViewModel
import tech.relaycorp.ping.ui.common.Click
import tech.relaycorp.ping.ui.common.Finish
import tech.relaycorp.ping.ui.common.clicked
import tech.relaycorp.ping.ui.common.finish
import java.util.*
import javax.inject.Inject

class AddPublicPeerViewModel
@Inject constructor(
    addPublicPeer: AddPublicPeer,
    readFile: ReadFile
) : BaseViewModel() {

    // Inputs

    private val connectionParamsFilePicks = MutableStateFlow(Optional.empty<Uri>())
    fun connectionParamsFilePicked(value: Uri) {
        connectionParamsFilePicks.value = Optional.of(value)
    }

    private val removeConnectionParamsFileClicks = PublishFlow<Click>()
    fun removeConnectionParamsFileClicked() {
        removeConnectionParamsFileClicks.clicked()
    }

    private val saveClicks = PublishFlow<Click>()
    fun saveClicked() {
        saveClicks.clicked()
    }

    // Outputs

    private val _connectionParamsFile = MutableStateFlow(Optional.empty<String>())
    fun connectionParamsFile(): Flow<Optional<String>> = _connectionParamsFile.asStateFlow()

    private val _errors = PublishFlow<Error>()
    fun errors(): Flow<Error> = _errors.asSharedFlow()

    private val _finish = PublishFlow<Finish>()
    fun finish(): Flow<Finish> = _finish.asSharedFlow()

    init {
        connectionParamsFilePicks
            .element()
            .onEach { _connectionParamsFile.value = Optional.of(readFile.getFileName(it)) }
            .launchIn(backgroundScope)

        removeConnectionParamsFileClicks
            .asSharedFlow()
            .onEach {
                connectionParamsFilePicks.value = Optional.empty()
                _connectionParamsFile.value = Optional.empty()
            }
            .launchIn(backgroundScope)

        saveClicks
            .asSharedFlow()
            .flatMapLatest { connectionParamsFilePicks.take(1) }
            .map { filePick ->
                if (filePick.isPresent) {
                    val connectionParams = readFile.read(filePick.get())
                    addPublicPeer.add(connectionParams)
                    _finish.finish()
                } else {
                    _errors.emit(Error.MissingConnectionParams)
                }
            }
            .catch { exception ->
                _errors.emit(
                    when (exception) {
                        is InvalidConnectionParams -> Error.InvalidConnectionParams
                        else -> Error.GenericSave
                    }
                )
                emit(Unit)
            }
            .launchIn(backgroundScope)
    }

    enum class Error {
        MissingConnectionParams, InvalidConnectionParams, GenericSave
    }
}
