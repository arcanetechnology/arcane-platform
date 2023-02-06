package com.k33.platform.filestore

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.k33.platform.utils.config.loadConfigEager

object FileStoreService {

    fun upload(
        fileId: String,
        content: ByteArray,
    ) {
        val config = loadConfigEager<Config>(name = "gcs", path = "gcs.$fileId")
        val storage: Storage = StorageOptions.getDefaultInstance().service
        val blobId: BlobId = BlobId.of(config.bucketName, config.objectName)
        val blobInfo: BlobInfo = BlobInfo.newBuilder(blobId).build()
        storage.create(blobInfo, content)
    }

    fun download(
        fileId: String,
    ): ByteArray {
        val config = loadConfigEager<Config>(name = "gcs", path = "gcs.$fileId")
        val storage: Storage = StorageOptions.getDefaultInstance().service
        val blobId: BlobId = BlobId.of(config.bucketName, config.objectName)
        return storage.readAllBytes(blobId)
    }
}