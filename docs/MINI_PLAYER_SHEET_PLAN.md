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
- Peek state shows mini player header (`FloatingPlayer`).
- Full player content (`FullPlayerSheetContent`) remains in the sheet, but returns early when not expanded.

## Performance Tuning
- Reduce blur strength when expanded (`12f`) and keep even lower in peek (`4f`).
- Throttle progress updates to 750ms while expanded.
- Hide full player content when not expanded to avoid recomposition overhead.

## Validation Checklist
- Tap mini player expands sheet.
- Drag down collapses to peek.
- Back collapses from expanded to peek.
- Bottom navigation is visible only in peek.
- No visual jumps when expanding/collapsing.

## Next Steps (Optional)
- Add Compose UI tests to validate expand/collapse and back behavior.
- Add animation polish for sheet content (e.g., fading in body controls).