package com.davenet.notely.helper

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

abstract class DataBoundListAdapter<T, V : ViewDataBinding>(diffCallback: DiffUtil.ItemCallback<T>) :
    ListAdapter<T, DataBoundViewHolder<V>>(
        AsyncDifferConfig.Builder<T>(diffCallback).build()
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<V> {
        val binding = createBinding(parent)
        return DataBoundViewHolder(binding)
    }

    protected abstract fun createBinding(parent: ViewGroup): V

    override fun onBindViewHolder(holder: DataBoundViewHolder<V>, position: Int) {
        bind(holder.binding, getItem(position), position)
        holder.binding.executePendingBindings()
    }

    abstract fun bind(binding: V, item: T, position: Int)
}
