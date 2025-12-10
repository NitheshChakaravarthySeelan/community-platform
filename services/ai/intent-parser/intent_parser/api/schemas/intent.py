from pydantic import BaseModel, Field
from typing import Dict, Any, List, Optional

class Action(BaseModel):
    name: str = Field(..., description="Name of the action to be performed (e.g., add_to_cart, checkout, search_product, get_product_details)")
    params: Dict[str, Any] = Field(default_factory=dict, description="Parameters for the action")

class Intent(BaseModel):
    user_query: str = Field(..., description="The original natural language query from the user")
    action: Optional[Action] = Field(None, description="The primary action identified from the user's query")
    follow_up_questions: List[str] = Field(default_factory=list, description="Questions to ask the user for clarification or more information")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Confidence score of the intent parsing")
    raw_llm_response: Dict[str, Any] = Field(default_factory=dict, description="Raw response from the LLM for debugging/auditing")
