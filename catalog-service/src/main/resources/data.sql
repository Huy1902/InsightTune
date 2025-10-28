
-- =====================
-- H2 data.sql seed file (no 'Yellow' track)
-- =====================

-- Parents first
INSERT INTO artists (id, name) VALUES
                                   ('11111111-1111-1111-1111-111111111111', 'Taylor Swift'),
                                   ('22222222-2222-2222-2222-222222222222', 'Coldplay'),
                                   ('66666666-6666-6666-6666-666666666666', 'Vũ Cát Tường'),
                                   ('99999999-9999-9999-9999-999999999999', 'Noo Phước Thịnh'),
                                   ('a3333333-3333-3333-3333-333333333333', 'Tony Việt'),
                                   ('a6666666-6666-6666-6666-666666666666', 'Karik')
;

INSERT INTO albums (id, title) VALUES
                                   ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Fearless'),
                                   ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Parachutes')
;

-- Tracks (note snake_case columns)  (removed: id=4444... 'Yellow')
INSERT INTO tracks (id, title, album_id, storage_key, duration_ms, cover_image_key)
VALUES
    ('33333333-3333-3333-3333-333333333333',
     'Love Story',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'tracks/5927151815778178627.mp3',
     236000,
     'covers/5927151815778178627.jpeg'),

    ('55555555-5555-5555-5555-555555555555',
     'The Fate of Ophelia',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'tracks/8452511130686959700.mp3',
     226000,
     'covers/8452511130686959700.jpeg'),

    ('77777777-7777-7777-7777-777777777777',
     'Mơ',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'tracks/-971023859160268633.mp3',
     262000,
     'covers/-971023859160268633.jpeg'),

    ('88888888-8888-8888-8888-888888888888',
     'Từng là',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'tracks/1923271498027091113.mp3',
     253000,
     'covers/1923271498027091113.jpeg'),

    ('a1111111-1111-1111-1111-111111111111',
     'Cause I Love You',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'tracks/-3631951113297635626.mp3',
     285000,
     'covers/-3631951113297635626.jpeg'),

    ('a2222222-2222-2222-2222-222222222222',
     'Người Bình Thường',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'tracks/7372504582425769857.mp3',
     285000,
     'covers/7372504582425769857.jpeg'),

    ('a4444444-4444-4444-4444-444444444444',
     'Chờ Ngày Mưa Tan',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'tracks/865072830054878051.mp3',
     212000,
     'covers/865072830054878051.jpeg'),

    ('a5555555-5555-5555-5555-555555555555',
     'NGƯỜI NHƯ ANH XỨNG ĐÁNG CÔ ĐƠN',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     'tracks/-7641205669204081844.mp3',
     317000,
     'covers/-7641205669204081844.jpeg'),

    ('a7777777-7777-7777-7777-777777777777',
     'Vết Mưa',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     'tracks/5257040073695959341.mp3',
     210000,
     'covers/5257040073695959341.jpeg')
;

-- Many-to-many join  (removed the 'Yellow' join row)
INSERT INTO track_artists (track_id, artist_id) VALUES
                                                    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111'),
                                                    ('55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111'),
                                                    ('77777777-7777-7777-7777-777777777777', '66666666-6666-6666-6666-666666666666'),
                                                    ('88888888-8888-8888-8888-888888888888', '66666666-6666-6666-6666-666666666666'),
                                                    ('88888888-8888-8888-8888-888888888888', '99999999-9999-9999-9999-999999999999'),
                                                    ('a1111111-1111-1111-1111-111111111111', '99999999-9999-9999-9999-999999999999'),
                                                    ('a2222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666666'),
                                                    ('a4444444-4444-4444-4444-444444444444', '99999999-9999-9999-9999-999999999999'),
                                                    ('a4444444-4444-4444-4444-444444444444', 'a3333333-3333-3333-3333-333333333333'),
                                                    ('a5555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666666'),
                                                    ('a5555555-5555-5555-5555-555555555555', 'a6666666-6666-6666-6666-666666666666'),
                                                    ('a7777777-7777-7777-7777-777777777777', '66666666-6666-6666-6666-666666666666')
;

-- ==============================
-- Spotify imports (17 tracks)
-- ==============================

-- Extra artists for these imports
INSERT INTO artists (id, name) VALUES
  ('b0000000-0000-0000-0000-000000000005', 'REO Speedwagon'),
  ('b0000000-0000-0000-0000-000000000006', 'The Cars'),
  ('b0000000-0000-0000-0000-000000000007', 'Stevie Ray Vaughan'),
  ('b0000000-0000-0000-0000-000000000009', 'Will Smith'),
  ('b0000000-0000-0000-0000-00000000000a', 'Bob Dylan'),
  ('b0000000-0000-0000-0000-00000000000b', 'Quiet Riot'),
  ('b0000000-0000-0000-0000-00000000000c', 'Red Hot Chili Peppers'),
  ('b0000000-0000-0000-0000-00000000000e', 'Dru Hill'),
  ('b0000000-0000-0000-0000-00000000000f', 'Kool Moe Dee'),
  ('b0000000-0000-0000-0000-000000000011', 'Pearl Jam'),
  ('b0000000-0000-0000-0000-000000000012', 'Run–D.M.C.'),
  ('b0000000-0000-0000-0000-000000000013', 'Aerosmith'),
  ('b0000000-0000-0000-0000-000000000014', 'Journey'),
  ('b0000000-0000-0000-0000-000000000015', 'Jimi Hendrix'),
  ('b0000000-0000-0000-0000-000000000016', 'AC/DC')
;

-- Album bucket for these imports
INSERT INTO albums (id, title) VALUES
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Spotify Imports')
;

-- 17 tracks using spotify id as storage_key = 'tracks/<spotify_id>.mp3'
INSERT INTO tracks (id, title, album_id, storage_key, duration_ms, cover_image_key) VALUES
  ('c0000000-0000-0000-0000-000000000001', 'My Best Friend''s Girl', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/1SRkKyJ2JjMZgyDWC30zKv.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000002', 'Voodoo Child (Slight Return)', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/1BhMUHjiQlwGlT5ZaMHHSf.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000003', 'Can''t Fight This Feeling', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/5WwqdeavrQrbeAMDxGawse.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000004', 'Time for Me to Fly - As heard in the Netflix series Ozark', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/0cO7JEo8deKuQMWpDyjenY.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000005', 'Gettin'' Jiggy Wit It', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/0weAUscowxeqDtpCgtbpgp.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000006', 'Forever Young - Fast Version', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/1C4yOUEjCUyPkxRDwwFksG.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000007', 'Metal Health (Bang Your Head)', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/28clONjZmul6FjfO6tZQDE.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000008', 'Scar Tissue', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/1G391cbiT3v3Cywg8T7DM1.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000009', 'Wild Wild West (feat. Dru Hill & Kool Mo Dee) - Album Version With Intro', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/5AMvBCtX2rspUdoeJ9IsPN.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-00000000000a', 'Take It On the Run', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/5gys5nzVQIYhgHIfiOJYva.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-00000000000b', 'Yellow Ledbetter', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/3bE5slaVEfaDreqARl6k4M.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-00000000000c', 'It''s Like That', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/2J6QnTjHIWwXErNWyF0RUC.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-00000000000d', 'It''s Tricky', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/6jBCehpNMkwFVF3dz4nLIW.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-00000000000e', 'Walk This Way (feat. Aerosmith)', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/6qUEOWqOzu1rLPUPQ1ECpx.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-00000000000f', 'Any Way You Want It', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/71SvEDmsOwIWw1IozsZoMA.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000010', 'All Along the Watchtower', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/2aoo2jlRnM3A0NyLQqMN2f.mp3', 0, NULL),
  ('c0000000-0000-0000-0000-000000000011', 'Back In Black', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
   'tracks/08mG3Y1vljYA6bvDt4Wqkj.mp3', 0, NULL)
;

-- M:N wiring for the 17 spotify tracks
INSERT INTO track_artists (track_id, artist_id) VALUES
  -- My Best Friend's Girl
  ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000006'),
  -- Voodoo Child (Slight Return)
  ('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000007'),
  -- Can't Fight This Feeling
  ('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000005'),
  -- Time for Me to Fly - Ozark
  ('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000005'),
  -- Gettin' Jiggy Wit It
  ('c0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000009'),
  -- Forever Young - Fast Version
  ('c0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-00000000000a'),
  -- Metal Health (Bang Your Head)
  ('c0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-00000000000b'),
  -- Scar Tissue
  ('c0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-00000000000c'),
  -- Wild Wild West (...)
  ('c0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000009'),
  ('c0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-00000000000e'),
  ('c0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-00000000000f'),
  -- Take It On the Run
  ('c0000000-0000-0000-0000-00000000000a', 'b0000000-0000-0000-0000-000000000005'),
  -- Yellow Ledbetter
  ('c0000000-0000-0000-0000-00000000000b', 'b0000000-0000-0000-0000-000000000011'),
  -- It's Like That
  ('c0000000-0000-0000-0000-00000000000c', 'b0000000-0000-0000-0000-000000000012'),
  -- It's Tricky
  ('c0000000-0000-0000-0000-00000000000d', 'b0000000-0000-0000-0000-000000000012'),
  -- Walk This Way (feat. Aerosmith)
  ('c0000000-0000-0000-0000-00000000000e', 'b0000000-0000-0000-0000-000000000012'),
  ('c0000000-0000-0000-0000-00000000000e', 'b0000000-0000-0000-0000-000000000013'),
  -- Any Way You Want It
  ('c0000000-0000-0000-0000-00000000000f', 'b0000000-0000-0000-0000-000000000014'),
  -- All Along the Watchtower
  ('c0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000015'),
  -- Back In Black
  ('c0000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000016')
;