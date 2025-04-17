from sqlalchemy import Boolean, Column, DateTime, ForeignKey, Integer, String, Table, Text, JSON
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship

# Create the SQLAlchemy base class
Base = declarative_base()
