/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.recents.model;

import android.util.Log;
import android.util.SparseArray;

import com.android.systemui.recents.model.Task.TaskKey;

/**
 * Base class for both strong and LRU task key cache.
 */
public abstract class TaskKeyCache<V> {

    protected static final String TAG = "TaskKeyCache";

    protected final SparseArray<TaskKey> mKeys = new SparseArray<>();

    /**
     * Gets a specific entry in the cache with the specified key, regardless of whether the cached
     * value is valid or not.
     */
    final V get(Task.TaskKey key) {
        return getCacheEntry(key.id);
    }

    /**
     * Returns the value only if the key is valid (has not been updated since the last time it was
     * in the cache)
     */
    final V getAndInvalidateIfModified(Task.TaskKey key) {
        Task.TaskKey lastKey = mKeys.get(key.id);
        if (lastKey != null) {
            if ((lastKey.stackId != key.stackId) ||
                    (lastKey.lastActiveTime != key.lastActiveTime)) {
                // The task has updated (been made active since the last time it was put into the
                // LRU cache) or the stack id for the task has changed, invalidate that cache item
                remove(key);
                return null;
            }
        }
        // Either the task does not exist in the cache, or the last active time is the same as
        // the key specified, so return what is in the cache
        return getCacheEntry(key.id);
    }

    /** Puts an entry in the cache for a specific key. */
    final void put(Task.TaskKey key, V value) {
        if (key == null || value == null) {
            Log.e(TAG, "Unexpected null key or value: " + key + ", " + value);
            return;
        }
        mKeys.put(key.id, key);
        putCacheEntry(key.id, value);
    }


    /** Removes a cache entry for a specific key. */
    final void remove(Task.TaskKey key) {
        // Remove the key after the cache value because we need it to make the callback
        removeCacheEntry(key.id);
        mKeys.remove(key.id);
    }

    /** Removes all the entries in the cache. */
    final void evictAll() {
        evictAllCache();
        mKeys.clear();
    }

    protected abstract V getCacheEntry(int id);
    protected abstract void putCacheEntry(int id, V value);
    protected abstract void removeCacheEntry(int id);
    protected abstract void evictAllCache();
}
