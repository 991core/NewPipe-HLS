/*
 * SPDX-FileCopyrightText: 2026 NewPipe HLS contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.schabi.newpipe.player.playqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class PlaylistContinuationTest {
    private static StreamInfoItem item(final String id) {
        return new StreamInfoItem(0, "https://www.youtube.com/watch?v=" + id,
                id, StreamType.VIDEO_STREAM);
    }

    private static List<StreamInfoItem> items() {
        return List.of(item("first"), item("selected"), item("next"));
    }

    private static PlaylistPlayQueue remote(final String playlist, final Page nextPage) {
        return new PlaylistPlayQueue(0, "https://www.youtube.com/playlist?list=" + playlist,
                nextPage, items(), 1);
    }

    private static PlayQueue cloneQueue(final PlayQueue queue) throws Exception {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(queue);
        }
        try (ObjectInputStream input = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            return (PlayQueue) input.readObject();
        }
    }

    @Test
    public void localSelectionKeepsTheFollowingPlaylistItem() {
        final PlayQueue queue = new SinglePlayQueue(items(), 1, true);
        assertEquals("selected", queue.getItem().getTitle());
        queue.offsetIndex(1);
        assertEquals("next", queue.getItem().getTitle());
        assertEquals(3, queue.size());
        assertTrue(queue.isPlaylist());
    }

    @Test
    public void selectingARepeatedEntryKeepsItsPosition() {
        final PlayQueue queue = new SinglePlayQueue(
                List.of(item("repeated"), item("middle"), item("repeated"), item("last")),
                2, true);
        assertEquals(2, queue.getIndex());
        queue.offsetIndex(1);
        assertEquals("last", queue.getItem().getTitle());
        assertEquals(4, queue.size());
    }

    @Test
    public void remotePagesMustFinishBeforeRecommendations() {
        final PlaylistPlayQueue queue = remote("playlistA", new Page("https://example.org/page2"));
        assertFalse(queue.isComplete());
        assertTrue(Page.isValid(queue.nextPage));
        assertFalse(queue.canAutoQueue(true));
        assertFalse(queue.canAutoQueue(false));
        queue.offsetIndex(1);
        assertEquals("next", queue.getItem().getTitle());
    }

    @Test
    public void completedPlaylistsContinueWithRecommendations() {
        final PlayQueue local = new SinglePlayQueue(items(), 2, true);
        final PlayQueue remote = remote("playlistA", null);
        assertTrue(local.canAutoQueue(false));
        assertTrue(local.canAutoQueue(true));
        assertTrue(remote.isComplete());
        assertTrue(remote.canAutoQueue(false));
        assertTrue(remote.canAutoQueue(true));
    }

    @Test
    public void differentPlaylistsCannotReuseAnIdenticalFirstPage() {
        final PlayQueue first = remote("playlistA", null);
        final PlayQueue other = remote("playlistB", null);
        assertTrue(first.equalStreamsAndIndex(other));
        assertFalse(first.hasSamePlaybackContext(other));
    }

    @Test
    public void aPlaylistAndIsolatedVideoHaveDifferentContinuationPolicies() {
        final PlayQueue playlist = new SinglePlayQueue(List.of(item("last")), 0, true);
        final PlayQueue isolated = new SinglePlayQueue(item("last"));
        assertTrue(playlist.equalStreamsAndIndex(isolated));
        assertFalse(playlist.hasSamePlaybackContext(isolated));
        assertFalse(isolated.hasSamePlaybackContext(playlist));
        assertFalse(isolated.isPlaylist());
        assertTrue(playlist.canAutoQueue(false));
    }

    @Test
    public void localContextAndPositionSurviveNavigationSerialization() throws Exception {
        final PlayQueue original = new SinglePlayQueue(items(), 1, true);
        final PlayQueue restored = cloneQueue(original);
        assertTrue(original.equalStreamsAndIndex(restored));
        assertTrue(original.hasSamePlaybackContext(restored));
        assertTrue(restored.isPlaylist());
        restored.offsetIndex(1);
        assertEquals("next", restored.getItem().getTitle());
    }

    @Test
    public void remotePaginationSurvivesNavigationSerialization() throws Exception {
        final PlaylistPlayQueue original = remote("playlistA",
                new Page("https://example.org/page2"));
        final PlaylistPlayQueue restored = (PlaylistPlayQueue) cloneQueue(original);
        assertTrue(original.hasSamePlaybackContext(restored));
        assertTrue(original.equalStreamsAndIndex(restored));
        assertFalse(restored.isComplete());
        assertFalse(restored.canAutoQueue(true));
        assertEquals("https://example.org/page2", restored.nextPage.getUrl());
    }

    @Test
    public void ordinaryQueuesRetainTheirExistingContinuationPolicy() {
        final PlayQueue first = new SinglePlayQueue(items(), 1);
        final PlayQueue same = new SinglePlayQueue(items(), 1);
        assertFalse(first.isPlaylist());
        assertTrue(first.canAutoQueue(true));
        assertFalse(first.canAutoQueue(false));
        assertTrue(first.hasSamePlaybackContext(same));
        assertTrue(first.equalStreamsAndIndex(same));
        assertFalse(first.hasSamePlaybackContext(null));
    }
}
