package com.example.data

import com.example.model.EmergencyRequest
import kotlin.math.floor
import kotlin.math.ln

/**
 * Hand-written Binary Max-Heap with O(1) hash map index lookup.
 *
 * Array Indexing Rules (0-based):
 *   Parent(i)      = (i - 1) / 2
 *   LeftChild(i)   = 2 * i + 1
 *   RightChild(i)  = 2 * i + 2
 *
 * Priority Rules:
 *   1. Severity: Higher level takes precedence (4 Critical > 3 High > 2 Moderate > 1 Low).
 *   2. Tie-Breaker: Lower arrival sequence takes precedence (FIFO among equal severities).
 */
class EmergencyMaxHeap {
    private val heap = ArrayList<EmergencyRequest>()
    private val idToIndex = HashMap<String, Int>()

    var lastComparisons: Long = 0
        private set
    var lastSwaps: Long = 0
        private set
    var lastOperationName: String = "Initialized"
        private set
    val recordedSwaps = ArrayList<Pair<Int, Int>>()

    private fun hasHigherPriority(a: EmergencyRequest, b: EmergencyRequest): Boolean {
        lastComparisons++
        if (a.severity != b.severity) {
            return a.severity > b.severity
        }
        return a.sequenceNumber < b.sequenceNumber
    }

    private fun swapNodes(i: Int, j: Int) {
        lastSwaps++
        recordedSwaps.add(Pair(i, j))
        val temp = heap[i]
        heap[i] = heap[j]
        heap[j] = temp

        idToIndex[heap[i].id] = i
        idToIndex[heap[j].id] = j
    }

    private fun siftUp(startIndex: Int) {
        var i = startIndex
        while (i > 0) {
            val parent = (i - 1) / 2
            if (hasHigherPriority(heap[i], heap[parent])) {
                swapNodes(i, parent)
                i = parent
            } else {
                break
            }
        }
    }

    private fun siftDown(startIndex: Int) {
        var i = startIndex
        val n = heap.size
        while (true) {
            val left = 2 * i + 1
            val right = 2 * i + 2
            var largest = i

            if (left < n && hasHigherPriority(heap[left], heap[largest])) {
                largest = left
            }
            if (right < n && hasHigherPriority(heap[right], heap[largest])) {
                largest = right
            }

            if (largest != i) {
                swapNodes(i, largest)
                i = largest
            } else {
                break
            }
        }
    }

    fun resetMetrics(opName: String) {
        lastComparisons = 0
        lastSwaps = 0
        lastOperationName = opName
        recordedSwaps.clear()
    }

    val size: Int get() = heap.size
    val isEmpty: Boolean get() = heap.isEmpty()

    fun clear() {
        heap.clear()
        idToIndex.clear()
        resetMetrics("Clear Heap")
    }

    fun peek(): EmergencyRequest? {
        resetMetrics("Peek")
        return if (heap.isEmpty()) null else heap[0]
    }

    fun insert(req: EmergencyRequest) {
        resetMetrics("Insert (${req.id})")
        heap.add(req)
        val idx = heap.size - 1
        idToIndex[req.id] = idx
        siftUp(idx)
    }

    fun extractMax(): EmergencyRequest? {
        resetMetrics("Extract-Max / Dispatch")
        if (heap.isEmpty()) return null

        val top = heap[0]
        idToIndex.remove(top.id)

        if (heap.size == 1) {
            heap.removeAt(0)
            return top
        }

        val last = heap.removeAt(heap.size - 1)
        heap[0] = last
        idToIndex[last.id] = 0
        siftDown(0)

        return top
    }

    fun changeSeverity(id: String, newSeverity: Int): Boolean {
        resetMetrics("Update Severity ($id)")
        val idx = idToIndex[id] ?: return false
        val oldSeverity = heap[idx].severity
        if (oldSeverity == newSeverity) return true

        heap[idx] = heap[idx].copy(severity = newSeverity)

        if (newSeverity > oldSeverity) {
            siftUp(idx)
        } else {
            siftDown(idx)
        }
        return true
    }

    fun updateRequest(updated: EmergencyRequest): Boolean {
        resetMetrics("Update (${updated.id})")
        val idx = idToIndex[updated.id] ?: return false
        val oldSeverity = heap[idx].severity
        heap[idx] = updated
        if (updated.severity > oldSeverity) {
            siftUp(idx)
        } else if (updated.severity < oldSeverity) {
            siftDown(idx)
        }
        return true
    }

    fun removeById(id: String): EmergencyRequest? {
        resetMetrics("Cancel/Remove ($id)")
        val idx = idToIndex[id] ?: return null
        val removed = heap[idx]
        idToIndex.remove(id)

        val lastIdx = heap.size - 1
        if (idx == lastIdx) {
            heap.removeAt(lastIdx)
            return removed
        }

        val moved = heap.removeAt(lastIdx)
        heap[idx] = moved
        idToIndex[moved.id] = idx

        val parent = if (idx > 0) (idx - 1) / 2 else 0
        if (idx > 0 && hasHigherPriority(heap[idx], heap[parent])) {
            siftUp(idx)
        } else {
            siftDown(idx)
        }
        return removed
    }

    fun findDuplicate(phoneNumber: String, serviceType: String, location: String): EmergencyRequest? {
        val cleanPhone = phoneNumber.trim()
        val cleanLoc = location.trim().lowercase()
        return heap.firstOrNull {
            it.phoneNumber.trim() == cleanPhone &&
            (it.serviceType.equals(serviceType.trim(), ignoreCase = true) ||
             it.location.trim().lowercase().contains(cleanLoc) ||
             cleanLoc.contains(it.location.trim().lowercase()))
        }
    }

    fun buildHeap(items: List<EmergencyRequest>) {
        resetMetrics("Build-Heap (Bottom-Up O(n))")
        heap.clear()
        heap.addAll(items)
        idToIndex.clear()
        for (i in heap.indices) {
            idToIndex[heap[i].id] = i
        }

        if (heap.isEmpty()) return

        val n = heap.size
        for (i in (n / 2) - 1 downTo 0) {
            siftDown(i)
        }
    }

    fun getSortedRequests(): List<EmergencyRequest> {
        val copy = ArrayList(heap)
        val tempHeap = EmergencyMaxHeap()
        tempHeap.buildHeap(copy)

        val result = ArrayList<EmergencyRequest>(copy.size)
        while (!tempHeap.isEmpty) {
            tempHeap.extractMax()?.let { result.add(it) }
        }
        return result
    }

    fun getRawHeap(): List<EmergencyRequest> = heap.toList()

    fun getTreeHeight(): Int {
        if (heap.isEmpty()) return 0
        return (floor(ln(heap.size.toDouble()) / ln(2.0)) + 1).toInt()
    }

    /**
     * O(n) Heap Invariant Verification:
     * Checks if for every node i, children 2i+1 and 2i+2 satisfy Max-Heap property.
     */
    fun validateHeapProperty(): Pair<Boolean, String> {
        if (heap.isEmpty()) return Pair(true, "Heap is empty. Invariant holds trivially.")
        val n = heap.size
        for (i in 0 until n / 2) {
            val left = 2 * i + 1
            val right = 2 * i + 2
            if (left < n && hasHigherPriority(heap[left], heap[i])) {
                return Pair(false, "Heap Invariant Violation: Left child [$left] (${heap[left].id}) has higher priority than parent [$i] (${heap[i].id})")
            }
            if (right < n && hasHigherPriority(heap[right], heap[i])) {
                return Pair(false, "Heap Invariant Violation: Right child [$right] (${heap[right].id}) has higher priority than parent [$i] (${heap[right].id})")
            }
        }
        return Pair(true, "Max-Heap Invariant perfectly satisfied across all $n nodes (Parent >= Children with FIFO tie-breaker).")
    }
}
