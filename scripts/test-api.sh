#!/bin/bash
# Manual API test script for the blog platform
# Usage: ./scripts/test-api.sh
# Ensure the app is running: mvn spring-boot:run -Dspring-boot.run.profiles=dev
# Uses jq for pretty output if available; otherwise prints raw JSON

BASE="${BASE_URL:-http://localhost:8080}"
pr() { jq . 2>/dev/null || cat; }

echo "=== USERS ==="
echo "List users (page 1, size 3)"
curl -s -X GET "$BASE/api/users?page=1&size=3" | pr

echo -e "\nGet user by id (1)"
curl -s -X GET "$BASE/api/users/1" | pr

echo -e "\nCreate user"
curl -s -X POST "$BASE/api/users" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"newuser$(date +%s)\",\"email\":\"newuser$(date +%s)@example.com\",\"fullName\":\"New User\"}" | pr

echo -e "\nUpdate user (1)"
curl -s -X PUT "$BASE/api/users/1" \
  -H "Content-Type: application/json" \
  -d '{"username":"jdoe","email":"jdoe@example.com","fullName":"John Doe Updated"}' | pr

echo -e "\n=== TAGS ==="
echo "List tags"
curl -s -X GET "$BASE/api/tags?page=1&size=10" | pr

echo -e "\nGet tag by id (1)"
curl -s -X GET "$BASE/api/tags/1" | pr

echo -e "\nGet tag by slug (spring-boot)"
curl -s -X GET "$BASE/api/tags/slug/spring-boot" | pr

echo -e "\nCreate tag"
curl -s -X POST "$BASE/api/tags" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Testing-$(date +%s)\",\"slug\":\"testing-$(date +%s)\",\"description\":\"For tests\"}" | pr

echo -e "\nUpdate tag (1)"
curl -s -X PUT "$BASE/api/tags/1" \
  -H "Content-Type: application/json" \
  -d '{"name":"Spring Boot","slug":"spring-boot","description":"Updated description"}' | pr

echo -e "\n=== POSTS ==="
echo "List posts"
curl -s -X GET "$BASE/api/posts?page=1&size=5" | pr

echo -e "\nList posts (published only)"
curl -s -X GET "$BASE/api/posts?page=1&size=5&publishedOnly=true" | pr

echo -e "\nList posts by author (1)"
curl -s -X GET "$BASE/api/posts?authorId=1&page=1&size=5" | pr

echo -e "\nList posts by tag (java)"
curl -s -X GET "$BASE/api/posts?tag=java&page=1&size=5" | pr

echo -e "\nList posts search 'GraphQL'"
curl -s -X GET "$BASE/api/posts?search=GraphQL&page=1&size=5" | pr

echo -e "\nGet post by id (1)"
curl -s -X GET "$BASE/api/posts/1" | pr

echo -e "\nGet post by slug"
curl -s -X GET "$BASE/api/posts/slug/getting-started-with-spring-boot" | pr

echo -e "\nCreate post"
curl -s -X POST "$BASE/api/posts" \
  -H "Content-Type: application/json" \
  -d "{\"authorId\":1,\"title\":\"Test Post\",\"content\":\"Test content here.\",\"slug\":\"test-post-$(date +%s)\",\"tagIds\":[1,3]}" | pr

echo -e "\nUpdate post (1)"
curl -s -X PUT "$BASE/api/posts/1" \
  -H "Content-Type: application/json" \
  -d '{"title":"Getting Started with Spring Boot","content":"Updated content.","slug":"getting-started-with-spring-boot","status":"PUBLISHED","tagIds":[1,3]}' | pr

echo -e "\n=== COMMENTS ==="
echo "List comments for post (1)"
curl -s -X GET "$BASE/api/comments/by-post/1?page=1&size=5" | pr

echo -e "\nList comments for user (1)"
curl -s -X GET "$BASE/api/comments?userId=1&page=1&size=5" | pr

echo -e "\nGet comment by id (1)"
curl -s -X GET "$BASE/api/comments/1" | pr

echo -e "\nCreate comment"
curl -s -X POST "$BASE/api/comments" \
  -H "Content-Type: application/json" \
  -d '{"postId":1,"userId":2,"parentId":null,"content":"Another test comment."}' | pr

echo -e "\nUpdate comment (1)"
curl -s -X PUT "$BASE/api/comments/1" \
  -H "Content-Type: application/json" \
  -d '{"content":"Updated comment content."}' | pr

echo -e "\n=== REVIEWS ==="
echo "List reviews for post (1)"
curl -s -X GET "$BASE/api/reviews/by-post/1?page=1&size=5" | pr

echo -e "\nList reviews for user (1)"
curl -s -X GET "$BASE/api/reviews?userId=1&page=1&size=5" | pr

echo -e "\nGet review by user and post"
curl -s -X GET "$BASE/api/reviews/by-user-and-post?userId=2&postId=1" | pr

echo -e "\nGet review by id (1)"
curl -s -X GET "$BASE/api/reviews/1" | pr

echo -e "\nCreate review (post 3 to avoid unique constraint)"
curl -s -X POST "$BASE/api/reviews" \
  -H "Content-Type: application/json" \
  -d '{"postId":3,"userId":2,"rating":4,"title":"Nice read","content":"Good article","verified":false}' | pr

echo -e "\nUpdate review (1)"
curl -s -X PUT "$BASE/api/reviews/1" \
  -H "Content-Type: application/json" \
  -d '{"rating":5,"title":"Perfect starter guide","content":"Clear and practical.","verified":true}' | pr

echo -e "\n=== GRAPHQL ==="
echo "Query: users"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ users(page: 0, size: 3) { id username email fullName } }"}' | pr

echo -e "\nQuery: posts"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ posts(page: 0, size: 3) { id title slug status tags { id slug } } }"}' | pr

echo -e "\nQuery: tags"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ tags(page: 0, size: 5) { id name slug } }"}' | pr

echo -e "\nMutation: createUser"
curl -s -X POST "$BASE/graphql" \
  -H "Content-Type: application/json" \
  -d "{\"query\":\"mutation { createUser(input: { username: \\\"gqluser$(date +%s)\\\", email: \\\"gqluser$(date +%s)@example.com\\\", fullName: \\\"GraphQL User\\\" }) { id username email fullName } }\"}" | pr

echo -e "\n=== HEALTH (optional) ==="
curl -s -X GET "$BASE/actuator/health" | pr
