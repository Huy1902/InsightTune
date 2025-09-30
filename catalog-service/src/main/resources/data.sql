
-- Parents first
INSERT INTO artists (id, name) VALUES
                                   ('11111111-1111-1111-1111-111111111111', 'Taylor Swift'),
                                   ('22222222-2222-2222-2222-222222222222', 'Coldplay');


INSERT INTO albums (id, title) VALUES
                                   ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Fearless'),
                                   ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Parachutes');

-- Tracks (note snake_case columns)
INSERT INTO tracks (id, title, album_id, storage_key, duration_ms, cover_image_key)
VALUES
    ('33333333-3333-3333-3333-333333333333',
     'Love Story',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'tracks/5927151815778178627.mp3',
     236000,
     'covers/5927151815778178627.jpeg'),

    ('44444444-4444-4444-4444-444444444444',
     'Yellow',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     'tracks/trk_yellow/original/yellow.mp3',
     267000,
     NULL);

-- Many-to-many join
INSERT INTO track_artists (track_id, artist_id) VALUES
                                                    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111'),
                                                    ('44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222');
