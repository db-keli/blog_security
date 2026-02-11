-- Development seed script for the blog_spring database
-- Populates users, tags, posts, post_tags, comments, and reviews
-- This script is SAFE for development only: it truncates data.

BEGIN;

-- Clear existing data (respect FK constraints)
TRUNCATE TABLE
    reviews,
    comments,
    post_tags,
    posts,
    tags,
    users
RESTART IDENTITY CASCADE;

-- =====================
-- Users
-- =====================
INSERT INTO users (id, username, email, password_hash, display_name, bio, created_at, updated_at)
VALUES
    (1, 'jdoe',   'jdoe@example.com',   'dev-hash-jdoe',   'John Doe',
     'Senior Java developer who writes about Spring and backend architecture.',
     NOW(), NOW()),
    (2, 'asmith', 'asmith@example.com', 'dev-hash-asmith', 'Alice Smith',
     'Full-stack engineer who enjoys GraphQL and modern frontend stacks.',
     NOW(), NOW()),
    (3, 'bwayne', 'bwayne@example.com', 'dev-hash-bwayne', 'Bruce Wayne',
     'Security-focused architect writing about performance and hardening.',
     NOW(), NOW());

-- =====================
-- Tags
-- =====================
INSERT INTO tags (id, name, slug, description, created_at)
VALUES
    (1, 'Spring Boot', 'spring-boot',
     'Articles related to Spring Boot and the Spring ecosystem.', NOW()),
    (2, 'GraphQL', 'graphql',
     'APIs and schema design using GraphQL.', NOW()),
    (3, 'Java', 'java',
     'Core Java language and JVM topics.', NOW()),
    (4, 'PostgreSQL', 'postgresql',
     'Relational data modeling and Postgres-specific features.', NOW());

-- =====================
-- Posts
-- =====================
-- Note: status is stored as a string from the PostStatus enum (e.g., DRAFT, PUBLISHED).
INSERT INTO posts (
    id,
    author_id,
    title,
    content,
    slug,
    status,
    created_at,
    updated_at,
    published_at
)
VALUES
    (
        1,
        1, -- jdoe
        'Getting Started with Spring Boot',
        'This post walks through setting up a new Spring Boot application, ' ||
        'including dependencies, configuration, and a simple REST endpoint.',
        'getting-started-with-spring-boot',
        'PUBLISHED',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        2,
        2, -- asmith
        'Building a GraphQL API with Spring',
        'In this article we design a schema-first GraphQL API and implement it ' ||
        'using Spring GraphQL, discussing resolvers, DTOs, and error handling.',
        'building-a-graphql-api-with-spring',
        'PUBLISHED',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        3,
        1, -- jdoe
        'PostgreSQL Tips for Spring Data JPA',
        'Draft notes on tuning PostgreSQL for Spring Data JPA, including indexes, ' ||
        'connection pooling, and query analysis.',
        'postgresql-tips-spring-data-jpa',
        'DRAFT',
        NOW(),
        NOW(),
        NULL
    );

-- =====================
-- Post <-> Tag relations
-- =====================
INSERT INTO post_tags (post_id, tag_id)
VALUES
    -- Post 1: Spring Boot intro
    (1, 1), -- Spring Boot
    (1, 3), -- Java

    -- Post 2: GraphQL with Spring
    (2, 1), -- Spring Boot
    (2, 2), -- GraphQL
    (2, 3), -- Java

    -- Post 3: Postgres tips
    (3, 3), -- Java
    (3, 4); -- PostgreSQL

-- =====================
-- Comments
-- =====================
INSERT INTO comments (
    id,
    post_id,
    user_id,
    parent_id,
    content,
    created_at,
    updated_at
)
VALUES
    (
        1,
        1, -- Post 1
        2, -- asmith
        NULL,
        'Great introduction! This is exactly what I needed to get started.',
        NOW(),
        NOW()
    ),
    (
        2,
        1, -- Post 1
        1, -- jdoe
        1, -- reply to comment 1
        'Thanks Alice, glad it helped. Let me know what you''d like to see next.',
        NOW(),
        NOW()
    ),
    (
        3,
        2, -- Post 2
        3, -- bwayne
        NULL,
        'Nice overview. Could you add a section on authentication and authorization?',
        NOW(),
        NOW()
    );

-- =====================
-- Reviews
-- =====================
INSERT INTO reviews (
    id,
    post_id,
    user_id,
    rating,
    title,
    content,
    is_verified,
    created_at,
    updated_at
)
VALUES
    (
        1,
        1, -- Post 1
        2, -- asmith
        5,
        'Perfect starter guide',
        'Clear, concise, and practical. I was able to bootstrap a new service in minutes.',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        2,
        1, -- Post 1
        3, -- bwayne
        4,
        'Solid content',
        'Great article, though I would love to see more on security considerations.',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        3,
        2, -- Post 2
        1, -- jdoe
        5,
        'GraphQL done right',
        'Excellent walk-through of schema design and resolver patterns with Spring.',
        TRUE,
        NOW(),
        NOW()
    );

COMMIT;

