    suspend fun copyVolume(src: DocumentFile, dst: DocumentFile): CopyVolumeResult {
        val dstRootDirectory = ObjRef<DocumentFile?>(null)
        val result = globalTask(R.string.copy_volume_notification, null, { taskId ->
            val total = recursiveCountChildElements(src)
            updateNotificationProgress(taskId, 0, total)
            recursiveCopyVolume(src, dst, dstRootDirectory, taskId, total)
        }, {
            dstRootDirectory.value?.delete()
        })
        return CopyVolumeResult(result, dstRootDirectory.value)
    }

    suspend fun duplicateFile(volumeId: Int, srcFilePath: String, dstFileName: String): TaskResult<out String?> {
        return volumeTask(R.string.file_op_copy_msg, 1, volumeId) { taskId, encryptedVolume ->
            val parentPath = PathUtils.getParentPath(srcFilePath)
            val dstFilePath = PathUtils.pathJoin(parentPath, dstFileName)

            if (encryptedVolume.pathExists(dstFilePath)) {
                return@volumeTask "File already exists"
            }

            if (!copyFile(encryptedVolume, srcFilePath, dstFilePath, encryptedVolume)) {
                return@volumeTask srcFilePath
            }

            updateNotificationProgress(taskId, 1, 1)
            null
        }
    }
}
