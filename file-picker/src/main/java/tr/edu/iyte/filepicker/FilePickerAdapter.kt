package tr.edu.iyte.filepicker

import android.content.Context
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import tr.edu.iyte.filepicker.helper.find
import tr.edu.iyte.filepicker.item.*
import tr.edu.iyte.filepicker.style.FilePickerItemStyle

internal class FilePickerAdapter(private val context: Context,
                                 private val style: FilePickerItemStyle,
                                 private val notifyOnChange: Boolean = true,
                                 private val onItemClick: (FileItem) -> Unit) :
        RecyclerView.Adapter<FilePickerAdapter.ViewHolder>() {
    open inner class ViewHolder(val background: View) : RecyclerView.ViewHolder(background) {
        val img = background.find<ImageView>(R.id.icon)
        val fileName = background.find<TextView>(R.id.name)
    }

    inner class ViewHolderS(v: View) : ViewHolder(v) {
        val path = v.find<TextView>(R.id.path)
    }

    private val files = mutableListOf<FileItem>()
    private val up = UpFileItem(context.getString(R.string.file_picker_folder_up))
    private val inflater = LayoutInflater.from(context)
    private val lock = Any()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = when(viewType) {
        R.layout.file_picker_list_item_storage ->
            ViewHolderS(inflater.inflate(R.layout.file_picker_list_item_storage, parent, false))
        else                                   ->
            ViewHolder(inflater.inflate(R.layout.file_picker_list_item_file, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = files[position]
        if(holder is ViewHolderS) {
            holder.path.text = (item as StorageFileItem).path
            holder.path.setTextColor(style.secondaryTextColor)
        }

        holder.background.setBackgroundColor(style.backgroundColor)
        // TODO implement background ripple generation

        holder.img.setImageDrawable(context.getDrawable(
                when {
                    item is UpFileItem      -> R.drawable.file_picker_ic_folder_up_black_24dp
                    item is StorageFileItem -> {
                        if(item.isInternal)
                            R.drawable.file_picker_ic_internal_storage_black_24dp
                        else
                            R.drawable.file_picker_ic_sd_storage_black_24dp
                    }
                    item.isDirectory        -> R.drawable.file_picker_ic_folder_black_24dp
                    else                    -> R.drawable.file_picker_ic_file_black_24dp
                }))
        holder.img.setColorFilter(style.drawableTintColor, PorterDuff.Mode.SRC_IN)

        holder.fileName.text = item.name
        holder.fileName.setTextColor(style.textColor)
        holder.itemView.setOnClickListener {
            onItemClick(files[holder.adapterPosition])
        }
    }

    fun addAll(items: Collection<FileItem>, includeUp: Boolean = false) {
        val offset = if(includeUp) 1 else 0
        synchronized(lock) { if(includeUp) files.add(up); files.addAll(items) }
        if(notifyOnChange) notifyItemRangeInserted(offset, items.size + offset)
    }

    fun newFolder(name: String) {
        val item = StandartFileItem(name, isDirectory = true)
        val idx = (1 until files.size)
                .firstOrNull { files[it].name > name }
                ?.let { it }
                ?: files.size - 1
        synchronized(lock) { files.add(idx, item) }
        if(notifyOnChange) notifyItemInserted(idx)
    }

    fun clear() {
        val size = files.size
        synchronized(lock) { files.clear() }
        if(notifyOnChange) notifyItemRangeRemoved(0, size)
    }

    override fun getItemViewType(position: Int) = when(files[position]) {
        is StorageFileItem -> R.layout.file_picker_list_item_storage
        else -> R.layout.file_picker_list_item_file
    }

    override fun getItemId(position: Int) = position.toLong()
    override fun getItemCount() = files.size
}