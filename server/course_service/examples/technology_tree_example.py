"""
Technology Tree Example

This example demonstrates how to create and work with a technology tree structure.
It includes:
1. Creating a tree structure
2. Adding nodes and connections
3. Working with groups
4. Localizing content
"""

import json
import uuid
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional

# Example technology tree structure based on the schema
def create_sample_technology_tree(course_id: str) -> Dict[str, Any]:
    """
    Create a sample technology tree for a programming course
    
    Args:
        course_id: UUID of the course this tree belongs to
        
    Returns:
        A complete technology tree structure ready to be stored in the database
    """
    # Generate unique IDs for nodes
    node_intro_id = str(uuid.uuid4())
    node_basics_id = str(uuid.uuid4())
    node_variables_id = str(uuid.uuid4())
    node_conditionals_id = str(uuid.uuid4())
    node_loops_id = str(uuid.uuid4())
    node_functions_id = str(uuid.uuid4())
    node_project_id = str(uuid.uuid4())
    
    # Create nodes
    nodes = {
        node_intro_id: {
            "id": node_intro_id,
            "titleKey": "programming.intro",
            "title": {
                "en": "Introduction to Programming",
                "ru": "Введение в программирование"
            },
            "descriptionKey": "programming.intro.description",
            "description": {
                "en": "Learn the basics of programming and computational thinking",
                "ru": "Изучите основы программирования и вычислительного мышления"
            },
            "position": {"x": 100, "y": 150},
            "style": "circular",
            "contentId": None,  # Would typically reference a lesson UUID
            "requirements": [],
            "status": "published",
            "type": "lesson",
            "metadata": {
                "estimatedTimeMinutes": 30,
                "difficultyLevel": "beginner",
                "order": 1,
                "tags": ["introduction", "basics"]
            },
            "visualAttributes": {
                "color": "#4A90E2",
                "icon": "book",
                "size": "medium"
            }
        },
        node_basics_id: {
            "id": node_basics_id,
            "titleKey": "programming.basics",
            "title": {
                "en": "Programming Basics",
                "ru": "Основы программирования"
            },
            "descriptionKey": "programming.basics.description",
            "description": {
                "en": "Learn about code structure, syntax, and basic operations",
                "ru": "Изучите структуру кода, синтаксис и основные операции"
            },
            "position": {"x": 250, "y": 150},
            "style": "circular",
            "contentId": None,
            "requirements": [node_intro_id],
            "status": "published",
            "type": "lesson",
            "metadata": {
                "estimatedTimeMinutes": 45,
                "difficultyLevel": "beginner",
                "order": 2,
                "tags": ["basics", "syntax"]
            },
            "visualAttributes": {
                "color": "#50E3C2",
                "icon": "code",
                "size": "medium"
            }
        },
        node_variables_id: {
            "id": node_variables_id,
            "titleKey": "programming.variables",
            "title": {
                "en": "Variables and Data Types",
                "ru": "Переменные и типы данных"
            },
            "descriptionKey": "programming.variables.description",
            "description": {
                "en": "Learn about variables, data types, and basic operations",
                "ru": "Изучите переменные, типы данных и основные операции"
            },
            "position": {"x": 400, "y": 100},
            "style": "hexagon",
            "contentId": None,
            "requirements": [node_basics_id],
            "status": "published",
            "type": "lesson",
            "metadata": {
                "estimatedTimeMinutes": 60,
                "difficultyLevel": "beginner",
                "order": 3,
                "tags": ["variables", "data types"]
            },
            "visualAttributes": {
                "color": "#F5A623",
                "icon": "database",
                "size": "medium"
            }
        },
        node_conditionals_id: {
            "id": node_conditionals_id,
            "titleKey": "programming.conditionals",
            "title": {
                "en": "Conditional Statements",
                "ru": "Условные операторы"
            },
            "descriptionKey": "programming.conditionals.description",
            "description": {
                "en": "Learn about if-else statements and logical operations",
                "ru": "Изучите операторы if-else и логические операции"
            },
            "position": {"x": 400, "y": 200},
            "style": "hexagon",
            "contentId": None,
            "requirements": [node_basics_id],
            "status": "published",
            "type": "lesson",
            "metadata": {
                "estimatedTimeMinutes": 60,
                "difficultyLevel": "beginner",
                "order": 4,
                "tags": ["conditionals", "logic"]
            },
            "visualAttributes": {
                "color": "#F5A623",
                "icon": "git-branch",
                "size": "medium"
            }
        },
        node_loops_id: {
            "id": node_loops_id,
            "titleKey": "programming.loops",
            "title": {
                "en": "Loops and Iterations",
                "ru": "Циклы и итерации"
            },
            "descriptionKey": "programming.loops.description",
            "description": {
                "en": "Learn about for loops, while loops, and iterations",
                "ru": "Изучите циклы for, while и итерации"
            },
            "position": {"x": 550, "y": 100},
            "style": "hexagon",
            "contentId": None,
            "requirements": [node_variables_id, node_conditionals_id],
            "status": "published",
            "type": "lesson",
            "metadata": {
                "estimatedTimeMinutes": 75,
                "difficultyLevel": "intermediate",
                "order": 5,
                "tags": ["loops", "iterations"]
            },
            "visualAttributes": {
                "color": "#BD10E0",
                "icon": "repeat",
                "size": "medium"
            }
        },
        node_functions_id: {
            "id": node_functions_id,
            "titleKey": "programming.functions",
            "title": {
                "en": "Functions and Methods",
                "ru": "Функции и методы"
            },
            "descriptionKey": "programming.functions.description",
            "description": {
                "en": "Learn about functions, parameters, and return values",
                "ru": "Изучите функции, параметры и возвращаемые значения"
            },
            "position": {"x": 550, "y": 200},
            "style": "hexagon",
            "contentId": None,
            "requirements": [node_variables_id, node_conditionals_id],
            "status": "published",
            "type": "lesson",
            "metadata": {
                "estimatedTimeMinutes": 75,
                "difficultyLevel": "intermediate",
                "order": 6,
                "tags": ["functions", "methods"]
            },
            "visualAttributes": {
                "color": "#BD10E0",
                "icon": "box",
                "size": "medium"
            }
        },
        node_project_id: {
            "id": node_project_id,
            "titleKey": "programming.project",
            "title": {
                "en": "Final Project",
                "ru": "Итоговый проект"
            },
            "descriptionKey": "programming.project.description",
            "description": {
                "en": "Apply everything you've learned in a comprehensive project",
                "ru": "Примените все, что вы изучили, в комплексном проекте"
            },
            "position": {"x": 700, "y": 150},
            "style": "square",
            "contentId": None,
            "requirements": [node_loops_id, node_functions_id],
            "status": "published",
            "type": "project",
            "metadata": {
                "estimatedTimeMinutes": 120,
                "difficultyLevel": "intermediate",
                "order": 7,
                "tags": ["project", "application"]
            },
            "visualAttributes": {
                "color": "#D0021B",
                "icon": "star",
                "size": "large"
            }
        }
    }
    
    # Create connections
    connections = [
        {
            "id": f"conn-{str(uuid.uuid4())}",
            "from": node_intro_id,
            "to": node_basics_id,
            "style": "solid_arrow",
            "type": "required",
            "visualAttributes": {
                "color": "#4A90E2",
                "thickness": 2,
                "dashPattern": [0]
            }
        },
        {
            "id": f"conn-{str(uuid.uuid4())}",
            "from": node_basics_id,
            "to": node_variables_id,
            "style": "solid_arrow",
            "type": "required",
            "visualAttributes": {
                "color": "#4A90E2",
                "thickness": 2,
                "dashPattern": [0]
            }
        },
        {
            "id": f"conn-{str(uuid.uuid4())}",
            "from": node_basics_id,
            "to": node_conditionals_id,
            "style": "solid_arrow",
            "type": "required",
            "visualAttributes": {
                "color": "#4A90E2",
                "thickness": 2,
                "dashPattern": [0]
            }
        },
        {
            "id": f"conn-{str(uuid.uuid4())}",
            "from": node_variables_id,
            "to": node_loops_id,
            "style": "solid_arrow",
            "type": "required",
            "visualAttributes": {
                "color": "#4A90E2",
                "thickness": 2,
                "dashPattern": [0]
            }
        },
        {
            "id": f"conn-{str(uuid.uuid4())}",
            "from": node_conditionals_id,
            "to": node_loops_id,
            "style": "dashed_line",
            "type": "recommended",
            "visualAttributes": {
                "color": "#9B9B9B",
                "thickness": 1,
                "dashPattern": [5, 5]
            }
        },
        {
            "id": f"conn-{str(uuid.uuid4())}",
            "from": node_variables_id,
            "to": node_functions_id,
            "style": "solid_arrow",
            "type": "required",
            "visualAttributes": {
                "color": "#4A90E2",
                "thickness": 2,
                "dashPattern": [0]
            }
        },
        {
            "id": f"conn-{str(uuid.uuid4())}",
            "from": node_conditionals_id,
            "to": node_functions_id,
            "style": "dashed_line",
            "type": "recommended",
            "visualAttributes": {
                "color": "#9B9B9B",
                "thickness": 1,
                "dashPattern": [5, 5]
            }
        },
        {
            "id": f"conn-{str(uuid.uuid4())}",
            "from": node_loops_id,
            "to": node_project_id,
            "style": "solid_arrow",
            "type": "required",
            "visualAttributes": {
                "color": "#4A90E2",
                "thickness": 2,
                "dashPattern": [0]
            }
        },
        {
            "id": f"conn-{str(uuid.uuid4())}",
            "from": node_functions_id,
            "to": node_project_id,
            "style": "solid_arrow",
            "type": "required",
            "visualAttributes": {
                "color": "#4A90E2",
                "thickness": 2,
                "dashPattern": [0]
            }
        }
    ]
    
    # Create groups
    basics_group_id = f"group-{str(uuid.uuid4())}"
    advanced_group_id = f"group-{str(uuid.uuid4())}"
    
    groups = [
        {
            "id": basics_group_id,
            "nameKey": "programming.group.basics",
            "name": {
                "en": "Programming Fundamentals",
                "ru": "Основы программирования"
            },
            "nodes": [node_intro_id, node_basics_id, node_variables_id, node_conditionals_id],
            "visualAttributes": {
                "color": "#E6F7FF",
                "position": {"x": 250, "y": 150},
                "collapsed": False,
                "border": {
                    "color": "#4A90E2",
                    "style": "dashed",
                    "thickness": 1
                }
            }
        },
        {
            "id": advanced_group_id,
            "nameKey": "programming.group.advanced",
            "name": {
                "en": "Advanced Topics",
                "ru": "Продвинутые темы"
            },
            "nodes": [node_loops_id, node_functions_id, node_project_id],
            "visualAttributes": {
                "color": "#FFF1E6",
                "position": {"x": 600, "y": 150},
                "collapsed": False,
                "border": {
                    "color": "#F5A623",
                    "style": "dashed",
                    "thickness": 1
                }
            }
        }
    ]
    
    # Create the complete tree structure
    tree = {
        "id": str(uuid.uuid4()),
        "course_id": course_id,
        "version": 1,
        "is_published": False,
        "data": {
            "nodes": nodes,
            "connections": connections,
            "groups": groups,
            "metadata": {
                "defaultLanguage": "en",
                "availableLanguages": ["en", "ru"],
                "totalNodes": len(nodes),
                "layoutType": "tree",
                "layoutDirection": "horizontal",
                "canvasSize": {
                    "width": 1200,
                    "height": 800
                },
                "version": "1.0",
                "created_at": datetime.now(timezone.utc).isoformat(),
                "updated_at": datetime.now(timezone.utc).isoformat()
            }
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
        "updated_at": datetime.now(timezone.utc).isoformat()
    }
    
    return tree

# Example function to add a node to an existing tree
def add_node_to_tree(tree: Dict[str, Any], node_data: Dict[str, Any]) -> Dict[str, Any]:
    """
    Add a new node to an existing technology tree
    
    Args:
        tree: The existing technology tree
        node_data: The node data to add
        
    Returns:
        The updated technology tree
    """
    # Clone the tree to avoid modifying the original
    updated_tree = dict(tree)
    
    # Make sure the node has an ID
    if "id" not in node_data:
        node_data["id"] = str(uuid.uuid4())
    
    # Add the node to the tree
    if "data" not in updated_tree:
        updated_tree["data"] = {}
    
    if "nodes" not in updated_tree["data"]:
        updated_tree["data"]["nodes"] = {}
    
    updated_tree["data"]["nodes"][node_data["id"]] = node_data
    
    # Update metadata
    if "metadata" not in updated_tree["data"]:
        updated_tree["data"]["metadata"] = {}
    
    updated_tree["data"]["metadata"]["totalNodes"] = len(updated_tree["data"]["nodes"])
    updated_tree["data"]["metadata"]["updated_at"] = datetime.now(timezone.utc).isoformat()
    
    # Increment version
    updated_tree["version"] = updated_tree.get("version", 0) + 1
    updated_tree["updated_at"] = datetime.now(timezone.utc).isoformat()
    
    return updated_tree

# Example function to connect nodes in a tree
def connect_nodes(tree: Dict[str, Any], from_node_id: str, to_node_id: str, 
                 connection_type: str = "required", style: str = "solid_arrow") -> Dict[str, Any]:
    """
    Create a connection between two nodes in the technology tree
    
    Args:
        tree: The existing technology tree
        from_node_id: The ID of the source node
        to_node_id: The ID of the target node
        connection_type: The type of connection (required, recommended, optional)
        style: The visual style of the connection
        
    Returns:
        The updated technology tree
    """
    # Clone the tree to avoid modifying the original
    updated_tree = dict(tree)
    
    # Make sure the nodes exist
    if ("data" not in updated_tree or "nodes" not in updated_tree["data"] or
            from_node_id not in updated_tree["data"]["nodes"] or
            to_node_id not in updated_tree["data"]["nodes"]):
        raise ValueError("Both nodes must exist in the tree")
    
    # Create the connection
    connection = {
        "id": f"conn-{str(uuid.uuid4())}",
        "from": from_node_id,
        "to": to_node_id,
        "style": style,
        "type": connection_type,
        "visualAttributes": {
            "color": "#4A90E2" if connection_type == "required" else "#9B9B9B",
            "thickness": 2 if connection_type == "required" else 1,
            "dashPattern": [0] if style == "solid_arrow" else [5, 5]
        }
    }
    
    # Add the connection to the tree
    if "connections" not in updated_tree["data"]:
        updated_tree["data"]["connections"] = []
    
    updated_tree["data"]["connections"].append(connection)
    
    # Update metadata
    updated_tree["data"]["metadata"]["updated_at"] = datetime.now(timezone.utc).isoformat()
    
    # Increment version
    updated_tree["version"] = updated_tree.get("version", 0) + 1
    updated_tree["updated_at"] = datetime.now(timezone.utc).isoformat()
    
    return updated_tree

# Example function to get a localized version of the tree
def get_localized_tree(tree: Dict[str, Any], language: str = "en") -> Dict[str, Any]:
    """
    Get a localized version of the technology tree
    
    Args:
        tree: The technology tree
        language: The language code to use for localization
        
    Returns:
        A localized version of the tree
    """
    # Clone the tree to avoid modifying the original
    localized_tree = dict(tree)
    
    if "data" not in localized_tree or "nodes" not in localized_tree["data"]:
        return localized_tree
    
    # Process nodes
    for node_id, node in localized_tree["data"]["nodes"].items():
        # Localize title
        if "title" in node and isinstance(node["title"], dict):
            if language in node["title"]:
                node["title_localized"] = node["title"][language]
            else:
                # Fallback to first available language
                node["title_localized"] = next(iter(node["title"].values()), "")
        
        # Localize description
        if "description" in node and isinstance(node["description"], dict):
            if language in node["description"]:
                node["description_localized"] = node["description"][language]
            else:
                # Fallback to first available language
                node["description_localized"] = next(iter(node["description"].values()), "")
    
    # Process groups
    if "groups" in localized_tree["data"]:
        for group in localized_tree["data"]["groups"]:
            # Localize name
            if "name" in group and isinstance(group["name"], dict):
                if language in group["name"]:
                    group["name_localized"] = group["name"][language]
                else:
                    # Fallback to first available language
                    group["name_localized"] = next(iter(group["name"].values()), "")
    
    return localized_tree

# Example usage
if __name__ == "__main__":
    # Create a sample course ID
    course_id = str(uuid.uuid4())
    
    # Create a sample technology tree
    tree = create_sample_technology_tree(course_id)
    
    # Add a new node
    new_node = {
        "titleKey": "programming.arrays",
        "title": {
            "en": "Arrays and Lists",
            "ru": "Массивы и списки"
        },
        "descriptionKey": "programming.arrays.description",
        "description": {
            "en": "Learn about arrays, lists, and collections",
            "ru": "Изучите массивы, списки и коллекции"
        },
        "position": {"x": 550, "y": 300},
        "style": "hexagon",
        "contentId": None,
        "requirements": [],
        "status": "draft",
        "type": "lesson",
        "metadata": {
            "estimatedTimeMinutes": 60,
            "difficultyLevel": "intermediate",
            "order": 8,
            "tags": ["arrays", "collections"]
        },
        "visualAttributes": {
            "color": "#BD10E0",
            "icon": "list",
            "size": "medium"
        }
    }
    
    tree = add_node_to_tree(tree, new_node)
    
    # Get a localized version of the tree
    localized_tree = get_localized_tree(tree, "ru")
    
    # Print the tree structure (pretty-printed for readability)
    print(json.dumps(tree, indent=2))
    
    # Print a localized node title as an example
    first_node_id = next(iter(tree["data"]["nodes"].keys()))
    print(f"\nLocalized node title: {localized_tree['data']['nodes'][first_node_id].get('title_localized', 'No title')}") 