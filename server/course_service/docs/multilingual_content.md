# Multilingual Content in Course Service

This document explains how multilingual content is implemented in the Course Service.

## Overview

The Course Service supports multilingual content for courses, articles, and technology trees. Instead of creating separate entries for each language, we use a JSON-based approach where each entity stores content for multiple languages in a single record.

## Data Structure

### Articles

Articles store multilingual content directly in JSON fields:

```json
{
  "title": {
    "en": "Introduction to Programming",
    "ru": "Введение в программирование",
    "fr": "Introduction à la programmation"
  },
  "description": {
    "en": "Learn the basics of programming concepts",
    "ru": "Изучите основы концепций программирования",
    "fr": "Apprenez les bases des concepts de programmation"
  },
  "content": {
    "en": "# Introduction to Programming\n\nProgramming is the process...",
    "ru": "# Введение в программирование\n\nПрограммирование — это процесс...",
    "fr": "# Introduction à la programmation\n\nLa programmation est le processus..."
  }
}
```

### Technology Trees

Technology trees store multilingual content in node titles and descriptions:

```json
{
  "nodes": {
    "node1": {
      "id": "node1",
      "title": {
        "en": "Introduction to Programming",
        "ru": "Введение в программирование"
      },
      "description": {
        "en": "Learn the basics of programming concepts",
        "ru": "Изучите основы концепций программирования"
      },
      "type": "article",
      "position": {"x": 100, "y": 200},
      "requirements": [],
      "content_id": "3fa85f64-5717-4562-b3fc-2c963f66afa7"
    }
  }
}
```

## API Usage

### Creating Content with Multiple Languages

When creating an article, you can provide content in multiple languages:

```http
POST /api/articles/
Content-Type: application/json

{
  "course_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "slug": "introduction-to-programming",
  "title": {
    "en": "Introduction to Programming",
    "ru": "Введение в программирование"
  },
  "description": {
    "en": "Learn the basics of programming concepts",
    "ru": "Изучите основы концепций программирования"
  },
  "content": {
    "en": "# Introduction to Programming\n\nProgramming is the process of creating a set of instructions...",
    "ru": "# Введение в программирование\n\nПрограммирование — это процесс создания набора инструкций..."
  },
  "order": 1,
  "is_published": true
}
```

### Adding a New Language to Existing Content

You can add a new language to existing content without affecting other languages:

```http
PUT /api/articles/{article_id}
Content-Type: application/json

{
  "title": {
    "fr": "Introduction à la programmation"
  },
  "description": {
    "fr": "Apprenez les bases des concepts de programmation"
  },
  "content": {
    "fr": "# Introduction à la programmation\n\nLa programmation est le processus de création..."
  }
}
```

### Getting Content in a Specific Language

You can request content in a specific language:

```http
GET /api/articles/{article_id}/localized?language=ru&fallback=true
```

The `fallback` parameter determines whether to return content in another language if the requested language is not available.

### Getting Available Languages

You can get a list of available languages for an article:

```http
GET /api/articles/{article_id}/languages
```

Response:

```json
{
  "languages": ["en", "ru", "fr"]
}
```

## Helper Methods

Each model provides helper methods for working with multilingual content:

- `get_title(language, fallback)`: Get the title in a specific language
- `get_description(language, fallback)`: Get the description in a specific language
- `get_content(language, fallback)`: Get the content in a specific language
- `get_localized_version(language, fallback)`: Get a fully localized version of the entity
- `available_languages()`: Get a list of all available languages in the entity

## Migration from Legacy Content

The previous implementation used separate records for each language. The new approach stores all languages in a single record, making it easier to manage translations and keep content in sync across languages.

To migrate from the old approach:

1. For each article in a specific language, create or update the multilingual fields in the corresponding article
2. Remove the `language` field from the database schema (it's no longer needed)
3. Update API clients to work with the new multilingual format

## Best Practices

1. Always provide content in at least one language (usually English as the default)
2. Use consistent language codes (ISO 639-1 recommended: 'en', 'ru', 'fr', etc.)
3. When updating content, only include the languages you want to modify
4. Use the localized endpoints when you need content in a specific language
5. Consider implementing a translation workflow for managing multilingual content 