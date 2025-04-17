"""
Pydantic schemas for TechnologyTree
"""
from typing import Dict, Any, Optional, List
from uuid import UUID
from pydantic import BaseModel, Field


# Base TechnologyTree schema with common attributes
class TechnologyTreeBase(BaseModel):
    """Base schema for technology tree"""
    data: Dict[str, Any] = Field(default_factory=dict, description="Technology tree structure data")


# Schema for creating a TechnologyTree
class TechnologyTreeCreate(TechnologyTreeBase):
    """Schema for creating a technology tree"""
    course_id: UUID = Field(..., description="ID of the course this technology tree belongs to")


# Schema for updating a TechnologyTree
class TechnologyTreeUpdate(BaseModel):
    """Schema for updating a technology tree"""
    data: Optional[Dict[str, Any]] = Field(None, description="Technology tree structure data")


# Schema for TechnologyTree in DB
class TechnologyTreeInDBBase(TechnologyTreeBase):
    """Base schema for technology tree in database"""
    id: UUID
    course_id: UUID

    class Config:
        from_attributes = True


# Schema for returning TechnologyTree
class TechnologyTree(TechnologyTreeInDBBase):
    """Schema for returning technology tree"""
    pass


# Schema for detailed TechnologyTree information
class TechnologyTreeDetail(TechnologyTree):
    """Schema for returning detailed technology tree information"""
    pass


# Base schema for node attributes
class TechnologyTreeNodeBase(BaseModel):
    """Base schema for technology tree node"""
    id: Optional[str] = Field(None, description="Unique ID of the node. If not provided, will be auto-generated")
    titleKey: Optional[str] = Field(None, description="Key for localized title text")
    descriptionKey: Optional[str] = Field(None, description="Key for localized description text")
    position: Optional[Dict[str, int]] = Field(None, description="Position of the node in the tree. Example: {x: 100, y: 150}")
    style: Optional[str] = Field(None, description="Style of the node (circular, hexagon, etc.)")
    styleClass: Optional[str] = Field(None, description="Style class (beginner, intermediate, advanced)")
    state: Optional[str] = Field("locked", description="State of the node (available, locked, completed)")
    difficulty: Optional[int] = Field(None, description="Difficulty level from 1-5")
    estimatedTime: Optional[int] = Field(None, description="Estimated time to complete in minutes")
    children: Optional[List[str]] = Field(default_factory=list, description="List of node IDs that are children of this node")
    contentId: Optional[str] = Field(None, description="ID of associated content")
    requirements: Optional[List[str]] = Field(default_factory=list, description="List of node IDs that must be completed before this node is available")


# Schema for creating a new node
class TechnologyTreeNodeCreate(TechnologyTreeNodeBase):
    """Schema for creating a new node in a technology tree"""
    pass


# Schema for updating a node
class TechnologyTreeNodeUpdate(TechnologyTreeNodeBase):
    """Schema for updating a node in a technology tree"""
    id: Optional[str] = Field(None, description="ID of the node (cannot be changed)")
    position: Optional[Dict[str, int]] = Field(None, description="Position of the node")

    class Config:
        extra = "allow"  # Allow additional fields for maximum flexibility


# Base schema for connection attributes
class TechnologyTreeConnectionBase(BaseModel):
    """Base schema for technology tree connection"""
    id: Optional[str] = Field(None, description="Unique ID of the connection. If not provided, will be auto-generated")
    from_node: str = Field(..., description="ID of the source node")
    to_node: str = Field(..., description="ID of the target node")
    style: Optional[str] = Field("solid_arrow", description="Style of the connection (solid_arrow, dashed_line, etc.)")
    styleClass: Optional[str] = Field(None, description="Style class (required, optional)")
    label: Optional[str] = Field(None, description="Label text for the connection")


# Schema for creating a new connection
class TechnologyTreeConnectionCreate(TechnologyTreeConnectionBase):
    """Schema for creating a new connection in a technology tree"""
    pass


# Schema for updating a connection
class TechnologyTreeConnectionUpdate(BaseModel):
    """Schema for updating a connection in a technology tree"""
    style: Optional[str] = Field(None, description="Style of the connection")
    styleClass: Optional[str] = Field(None, description="Style class")
    label: Optional[str] = Field(None, description="Label text for the connection")

    class Config:
        extra = "allow"  # Allow additional fields for maximum flexibility
