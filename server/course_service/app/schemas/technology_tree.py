"""
Schemas for technology tree
"""
from datetime import datetime
from typing import Dict, Any, Optional, List, Union
from uuid import UUID

from pydantic import BaseModel, Field, field_validator


class VisualAttributes(BaseModel):
    """Base model for visual attributes"""
    color: str = Field('#4A90E2', description="Element color in HEX format")


class NodeVisualAttributes(VisualAttributes):
    """Visual attributes for nodes"""
    icon: Optional[str] = Field(None, description="Node icon")
    size: str = Field('medium', description="Node size (small, medium, large)")


class ConnectionVisualAttributes(VisualAttributes):
    """Visual attributes for connections"""
    thickness: int = Field(1, description="Line thickness")
    dashPattern: List[int] = Field([0], description="Dash pattern (0 for solid line)")


class BorderAttributes(BaseModel):
    """Attributes for group border"""
    color: str = Field('#4A90E2', description="Border color in HEX format")
    style: str = Field("solid", description="Border style (solid, dashed, dotted)")
    thickness: int = Field(1, description="Border thickness")


class GroupVisualAttributes(VisualAttributes):
    """Visual attributes for groups"""
    position: Dict[str, float] = Field(..., description="Group position (x, y)")
    collapsed: bool = Field(False, description="Whether the group is collapsed")
    border: BorderAttributes = Field(default_factory=BorderAttributes)


class Position(BaseModel):
    """Position on canvas"""
    x: float
    y: float


class CanvasSize(BaseModel):
    """Canvas size"""
    width: int = Field(1200, description="Canvas width in pixels")
    height: int = Field(800, description="Canvas height in pixels")


class NodeMetadata(BaseModel):
    """Node metadata"""
    estimatedTimeMinutes: Optional[int] = Field(None, description="Estimated time in minutes")
    difficultyLevel: Optional[str] = Field(None, description="Difficulty level")
    order: Optional[int] = Field(None, description="Order number")
    tags: Optional[List[str]] = Field(None, description="Tags")


class TreeMetadata(BaseModel):
    """Tree metadata"""
    defaultLanguage: str = Field("en", description="Default language code")
    availableLanguages: List[str] = Field(default_factory=lambda: ["en"], description="Available languages")
    totalNodes: int = Field(0, description="Total number of nodes")
    layoutType: str = Field("tree", description="Layout type (tree, mesh, radial)")
    layoutDirection: str = Field("horizontal", description="Layout direction (horizontal, vertical)")
    canvasSize: CanvasSize = Field(default_factory=CanvasSize)


class NodeBase(BaseModel):
    """Base schema for a technology tree node"""
    id: UUID = Field(..., description="Unique identifier for the node")
    titleKey: str = Field(..., description="Localization key for title")
    descriptionKey: Optional[str] = Field(None, description="Localization key for description")
    position: Position = Field(..., description="Position on canvas")
    style: str = Field("circular", description="Display style (circular, hexagon, square)")
    contentId: Optional[UUID] = Field(None, description="Reference to content")
    requirements: List[UUID] = Field(default_factory=list, description="List of required node IDs")
    status: str = Field("published", description="Node status (published, draft, hidden)")
    type: str = Field("lesson", description="Content type (lesson, quiz, assignment, etc.)")
    metadata: Optional[NodeMetadata] = Field(None, description="Additional metadata")
    visualAttributes: NodeVisualAttributes = Field(default_factory=NodeVisualAttributes)


class NodeCreate(NodeBase):
    """Schema for creating a new node"""
    pass


class NodeUpdate(BaseModel):
    """Schema for updating an existing node"""
    titleKey: Optional[str] = None
    descriptionKey: Optional[str] = None
    position: Optional[Position] = None
    style: Optional[str] = None
    contentId: Optional[UUID] = None
    requirements: Optional[List[UUID]] = None
    status: Optional[str] = None
    type: Optional[str] = None
    metadata: Optional[NodeMetadata] = None
    visualAttributes: Optional[NodeVisualAttributes] = None


class Connection(BaseModel):
    """Model for connection between nodes"""
    id: UUID = Field(..., description="Unique identifier for the connection")
    from_: UUID = Field(..., alias="from", description="Source node ID")
    to: UUID = Field(..., description="Target node ID")
    style: str = Field("solid_arrow", description="Line style (solid_arrow, dashed_line, etc.)")
    type: str = Field("required", description="Connection type (required, recommended, optional)")
    visualAttributes: ConnectionVisualAttributes = Field(default_factory=ConnectionVisualAttributes)

    class Config:
        populate_by_name = True


class ConnectionCreate(BaseModel):
    """Schema for creating a new connection"""
    from_: UUID = Field(..., alias="from", description="Source node ID")
    to: UUID = Field(..., description="Target node ID")
    style: str = Field("solid_arrow", description="Line style (solid_arrow, dashed_line, etc.)")
    type: str = Field("required", description="Connection type (required, recommended, optional)")
    visualAttributes: Optional[ConnectionVisualAttributes] = Field(default_factory=ConnectionVisualAttributes)

    class Config:
        populate_by_name = True


class ConnectionUpdate(BaseModel):
    """Schema for updating an existing connection"""
    style: Optional[str] = None
    type: Optional[str] = None
    visualAttributes: Optional[ConnectionVisualAttributes] = None


class Group(BaseModel):
    """Model for grouping nodes"""
    id: UUID = Field(..., description="Unique identifier for the group")
    nameKey: str = Field(..., description="Localization key for group name")
    nodes: List[UUID] = Field(..., description="List of node IDs in the group")
    visualAttributes: GroupVisualAttributes


class GroupCreate(BaseModel):
    """Schema for creating a new group"""
    nameKey: str = Field(..., description="Localization key for group name")
    nodes: List[UUID] = Field(..., description="List of node IDs in the group")
    visualAttributes: GroupVisualAttributes


class GroupUpdate(BaseModel):
    """Schema for updating an existing group"""
    nameKey: Optional[str] = None
    nodes: Optional[List[UUID]] = None
    visualAttributes: Optional[GroupVisualAttributes] = None


class TechnologyTreeBase(BaseModel):
    """Base schema for technology tree data"""
    course_id: UUID
    data: Optional[Dict[str, Any]] = Field(default_factory=dict, description="Tree data containing nodes, connections, and metadata")
    is_published: bool = Field(default=False, description="Whether the tree is published")
    version: int = Field(default=1, description="Tree version number")


class TechnologyTreeCreate(BaseModel):
    """Schema for creating a new technology tree"""
    course_id: UUID
    data: Optional[Dict[str, Any]] = Field(default_factory=dict, description="Tree data containing nodes, connections, and metadata")
    is_published: Optional[bool] = Field(default=False)
    version: Optional[int] = Field(default=1)


class TechnologyTreeUpdate(BaseModel):
    """Schema for updating an existing technology tree"""
    data: Optional[Dict[str, Any]] = None
    is_published: Optional[bool] = None
    version: Optional[int] = None

    @field_validator('version')
    def version_must_be_positive(cls, v):
        if v is not None and v < 1:
            raise ValueError('Version must be a positive integer')
        return v


class TechnologyTree(TechnologyTreeBase):
    """Schema for a complete technology tree with metadata"""
    id: UUID
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class TechnologyTreeLanguages(BaseModel):
    """Schema for technology tree languages response"""
    languages: List[str] = Field(..., description="List of available languages in the technology tree")


class NodeAddRequest(BaseModel):
    """Request for adding a node to a technology tree"""
    node_id: Optional[UUID] = Field(None, description="Optional unique identifier for the node")
    node_data: NodeCreate = Field(..., description="Node data to add")


class NodeUpdateRequest(BaseModel):
    """Request for updating a node in a technology tree"""
    node_id: UUID = Field(..., description="Identifier of the node to update")
    node_data: NodeUpdate = Field(..., description="Node data to update")


class NodeResponse(BaseModel):
    """Response after node operation"""
    success: bool = Field(..., description="Whether the operation was successful")
    node_id: UUID = Field(..., description="ID of the affected node")
    message: Optional[str] = Field(None, description="Additional information")
    node: Optional[NodeBase] = Field(None, description="Updated node data if relevant")


class ConnectionAddRequest(BaseModel):
    """Request for adding a connection to a technology tree"""
    connection_data: ConnectionCreate = Field(..., description="Connection data to add")


class ConnectionUpdateRequest(BaseModel):
    """Request for updating a connection in a technology tree"""
    connection_id: UUID = Field(..., description="Identifier of the connection to update")
    connection_data: ConnectionUpdate = Field(..., description="Connection data to update")


class ConnectionResponse(BaseModel):
    """Response after connection operation"""
    success: bool = Field(..., description="Whether the operation was successful")
    connection_id: UUID = Field(..., description="ID of the affected connection")
    message: Optional[str] = Field(None, description="Additional information")
    connection: Optional[Connection] = Field(None, description="Updated connection data if relevant")


class GroupAddRequest(BaseModel):
    """Request for adding a group to a technology tree"""
    group_data: GroupCreate = Field(..., description="Group data to add")


class GroupUpdateRequest(BaseModel):
    """Request for updating a group in a technology tree"""
    group_id: UUID = Field(..., description="Identifier of the group to update")
    group_data: GroupUpdate = Field(..., description="Group data to update")


class GroupResponse(BaseModel):
    """Response after group operation"""
    success: bool = Field(..., description="Whether the operation was successful")
    group_id: UUID = Field(..., description="ID of the affected group")
    message: Optional[str] = Field(None, description="Additional information")
    group: Optional[Group] = Field(None, description="Updated group data if relevant")


class TreeExportFormat(BaseModel):
    """Schema for technology tree export format"""
    nodes: Dict[str, NodeBase] = Field(..., description="Tree nodes")
    connections: List[Connection] = Field(default_factory=list, description="Node connections for visualization")
    groups: Optional[List[Group]] = Field(default_factory=list, description="Node groups")
    metadata: Dict[str, Any] = Field(default_factory=dict, description="Tree metadata")


class TreeImportRequest(BaseModel):
    """Schema for importing a technology tree"""
    course_id: UUID = Field(..., description="Course ID for the tree")
    tree_data: TreeExportFormat = Field(..., description="Tree data to import")
    replace_existing: bool = Field(default=False, description="Whether to replace existing tree if present")
