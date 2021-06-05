package com.xcaret.loyaltyreps.util

import java.io.File

abstract class AlbumStorageDirFactory {
    abstract fun getAlbumStorageDir(albumName: String): File
}
