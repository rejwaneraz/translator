package com.rejwane.reelslocal.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaPathsTest {

    @Test
    fun `isAsset detects the asset prefix`() {
        assertTrue(MediaPaths.isAsset("asset:videos/video_001.mp4"))
        assertFalse(MediaPaths.isAsset("/storage/emulated/0/vid.mp4"))
        assertFalse(MediaPaths.isAsset(""))
    }

    @Test
    fun `assetRelativePath strips the prefix`() {
        assertEquals(
            "videos/video_001.mp4",
            MediaPaths.assetRelativePath("asset:videos/video_001.mp4")
        )
    }

    @Test
    fun `imageModel maps assets to android_asset urls`() {
        assertEquals(
            "file:///android_asset/videos/poster.jpg",
            MediaPaths.imageModel("asset:videos/poster.jpg")
        )
    }

    @Test
    fun `imageModel is null for blank or missing files`() {
        assertNull(MediaPaths.imageModel(null))
        assertNull(MediaPaths.imageModel(""))
        assertNull(MediaPaths.imageModel("/definitely/not/a/real/file.jpg"))
    }
}
