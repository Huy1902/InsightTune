BEGIN;

-- ===== Schema (id = UUID strings you supply) =====
CREATE TABLE IF NOT EXISTS albums (
                                      id UUID PRIMARY KEY,
                                      title TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS artists (
                                       id UUID PRIMARY KEY,
                                       name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tracks (
                                      id UUID PRIMARY KEY,
                                      title TEXT NOT NULL,
                                      album_id UUID NOT NULL REFERENCES albums(id) ON DELETE CASCADE,
                                      storage_key TEXT NOT NULL UNIQUE,
                                      duration_ms INTEGER NOT NULL,
                                      cover_image_key TEXT
);

CREATE TABLE IF NOT EXISTS track_artists (
                                             track_id UUID NOT NULL REFERENCES tracks(id) ON DELETE CASCADE,
                                             artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
                                             PRIMARY KEY (track_id, artist_id)
);

-- ===== Albums =====
INSERT INTO albums (id, title) VALUES
                                   ('40c0255e-44e6-eff0-ce94-4c3aa3ad21ca','Around the World in a Day'),
                                   ('8d1b96e1-5b2e-7ed8-3ff3-530eb72b24a2','Back In Black'),
                                   ('2ea78fb8-2481-cc1c-cb3b-ad2bca93c26b','Big Willie Style'),
                                   ('969ff60d-f5d2-42ed-4811-0397aa5dbc89','Californication (Deluxe Edition)'),
                                   ('a5bf5e85-41ac-4682-05e6-13278286e998','Departure'),
                                   ('6bb3fae2-896a-1c11-4c3f-a8303b634e63','Electric Ladyland'),
                                   ('e6e06d32-988b-7403-e1db-ed90f29d13fc','Heartbeat City'),
                                   ('2d35a868-abb3-9366-25bb-d9be6ee262ca','Hi Infidelity (30th Anniversary Edition)'),
                                   ('65660b2e-b01b-3c28-0120-db3c082ea7f4','Jeremy'),
                                   ('1ebd4b55-1411-7658-5614-f03b2d25cb04','Metal Health'),
                                   ('c7e73ae9-44e3-041f-9117-ea4360b0074d','Piano Man'),
                                   ('3fba666d-c421-61bb-bf7f-4d90e60f0878','Planet Waves'),
                                   ('96fde1f9-c39a-201a-0e8f-1a1b48eb5d06','Raising Hell'),
                                   ('6812fdd5-db32-70b9-4062-e8b5a428419b','RUN-DMC (Expanded Edition)'),
                                   ('f8d10b22-463f-47d1-9d4b-783b31f10f7e','Speakerboxxx/The Love Below'),
                                   ('bdffa94f-2c98-b54c-9a61-1f3168258928','The Cars'),
                                   ('049ece8d-7f6d-3e77-58de-4074e8893ba7','The Real Deal: Greatest Hits Volume 2'),
                                   ('f20b4d7b-3d5a-4a6a-b8b5-336a6f09d72f','Touch (Reissue - Deluxe Edition)'),
                                   ('b1e2f54f-10df-a8e9-6ed8-2ac0f486a9da','Totally Krossed Out'),
                                   ('be068cce-3c96-8d28-9cb4-7f2fa360b90d','Wheels Are Turnin'''),
                                   ('55d94d7b-50c6-6a3d-77b6-9d4b6d92d38b','Willennium'),
                                   ('e55759cb-3b31-7b49-4106-7a0a3b5d4028','You Can Tune a Piano, But You Can''t Tuna Fish')
ON CONFLICT DO NOTHING;

-- ===== Artists =====
INSERT INTO artists (id, name) VALUES
                                   ('53c3ef68-ccff-1219-6af4-1984bef7679d','AC/DC'),
                                   ('05a4f117-136a-6d6f-9aad-37e1f5e3aa5b','Aerosmith'),
                                   ('23084c6e-0d9a-6d9a-69f2-2a3a4903f29a','Annie Lennox'),
                                   ('10b1f0fd-e527-8dee-3c27-625c46edf477','Billy Joel'),
                                   ('4a156ef6-03dd-1538-93eb-b63e68941be6','Bob Dylan'),
                                   ('6c4b42a7-465e-b8c5-d8e1-8e8cfe647c7e','Dave Stewart'),
                                   ('3328637c-1bcf-83f1-d4ab-81252eb7ee9c','Dru Hill'),
                                   ('8e9a3a32-a0b7-0247-df74-08d967f1c3b1','Journey'),
                                   ('7d57c945-9b58-0e46-7a6a-9b4bb44a7b7f','Jimi Hendrix'),
                                   ('b3f93f04-7d57-4338-3d28-a12fd8e84406','Kool Moe Dee'),
                                   ('20879817-0393-7f1f-1ddc-169274bcd87e','Kris Kross'),
                                   ('d52ebd8b-f0ae-e7f0-a482-e84a091e6c3f','OutKast'),
                                   ('6b15c0bc-d283-da60-580f-d45e76be5e24','Pearl Jam'),
                                   ('7b781758-e5d0-421f-80d7-1e2e3d6d079f','Prince'),
                                   ('31334537-6e25-a53d-21d2-aae16996c6da','Red Hot Chili Peppers'),
                                   ('82d71daf-c5cf-d87c-38ec-091559c05428','REO Speedwagon'),
                                   ('bfa9c31f-6e1a-0a3f-89a0-4a6c57f4fdf0','Run–D.M.C.'),
                                   ('6d591c62-c230-4cb0-9e8d-8d5aac59738e','Stevie Ray Vaughan'),
                                   ('1ec1a1d7-b781-758e-5d0d-d1c8e30bbf78','The Cars'),
                                   ('9bd74ab7-3f91-711c-d814-0eb3fa759374','Will Smith'),
                                   ('a5f3df33-71d3-4812-9c73-60b30a2e1b77','Eurythmics'),
                                   ('32f721a3-f5fa-62a0-1e8a-921e14cfb22f','Quiet Riot')
ON CONFLICT DO NOTHING;

-- ===== Tracks (duration_ms filled; cover_image_key from storage_key) =====
INSERT INTO tracks (id, title, album_id, storage_key, duration_ms, cover_image_key) VALUES
                                                                                        ('65130cad-97e6-ed1b-37d0-1c986b9d23c7','Piano Man','c7e73ae9-44e3-041f-9117-ea4360b0074d','tracks/78WVLOP9pN0G3gRLFy1rAa.mp3',339000,'covers/78WVLOP9pN0G3gRLFy1rAa.jpeg'),
                                                                                        ('c1a2d905-58a7-c324-b67c-de9eaaf27dfc','Here Comes the Rain Again - Remastered Version','f20b4d7b-3d5a-4a6a-b8b5-336a6f09d72f','tracks/78RIER8V6EhrqVPOBi2GYa.mp3',293000,'covers/78RIER8V6EhrqVPOBi2GYa.jpeg'),
                                                                                        ('7d402a6a-8fa7-a230-1a2b-281519c4a77d','Roll with the Changes','e55759cb-3b31-7b49-4106-7a0a3b5d4028','tracks/16x9viSmRS3PII71Pdeowc.mp3',335000,'covers/16x9viSmRS3PII71Pdeowc.jpeg'),
                                                                                        ('f82e7d88-8c82-5859-6ea8-05ad3505b2bf','You Might Think','e6e06d32-988b-7403-e1db-ed90f29d13fc','tracks/7BvuV4c1BGhaRapcXvRu5z.mp3',184000,'covers/7BvuV4c1BGhaRapcXvRu5z.jpeg'),
                                                                                        ('6d2d578b-e588-8699-2731-420d3cf68639','My Best Friend''s Girl','bdffa94f-2c98-b54c-9a61-1f3168258928','tracks/1SRkKyJ2JjMZgyDWC30zKv.mp3',224000,'covers/1SRkKyJ2JjMZgyDWC30zKv.jpeg'),
                                                                                        ('c2f46d16-0d7f-4d3c-0fa0-2ce9a1b2b686','Just What I Needed','bdffa94f-2c98-b54c-9a61-1f3168258928','tracks/4alHo6RGd0D3OUbTPExTHN.mp3',224000,'covers/4alHo6RGd0D3OUbTPExTHN.jpeg'),
                                                                                        ('a0809f4c-11b2-9b17-9954-9d5b37210a3a','Voodoo Child (Slight Return)','049ece8d-7f6d-3e77-58de-4074e8893ba7','tracks/1BhMUHjiQlwGlT5ZaMHHSf.mp3',478000,'covers/1BhMUHjiQlwGlT5ZaMHHSf.jpeg'),
                                                                                        ('3f4bf0df-d88a-5247-1d52-ec332514b0f8','Hey Ya! - Radio Mix','f8d10b22-463f-47d1-9d4b-783b31f10f7e','tracks/2PpruBYCo4H7WOBJ7Q2EwM.mp3',235000,'covers/2PpruBYCo4H7WOBJ7Q2EwM.jpeg'),
                                                                                        ('99f5be58-7e81-986c-d38e-dc4bdbdc8669','Can''t Fight This Feeling','be068cce-3c96-8d28-9cb4-7f2fa360b90d','tracks/5WwqdeavrQrbeAMDxGawse.mp3',294000,'covers/5WwqdeavrQrbeAMDxGawse.jpeg'),
                                                                                        ('eb8f9d6b-1dc4-780f-88ef-72704e9fa558','Time for Me to Fly - As heard in the Netflix series Ozark','e55759cb-3b31-7b49-4106-7a0a3b5d4028','tracks/0cO7JEo8deKuQMWpDyjenY.mp3',224000,'covers/0cO7JEo8deKuQMWpDyjenY.jpeg'),
                                                                                        ('a9a1e6cd-5e3f-3fd0-8e48-7556ac49fbb6','Gettin'' Jiggy Wit It','2ea78fb8-2481-cc1c-cb3b-ad2bca93c26b','tracks/0weAUscowxeqDtpCgtbpgp.mp3',227000,'covers/0weAUscowxeqDtpCgtbpgp.jpeg'),
                                                                                        ('d25b53c9-a6ba-9c13-895c-8b79c16fca2a','Forever Young - Fast Version','3fba666d-c421-61bb-bf7f-4d90e60f0878','tracks/1C4yOUEjCUyPkxRDwwFksG.mp3',168000,'covers/1C4yOUEjCUyPkxRDwwFksG.jpeg'),
                                                                                        ('cfd5508c-130a-ba23-21b2-232426b737c8','Metal Health (Bang Your Head)','1ebd4b55-1411-7658-5614-f03b2d25cb04','tracks/28clONjZmul6FjfO6tZQDE.mp3',316000,'covers/28clONjZmul6FjfO6tZQDE.jpeg'),
                                                                                        ('7b2d28ec-b2d7-d473-db38-6c773b57b50e','Scar Tissue','969ff60d-f5d2-42ed-4811-0397aa5dbc89','tracks/1G391cbiT3v3Cywg8T7DM1.mp3',217000,'covers/1G391cbiT3v3Cywg8T7DM1.jpeg'),
                                                                                        ('01715774-ffa0-ce75-b219-65c9005e8844','Jump','b1e2f54f-10df-a8e9-6ed8-2ac0f486a9da','tracks/27AHAtAirQapVldIm4c9ZX.mp3',196000,'covers/27AHAtAirQapVldIm4c9ZX.jpeg'),
                                                                                        ('eba31b00-0618-edf9-c11d-8172c2cf2433','Wild Wild West (feat. Dru Hill & Kool Mo Dee) - Album Version With Intro','55d94d7b-50c6-6a3d-77b6-9d4b6d92d38b','tracks/5AMvBCtX2rspUdoeJ9IsPN.mp3',250000,'covers/5AMvBCtX2rspUdoeJ9IsPN.jpeg'),
                                                                                        ('6f6745a3-46c2-d648-77c3-3e3bb9ccb3a7','Raspberry Beret','40c0255e-44e6-eff0-ce94-4c3aa3ad21ca','tracks/5jSz894ljfWE0IcHBSM39i.mp3',215000,'covers/5jSz894ljfWE0IcHBSM39i.jpeg'),
                                                                                        ('4a517c1e-b34a-1562-5a70-e7dcde506947','Take It On the Run','2d35a868-abb3-9366-25bb-d9be6ee262ca','tracks/5gys5nzVQIYhgHIfiOJYva.mp3',240000,'covers/5gys5nzVQIYhgHIfiOJYva.jpeg'),
                                                                                        ('2d7aba3f-7fa8-7bc9-b0c2-2cb7c93d4c97','Yellow Ledbetter','65660b2e-b01b-3c28-0120-db3c082ea7f4','tracks/3bE5slaVEfaDreqARl6k4M.mp3',300000,'covers/3bE5slaVEfaDreqARl6k4M.jpeg'),
                                                                                        ('9f322b4c-3d4b-bb2a-782e-85aa9bd8aa50','It''s Like That','6812fdd5-db32-70b9-4062-e8b5a428419b','tracks/2J6QnTjHIWwXErNWyF0RUC.mp3',249000,'covers/2J6QnTjHIWwXErNWyF0RUC.jpeg'),
                                                                                        ('9bdc7c9d-9577-a101-e3c9-5be9d7b2f6f0','It''s Tricky','96fde1f9-c39a-201a-0e8f-1a1b48eb5d06','tracks/6jBCehpNMkwFVF3dz4nLIW.mp3',183000,'covers/6jBCehpNMkwFVF3dz4nLIW.jpeg'),
                                                                                        ('0fbcde23-f03b-ccb4-7c6b-3a3e1cbfd08a','Walk This Way (feat. Aerosmith)','96fde1f9-c39a-201a-0e8f-1a1b48eb5d06','tracks/6qUEOWqOzu1rLPUPQ1ECpx.mp3',311000,'covers/6qUEOWqOzu1rLPUPQ1ECpx.jpeg'),
                                                                                        ('9bd74ab7-3f91-711c-d814-0eb3fa759374','Any Way You Want It','a5bf5e85-41ac-4682-05e6-13278286e998','tracks/71SvEDmsOwIWw1IozsZoMA.mp3',203000,'covers/71SvEDmsOwIWw1IozsZoMA.jpeg'),
                                                                                        ('32f721a3-f5fa-62a0-1e8a-921e14cfb22f','All Along the Watchtower','6bb3fae2-896a-1c11-4c3f-a8303b634e63','tracks/2aoo2jlRnM3A0NyLQqMN2f.mp3',241000,'covers/2aoo2jlRnM3A0NyLQqMN2f.jpeg'),
                                                                                        ('ce8c211e-c1b1-e811-d394-fc64d8404d61','Back In Black','8d1b96e1-5b2e-7ed8-3ff3-530eb72b24a2','tracks/08mG3Y1vljYA6bvDt4Wqkj.mp3',255000,'covers/08mG3Y1vljYA6bvDt4Wqkj.jpeg')
ON CONFLICT DO NOTHING;

-- ===== Track ↔ Artists =====
INSERT INTO track_artists (track_id, artist_id) VALUES
                                                    ('65130cad-97e6-ed1b-37d0-1c986b9d23c7','10b1f0fd-e527-8dee-3c27-625c46edf477'),
                                                    ('c1a2d905-58a7-c324-b67c-de9eaaf27dfc','a5f3df33-71d3-4812-9c73-60b30a2e1b77'),
                                                    ('c1a2d905-58a7-c324-b67c-de9eaaf27dfc','23084c6e-0d9a-6d9a-69f2-2a3a4903f29a'),
                                                    ('c1a2d905-58a7-c324-b67c-de9eaaf27dfc','6c4b42a7-465e-b8c5-d8e1-8e8cfe647c7e'),
                                                    ('7d402a6a-8fa7-a230-1a2b-281519c4a77d','82d71daf-c5cf-d87c-38ec-091559c05428'),
                                                    ('f82e7d88-8c82-5859-6ea8-05ad3505b2bf','1ec1a1d7-b781-758e-5d0d-d1c8e30bbf78'),
                                                    ('6d2d578b-e588-8699-2731-420d3cf68639','1ec1a1d7-b781-758e-5d0d-d1c8e30bbf78'),
                                                    ('c2f46d16-0d7f-4d3c-0fa0-2ce9a1b2b686','1ec1a1d7-b781-758e-5d0d-d1c8e30bbf78'),
                                                    ('a0809f4c-11b2-9b17-9954-9d5b37210a3a','6d591c62-c230-4cb0-9e8d-8d5aac59738e'),
                                                    ('3f4bf0df-d88a-5247-1d52-ec332514b0f8','d52ebd8b-f0ae-e7f0-a482-e84a091e6c3f'),
                                                    ('99f5be58-7e81-986c-d38e-dc4bdbdc8669','82d71daf-c5cf-d87c-38ec-091559c05428'),
                                                    ('eb8f9d6b-1dc4-780f-88ef-72704e9fa558','82d71daf-c5cf-d87c-38ec-091559c05428'),
                                                    ('a9a1e6cd-5e3f-3fd0-8e48-7556ac49fbb6','9bd74ab7-3f91-711c-d814-0eb3fa759374'),
                                                    ('d25b53c9-a6ba-9c13-895c-8b79c16fca2a','4a156ef6-03dd-1538-93eb-b63e68941be6'),
                                                    ('cfd5508c-130a-ba23-21b2-232426b737c8','32f721a3-f5fa-62a0-1e8a-921e14cfb22f'),
                                                    ('7b2d28ec-b2d7-d473-db38-6c773b57b50e','31334537-6e25-a53d-21d2-aae16996c6da'),
                                                    ('01715774-ffa0-ce75-b219-65c9005e8844','20879817-0393-7f1f-1ddc-169274bcd87e'),
                                                    ('eba31b00-0618-edf9-c11d-8172c2cf2433','9bd74ab7-3f91-711c-d814-0eb3fa759374'),
                                                    ('eba31b00-0618-edf9-c11d-8172c2cf2433','3328637c-1bcf-83f1-d4ab-81252eb7ee9c'),
                                                    ('eba31b00-0618-edf9-c11d-8172c2cf2433','b3f93f04-7d57-4338-3d28-a12fd8e84406'),
                                                    ('6f6745a3-46c2-d648-77c3-3e3bb9ccb3a7','7b781758-e5d0-421f-80d7-1e2e3d6d079f'),
                                                    ('4a517c1e-b34a-1562-5a70-e7dcde506947','82d71daf-c5cf-d87c-38ec-091559c05428'),
                                                    ('2d7aba3f-7fa8-7bc9-b0c2-2cb7c93d4c97','6b15c0bc-d283-da60-580f-d45e76be5e24'),
                                                    ('9f322b4c-3d4b-bb2a-782e-85aa9bd8aa50','bfa9c31f-6e1a-0a3f-89a0-4a6c57f4fdf0'),
                                                    ('9bdc7c9d-9577-a101-e3c9-5be9d7b2f6f0','bfa9c31f-6e1a-0a3f-89a0-4a6c57f4fdf0'),
                                                    ('0fbcde23-f03b-ccb4-7c6b-3a3e1cbfd08a','bfa9c31f-6e1a-0a3f-89a0-4a6c57f4fdf0'),
                                                    ('0fbcde23-f03b-ccb4-7c6b-3a3e1cbfd08a','05a4f117-136a-6d6f-9aad-37e1f5e3aa5b'),
                                                    ('9bd74ab7-3f91-711c-d814-0eb3fa759374','8e9a3a32-a0b7-0247-df74-08d967f1c3b1'),
                                                    ('32f721a3-f5fa-62a0-1e8a-921e14cfb22f','7d57c945-9b58-0e46-7a6a-9b4bb44a7b7f'),
                                                    ('ce8c211e-c1b1-e811-d394-fc64d8404d61','53c3ef68-ccff-1219-6af4-1984bef7679d')
ON CONFLICT DO NOTHING;

COMMIT;