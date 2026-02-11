INSERT INTO users (username, email, password_hash, display_name, bio, created_at, updated_at)
VALUES ('jdoe', 'jdoe@example.com', '', 'John Doe', null, NOW(), NOW())
ON CONFLICT (username) DO NOTHING;
