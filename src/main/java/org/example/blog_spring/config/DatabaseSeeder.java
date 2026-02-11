package org.example.blog_spring.config;

import java.time.Instant;
import java.util.Set;

import org.example.blog_spring.dao.CommentDao;
import org.example.blog_spring.dao.PostDao;
import org.example.blog_spring.dao.ReviewDao;
import org.example.blog_spring.dao.TagDao;
import org.example.blog_spring.dao.UserDao;
import org.example.blog_spring.domain.Comment;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.domain.Review;
import org.example.blog_spring.domain.Tag;
import org.example.blog_spring.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the database with sample data when empty. Runs only in dev/docker profiles.
 */
@Component
@Profile({"dev", "docker"})
@Order(1)
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserDao userDao;
    private final TagDao tagDao;
    private final PostDao postDao;
    private final CommentDao commentDao;
    private final ReviewDao reviewDao;

    public DatabaseSeeder(UserDao userDao, TagDao tagDao, PostDao postDao, CommentDao commentDao,
            ReviewDao reviewDao) {
        this.userDao = userDao;
        this.tagDao = tagDao;
        this.postDao = postDao;
        this.commentDao = commentDao;
        this.reviewDao = reviewDao;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userDao.count() > 0) {
            log.debug("Database already seeded, skipping");
            return;
        }
        log.info("Seeding database with sample data...");

        var jdoe = new User("jdoe", "jdoe@example.com", "John Doe");
        jdoe.setBio("Senior Java developer who writes about Spring and backend architecture.");
        userDao.insert(jdoe);

        var asmith = new User("asmith", "asmith@example.com", "Alice Smith");
        asmith.setBio("Full-stack engineer who enjoys GraphQL and modern frontend stacks.");
        userDao.insert(asmith);

        var bwayne = new User("bwayne", "bwayne@example.com", "Bruce Wayne");
        bwayne.setBio("Security-focused architect writing about performance and hardening.");
        userDao.insert(bwayne);

        var tagSpring = new Tag("Spring Boot", "spring-boot");
        tagSpring.setDescription("Articles related to Spring Boot and the Spring ecosystem.");
        tagDao.insert(tagSpring);

        var tagGraphql = new Tag("GraphQL", "graphql");
        tagGraphql.setDescription("APIs and schema design using GraphQL.");
        tagDao.insert(tagGraphql);

        var tagJava = new Tag("Java", "java");
        tagJava.setDescription("Core Java language and JVM topics.");
        tagDao.insert(tagJava);

        var tagPostgres = new Tag("PostgreSQL", "postgresql");
        tagPostgres.setDescription("Relational data modeling and Postgres-specific features.");
        tagDao.insert(tagPostgres);

        var post1 = new Post(jdoe.getId(), "Getting Started with Spring Boot",
                "This post walks through setting up a new Spring Boot application, including dependencies, configuration, and a simple REST endpoint.",
                "getting-started-with-spring-boot");
        post1.setStatus(PostStatus.PUBLISHED);
        post1.setPublishedAt(Instant.now());
        postDao.insert(post1, Set.of(tagSpring.getId(), tagJava.getId()));

        var post2 = new Post(asmith.getId(), "Building a GraphQL API with Spring",
                "In this article we design a schema-first GraphQL API and implement it using Spring GraphQL, discussing resolvers, DTOs, and error handling.",
                "building-a-graphql-api-with-spring");
        post2.setStatus(PostStatus.PUBLISHED);
        post2.setPublishedAt(Instant.now());
        postDao.insert(post2, Set.of(tagSpring.getId(), tagGraphql.getId(), tagJava.getId()));

        var post3 = new Post(jdoe.getId(), "PostgreSQL Tips for Spring Data JPA",
                "Draft notes on tuning PostgreSQL for Spring Data JPA, including indexes, connection pooling, and query analysis.",
                "postgresql-tips-spring-data-jpa");
        postDao.insert(post3, Set.of(tagJava.getId(), tagPostgres.getId()));

        var posts = postDao.findAll(org.springframework.data.domain.PageRequest.of(0, 10));
        var post1Loaded = posts.getContent().get(0);
        var post2Loaded = posts.getContent().get(1);

        var c1 = new Comment(post1Loaded.getId(), asmith.getId(),
                "Great introduction! This is exactly what I needed to get started.", null);
        commentDao.insert(c1);

        var c2 = new Comment(post1Loaded.getId(), jdoe.getId(),
                "Thanks Alice, glad it helped. Let me know what you'd like to see next.", c1.getId());
        commentDao.insert(c2);

        var c3 = new Comment(post2Loaded.getId(), bwayne.getId(),
                "Nice overview. Could you add a section on authentication and authorization?", null);
        commentDao.insert(c3);

        var r1 = new Review(post1Loaded.getId(), asmith.getId(), (short) 5);
        r1.setTitle("Perfect starter guide");
        r1.setContent(
                "Clear, concise, and practical. I was able to bootstrap a new service in minutes.");
        r1.setVerified(true);
        reviewDao.insert(r1);

        var r2 = new Review(post1Loaded.getId(), bwayne.getId(), (short) 4);
        r2.setTitle("Solid content");
        r2.setContent("Great article, though I would love to see more on security considerations.");
        reviewDao.insert(r2);

        var r3 = new Review(post2Loaded.getId(), jdoe.getId(), (short) 5);
        r3.setTitle("GraphQL done right");
        r3.setContent("Excellent walk-through of schema design and resolver patterns with Spring.");
        r3.setVerified(true);
        reviewDao.insert(r3);

        log.info("Database seeded successfully");
    }
}
