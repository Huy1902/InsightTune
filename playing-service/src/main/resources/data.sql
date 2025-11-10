DELETE FROM user_state;

INSERT INTO user_state (id, email, track_id, position_ms) VALUES
                                                              (RANDOM_UUID(), 'alice@example.com',  'track-001', 45000),
                                                              (RANDOM_UUID(), 'bob@example.com',    'track-002', 120000),
                                                              (RANDOM_UUID(), 'charlie@example.com','track-003', 90000);
