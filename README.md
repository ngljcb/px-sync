# Publish/Subscribe Messaging System

### Summary

This project implements a basic publish/subscribe messaging system using Java. The system allows multiple clients to connect to a server and interact through different roles: **publishers** and **subscribers**. Publishers can send messages to specific topics, while subscribers receive messages from the topics they are subscribed to.

### Objectives

- **Client-Server Architecture**: Implement a server that handles multiple client connections using sockets.
- **Publish/Subscribe Model**: Enable clients to publish messages to and subscribe to topics dynamically.
- **Topic Management**: Automatically create and manage topics based on client requests.
- **Concurrency Handling**: Ensure correct operation of the system under concurrent client interactions.
- **Server Administration**: Provide server-side commands to inspect and manage topics and messages.

### Features

- Clients can publish and subscribe to topics.
- Server can inspect and manage topics, including message deletion.
- Concurrency is handled to allow multiple clients to interact with the system simultaneously.