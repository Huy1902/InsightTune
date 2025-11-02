DROP TABLE artists CASCADE ;
DROP TABLE albums CASCADE ;
DROP TABLE tracks CASCADE ;
DROP TABLE track_artists CASCADE ;

-- artists
CREATE TABLE IF NOT EXISTS artists (
                                       id   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL UNIQUE
    );

-- albums
CREATE TABLE IF NOT EXISTS albums (
                                      id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL UNIQUE
    );

-- tracks
CREATE TABLE IF NOT EXISTS tracks (
                                      id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           TEXT NOT NULL,
    album_id        UUID NOT NULL REFERENCES albums(id) ON DELETE CASCADE,
    storage_key     TEXT NOT NULL UNIQUE,
    duration_ms     INTEGER NOT NULL DEFAULT 0,
    cover_image_key TEXT
    );

-- track ↔ artists (M:N)
CREATE TABLE IF NOT EXISTS track_artists (
                                             track_id  UUID NOT NULL REFERENCES tracks(id)  ON DELETE CASCADE,
    artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    PRIMARY KEY (track_id, artist_id)
    );
