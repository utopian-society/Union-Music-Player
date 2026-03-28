# Mini Player Sheet Plan

## Goal
Replace the current morphing overlay with a persistent bottom sheet (peek mini-player + expand to full player) for smoother interaction and less jank.

## Architecture
- Use `BottomSheetScaffold` with `rememberStandardBottomSheetState`.
- Two states: `PartiallyExpanded` (peek) and `Expanded` (full player).
- Bottom navigation is visible only in peek state; it hides when expanded.
- Back press collapses the sheet from expanded to peek.

## Implementation Summary
- Host sheet in `UnionMusicApp`.
- Peek state shows mini player via new `PlayerScreen` + `MiniPlayer`.
- Full player content is provided by `PlayerScreen` + `FullScreenPlayer` only when expanded.
- Sheet container is transparent; glass surfaces are drawn within player content.

## Apple Music-like UI Enhancements
- True blur for glass surfaces using Haze (Mini/Full surfaces).
- Mini player shows album art thumbnail + subtle progress bar.
- Full player uses a 2-page horizontal pager: album art page and lyrics page.

## Lyrics Behavior
- Lyrics are extracted from embedded playback metadata into `lyricsFlow`.
- If LRC timestamps exist, the current line is highlighted and auto-scrolled.
- Manual scroll pauses auto-scroll for a short cooldown.
- If no timestamps, show plain text lyrics.

## Performance Tuning
- Reduce blur strength when expanded (`12f`) and keep even lower in peek (`4f`).
- Throttle progress updates to 500ms while expanded.
- Hide full player content when not expanded to avoid recomposition overhead.

## Validation Checklist
- Tap mini player expands sheet.
- Drag down collapses to peek.
- Back collapses from expanded to peek.
- Bottom navigation is visible only in peek.
- Swipe left shows lyrics page.
- Lyrics highlight follows playback time when timestamps exist.
- No visual jumps when expanding/collapsing.

## Next Steps (Optional)
- Add Compose UI tests to validate expand/collapse and back behavior.
- Add animation polish for sheet content (e.g., fading in body controls).