package com.example.sudoku.data.repository

import android.util.AtomicFile
import com.example.sudoku.data.json.SessionJson
import com.example.sudoku.domain.repository.LocalProgress
import com.example.sudoku.domain.repository.SessionRepository
import java.io.File
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** One atomic, versioned local save. All IO is serialized and stays off the UI thread. */
class FileSessionRepository(directory: File) : SessionRepository {
    private val file = AtomicFile(File(directory, "progress.json"))
    private val mutex = Mutex()

    override suspend fun load(): LocalProgress = withContext(Dispatchers.IO) {
        mutex.withLock {
            val text = try {
                file.openRead().bufferedReader().use { it.readText() }
            } catch (exception: FileNotFoundException) {
                if (file.baseFile.exists()) throw exception
                return@withLock LocalProgress()
            }
            SessionJson.decode(text)
        }
    }

    override suspend fun save(progress: LocalProgress): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            val bytes = SessionJson.encode(progress).toByteArray(Charsets.UTF_8)
            val output = file.startWrite()
            try {
                output.write(bytes)
                file.finishWrite(output)
            } catch (exception: Exception) {
                file.failWrite(output)
                throw exception
            }
        }
    }
}
