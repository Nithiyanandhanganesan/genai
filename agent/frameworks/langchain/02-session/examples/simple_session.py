"""
Session Management Examples - Simple In-Memory Implementation
"""

import uuid
from datetime import datetime
from typing import Dict, Any, Optional
from langchain.llms import OpenAI
from langchain.memory import ConversationBufferMemory
from langchain.chains import ConversationChain

class SimpleSessionManager:
    """Basic in-memory session manager for development and testing"""

    def __init__(self, llm):
        self.llm = llm
        self.sessions: Dict[str, Dict[str, Any]] = {}

    def create_session(self, user_id: str) -> str:
        """Create a new session for a user"""
        session_id = str(uuid.uuid4())

        # Initialize memory for this session
        memory = ConversationBufferMemory()

        # Create conversation chain with memory
        conversation = ConversationChain(
            llm=self.llm,
            memory=memory,
            verbose=True
        )

        # Store session data
        self.sessions[session_id] = {
            'user_id': user_id,
            'created_at': datetime.now(),
            'last_accessed': datetime.now(),
            'memory': memory,
            'conversation': conversation,
            'metadata': {
                'total_messages': 0,
                'session_active': True
            }
        }

        print(f"Created session {session_id} for user {user_id}")
        return session_id

    def get_session(self, session_id: str) -> Optional[Dict[str, Any]]:
        """Retrieve session data"""
        if session_id in self.sessions:
            # Update last accessed time
            self.sessions[session_id]['last_accessed'] = datetime.now()
            return self.sessions[session_id]
        return None

    def send_message(self, session_id: str, message: str) -> str:
        """Send a message in a session and get response"""
        session = self.get_session(session_id)
        if not session:
            raise ValueError(f"Session {session_id} not found")

        # Get response from conversation chain
        response = session['conversation'].predict(input=message)

        # Update metadata
        session['metadata']['total_messages'] += 1

        return response

    def get_conversation_history(self, session_id: str) -> list:
        """Get conversation history for a session"""
        session = self.get_session(session_id)
        if not session:
            return []

        # Extract messages from memory
        messages = []
        memory = session['memory']

        if hasattr(memory.chat_memory, 'messages'):
            for message in memory.chat_memory.messages:
                messages.append({
                    'type': message.type,
                    'content': message.content,
                    'timestamp': getattr(message, 'timestamp', None)
                })

        return messages

    def end_session(self, session_id: str):
        """End a session and clean up resources"""
        if session_id in self.sessions:
            self.sessions[session_id]['metadata']['session_active'] = False
            print(f"Session {session_id} ended")

    def list_active_sessions(self) -> list:
        """List all active sessions"""
        active_sessions = []
        for session_id, session_data in self.sessions.items():
            if session_data['metadata']['session_active']:
                active_sessions.append({
                    'session_id': session_id,
                    'user_id': session_data['user_id'],
                    'created_at': session_data['created_at'],
                    'total_messages': session_data['metadata']['total_messages']
                })
        return active_sessions

# Example usage
def main():
    # Initialize LLM (you'll need to set your API key)
    llm = OpenAI(temperature=0.7)

    # Create session manager
    session_manager = SimpleSessionManager(llm)

    # Create sessions for different users
    session1 = session_manager.create_session("user_123")
    session2 = session_manager.create_session("user_456")

    print("\n=== Session 1 Conversation ===")
    # Have a conversation in session 1
    response1 = session_manager.send_message(session1, "Hello, my name is Alice")
    print(f"AI: {response1}")

    response2 = session_manager.send_message(session1, "What's my name?")
    print(f"AI: {response2}")

    print("\n=== Session 2 Conversation ===")
    # Have a different conversation in session 2
    response3 = session_manager.send_message(session2, "Hello, my name is Bob")
    print(f"AI: {response3}")

    response4 = session_manager.send_message(session2, "What's my name?")
    print(f"AI: {response4}")

    print("\n=== Session History ===")
    # Check conversation history
    history1 = session_manager.get_conversation_history(session1)
    print(f"Session 1 has {len(history1)} messages")

    history2 = session_manager.get_conversation_history(session2)
    print(f"Session 2 has {len(history2)} messages")

    print("\n=== Active Sessions ===")
    # List active sessions
    active = session_manager.list_active_sessions()
    for session_info in active:
        print(f"Session: {session_info['session_id'][:8]}... "
              f"User: {session_info['user_id']} "
              f"Messages: {session_info['total_messages']}")

if __name__ == "__main__":
    main()
