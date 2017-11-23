package tr.edu.iyte.filepicker.item

/**
 * A [FileItem] that represents data storage units (SD Cards, Internal Storage)
 * @param name Name of the storage unit
 * @param path Path of the storage unit
 * @param isInternal Indicates whether this storage unit is internal or not
 */
internal data class StorageFileItem(override val name: String,
                                    val path: String,
                                    val isInternal: Boolean) : FileItem {
    /**
     * Flag for whether this is a directory or a file. A storage unit is always a directory.
     */
    override val isDirectory = true
}