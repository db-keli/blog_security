#!/bin/bash
# GraphQL API test script for the blog platform
# Usage: ./scripts/test_graphql.sh
# Ensure the app is running: mvn spring-boot:run -Dspring-boot.run.profiles=dev
# Uses jq for pretty output if available; otherwise prints raw JSON

BASE="${BASE_URL:-http://localhost:8080}"
pr() { jq . 2>/dev/null || cat; }

echo "=== GraphQL Tests ==="

echo -e "\n--- Query: users ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ users(page: 0, size: 3) { id username email fullName createdAt } }"}' | pr

echo -e "\n--- Query: user (by id) ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ user(id: 1) { id username email fullName } }"}' | pr

echo -e "\n--- Query: posts ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ posts(page: 0, size: 3) { id title slug status authorId tags { id name slug } } }"}' | pr

echo -e "\n--- Query: posts (published only) ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ posts(page: 0, size: 5, publishedOnly: true) { id title slug status } }"}' | pr

echo -e "\n--- Query: posts (by author) ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ posts(page: 0, size: 5, authorId: 1) { id title authorId } }"}' | pr

echo -e "\n--- Query: posts (by tag) ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ posts(page: 0, size: 5, tag: \"java\") { id title tags { slug } } }"}' | pr

echo -e "\n--- Query: posts (search) ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ posts(page: 0, size: 5, search: \"GraphQL\") { id title content } }"}' | pr

echo -e "\n--- Query: post (by id) ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ post(id: 1) { id title slug status content } }"}' | pr

echo -e "\n--- Query: postBySlug ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ postBySlug(slug: \"getting-started-with-spring-boot\") { id title slug } }"}' | pr

echo -e "\n--- Query: tags ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ tags(page: 0, size: 10) { id name slug description } }"}' | pr

echo -e "\n--- Query: tagBySlug ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ tagBySlug(slug: \"spring-boot\") { id name slug description } }"}' | pr

echo -e "\n--- Query: commentsByPost ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ commentsByPost(postId: 1, page: 0, size: 10) { id postId userId content parentId } }"}' | pr

echo -e "\n--- Query: reviewsByPost ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ reviewsByPost(postId: 1, page: 0, size: 10) { id postId userId rating title verified } }"}' | pr

echo -e "\n--- Mutation: createUser ---"
TS=$(date +%s)
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d "{\"query\":\"mutation { createUser(input: { username: \\\"gqluser$TS\\\", email: \\\"gqluser$TS@example.com\\\", fullName: \\\"GraphQL Test User\\\" }) { id username email fullName } }\"}" | pr

echo -e "\n--- Mutation: createTag ---"
TS=$(date +%s)
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d "{\"query\":\"mutation { createTag(input: { name: \\\"GraphQL-Tag-$TS\\\", slug: \\\"graphql-tag-$TS\\\", description: \\\"Created via GraphQL\\\" }) { id name slug } }\"}" | pr

echo -e "\n--- Mutation: createPost ---"
TS=$(date +%s)
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d "{\"query\":\"mutation { createPost(input: { authorId: 1, title: \\\"GraphQL Test Post\\\", content: \\\"Content created via GraphQL test script.\\\", slug: \\\"graphql-test-$TS\\\", tagIds: [1, 2] }) { id title slug authorId tags { id slug } } }\"}" | pr

echo -e "\n--- Mutation: createComment ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { createComment(input: { postId: 1, userId: 2, parentId: null, content: "GraphQL test comment." }) { id postId userId content } }"}' | pr

echo -e "\n--- Mutation: createReview ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { createReview(input: { postId: 3, userId: 2, rating: 4, title: "GraphQL test review", content: "Created via test script", verified: false }) { id postId userId rating title verified } }"}' | pr

echo -e "\n--- Mutation: updateUser ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { updateUser(id: 1, input: { username: \"jdoe\", email: \"jdoe@example.com\", fullName: \"John Doe\" }) { id username fullName } }"}' | pr

echo -e "\n--- Mutation: updatePost ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { updatePost(id: 1, input: { title: \"Getting Started with Spring Boot\", content: \"Updated content.\", slug: \"getting-started-with-spring-boot\", status: \"PUBLISHED\", tagIds: [1, 3] }) { id title status } }"}' | pr

echo -e "\n--- Mutation: updateComment ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { updateComment(id: 1, input: { content: \"Updated via GraphQL.\" }) { id content } }"}' | pr

echo -e "\n--- Mutation: updateReview ---"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { updateReview(id: 1, input: { rating: 5, title: \"Perfect\", content: \"Excellent.\", verified: true }) { id rating verified } }"}' | pr

echo -e "\n=== GraphQL tests complete ==="
