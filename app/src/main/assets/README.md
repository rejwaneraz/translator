# Sample media assets

Seed videos are referenced as `asset:videos/video_001.mp4` … `video_024.mp4` and
thumbnails as `asset:images/thumb_001.jpg` … `thumb_024.jpg`; avatars as
`asset:images/avatar_<username>.jpg`.

The binaries are NOT committed (repo stays small and free of unlicensed media).
Drop your own files with these exact names into:

- `app/src/main/assets/videos/`
- `app/src/main/assets/images/`

Missing files are handled gracefully: the feed shows a "Video unavailable"
placeholder with the thumbnail, and avatars fall back to initials.
