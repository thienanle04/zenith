package com.zenith.domain.portfolio

internal fun <T> order(
    items: List<T>,
    saved: List<String>,
    id: (T) -> String,
    created: (T) -> Int,
): List<T> {
    val rank = saved.withIndex().associate { it.value to it.index }
    return items.sortedWith { leftItem, rightItem ->
        val left = rank[id(leftItem)]
        val right = rank[id(rightItem)]
        when {
            left == null && right == null -> created(leftItem).compareTo(created(rightItem))
            left == null -> 1
            right == null -> -1
            else -> left.compareTo(right)
        }
    }
}

internal fun moveSection(
    book: Portfolio,
    key: String,
    delta: Int,
): Portfolio {
    val order = mergedSectionOrder(book, presentSectionKeys(book)).toMutableList()
    val index = order.indexOf(key)
    val target = index + delta
    if (index < 0 || target < 0 || target >= order.size) {
        return book
    }
    val moved = order.removeAt(index)
    order.add(target, moved)
    return book.copy(sectionOrder = order)
}

internal fun moveRow(
    book: Portfolio,
    sectionKey: String,
    rowId: String,
    delta: Int,
): Portfolio {
    val order = rowIds(book, sectionKey).toMutableList()
    val index = order.indexOf(rowId)
    val target = index + delta
    if (index < 0 || target < 0 || target >= order.size) {
        return book
    }
    val moved = order.removeAt(index)
    order.add(target, moved)
    return book.copy(rowOrder = book.rowOrder + (sectionKey to order))
}

private fun rowIds(
    book: Portfolio,
    key: String,
): List<String> = section(book, key).rows.map { it.id }
