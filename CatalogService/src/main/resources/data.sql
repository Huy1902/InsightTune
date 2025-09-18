CREATE TABLE track (
                       id              UUID PRIMARY KEY,
                       title           VARCHAR(255) NOT NULL,
                       artist          VARCHAR(255) NOT NULL,
                       album           VARCHAR(255),
                       storage_key     VARCHAR(1000) NOT NULL,
                       duration_ms     INT NOT NULL,
                       cover_image_key VARCHAR(1000)
);

INSERT INTO track (id, title, artist, album, storage_key, duration_ms, cover_image_key)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Love Story', 'Taylor Swift', 'Fearless',
     's3://bucket/tracks/1.mp3', 235000, 's3://bucket/covers/1.jpg'),

    ('22222222-2222-2222-2222-222222222222', 'Yellow', 'Coldplay', 'Parachutes',
     's3://bucket/tracks/2.mp3', 267000, 's3://bucket/covers/2.jpg'),

    ('33333333-3333-3333-3333-333333333333', 'Shape of You', 'Ed Sheeran', 'Divide',
     's3://bucket/tracks/3.mp3', 263000, 's3://bucket/covers/3.jpg');
