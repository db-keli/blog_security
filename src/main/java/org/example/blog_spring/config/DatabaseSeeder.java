package org.example.blog_spring.config;

import java.time.Instant;
import java.util.Set;

import org.example.blog_spring.domain.Comment;
import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.domain.Review;
import org.example.blog_spring.domain.Tag;
import org.example.blog_spring.domain.User;
import org.example.blog_spring.repository.CommentRepository;
import org.example.blog_spring.repository.PostRepository;
import org.example.blog_spring.repository.ReviewRepository;
import org.example.blog_spring.repository.TagRepository;
import org.example.blog_spring.repository.UserRepository;
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

        private final UserRepository userRepository;
        private final TagRepository tagRepository;
        private final PostRepository postRepository;
        private final CommentRepository commentRepository;
        private final ReviewRepository reviewRepository;

        public DatabaseSeeder(UserRepository userRepository, TagRepository tagRepository,
                        PostRepository postRepository, CommentRepository commentRepository,
                        ReviewRepository reviewRepository) {
                this.userRepository = userRepository;
                this.tagRepository = tagRepository;
                this.postRepository = postRepository;
                this.commentRepository = commentRepository;
                this.reviewRepository = reviewRepository;
        }

        @Override
        @Transactional
        public void run(String... args) {
                if (userRepository.count() > 0) {
                        log.debug("Database already seeded, skipping");
                        return;
                }
                log.info("Seeding database with sample data...");

                var jdoe = new User("jdoe", "jdoe@example.com", "John Doe");
                jdoe.setBio("Senior Java developer who writes about Spring and backend architecture.");
                jdoe = userRepository.save(jdoe);

                var asmith = new User("asmith", "asmith@example.com", "Alice Smith");
                asmith.setBio("Full-stack engineer who enjoys GraphQL and modern frontend stacks.");
                asmith = userRepository.save(asmith);

                var bwayne = new User("bwayne", "bwayne@example.com", "Bruce Wayne");
                bwayne.setBio("Security-focused architect writing about performance and hardening.");
                bwayne = userRepository.save(bwayne);

                var tagSpring = new Tag("Spring Boot", "spring-boot");
                tagSpring.setDescription(
                                "Articles related to Spring Boot and the Spring ecosystem.");
                tagSpring = tagRepository.save(tagSpring);

                var tagGraphql = new Tag("GraphQL", "graphql");
                tagGraphql.setDescription("APIs and schema design using GraphQL.");
                tagGraphql = tagRepository.save(tagGraphql);

                var tagJava = new Tag("Java", "java");
                tagJava.setDescription("Core Java language and JVM topics.");
                tagJava = tagRepository.save(tagJava);

                var tagPostgres = new Tag("PostgreSQL", "postgresql");
                tagPostgres.setDescription(
                                "Relational data modeling and Postgres-specific features.");
                tagPostgres = tagRepository.save(tagPostgres);

                var post1 = new Post(jdoe.getId(), "Getting Started with Spring Boot",
                                "This post walks through setting up a new Spring Boot application, including dependencies, configuration, and a simple REST endpoint.",
                                "getting-started-with-spring-boot");
                post1.setStatus(PostStatus.PUBLISHED);
                post1.setPublishedAt(Instant.now());
                post1.setTags(Set.of(tagSpring, tagJava));
                post1 = postRepository.save(post1);

                var post2 = new Post(asmith.getId(), "Building a GraphQL API with Spring",
                                "In this article we design a schema-first GraphQL API and implement it using Spring GraphQL, discussing resolvers, DTOs, and error handling.",
                                "building-a-graphql-api-with-spring");
                post2.setStatus(PostStatus.PUBLISHED);
                post2.setPublishedAt(Instant.now());
                post2.setTags(Set.of(tagSpring, tagGraphql, tagJava));
                post2 = postRepository.save(post2);

                var post3 = new Post(jdoe.getId(), "PostgreSQL Tips for Spring Data JPA",
                                "Draft notes on tuning PostgreSQL for Spring Data JPA, including indexes, connection pooling, and query analysis.",
                                "postgresql-tips-spring-data-jpa");
                post3.setTags(Set.of(tagJava, tagPostgres));
                post3 = postRepository.save(post3);

                var c1 = new Comment(post1.getId(), asmith.getId(),
                                "Great introduction! This is exactly what I needed to get started.",
                                null);
                c1 = commentRepository.save(c1);

                var c2 = new Comment(post1.getId(), jdoe.getId(),
                                "Thanks Alice, glad it helped. Let me know what you'd like to see next.",
                                c1.getId());
                commentRepository.save(c2);

                var c3 = new Comment(post2.getId(), bwayne.getId(),
                                "Nice overview. Could you add a section on authentication and authorization?",
                                null);
                commentRepository.save(c3);

                post1.setCommentCount(commentRepository.countByPostId(post1.getId()));
                post2.setCommentCount(commentRepository.countByPostId(post2.getId()));
                post3.setCommentCount(commentRepository.countByPostId(post3.getId()));
                postRepository.save(post1);
                postRepository.save(post2);
                postRepository.save(post3);

                var r1 = new Review(post1.getId(), asmith.getId(), (short) 5);
                r1.setTitle("Perfect starter guide");
                r1.setContent("Clear, concise, and practical. I was able to bootstrap a new service in minutes.");
                r1.setVerified(true);
                reviewRepository.save(r1);

                var r2 = new Review(post1.getId(), bwayne.getId(), (short) 4);
                r2.setTitle("Solid content");
                r2.setContent("Great article, though I would love to see more on security considerations.");
                reviewRepository.save(r2);

                var r3 = new Review(post2.getId(), jdoe.getId(), (short) 5);
                r3.setTitle("GraphQL done right");
                r3.setContent("Excellent walk-through of schema design and resolver patterns with Spring.");
                r3.setVerified(true);
                reviewRepository.save(r3);

                log.info("Database seeded successfully");
        }
}
