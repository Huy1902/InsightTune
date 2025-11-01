import logging
import os  # THAY ĐỔI: Import os để dùng biến môi trường
from typing import Annotated

from langchain_google_genai import ChatGoogleGenerativeAI
from typing_extensions import TypedDict

from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode, tools_condition
from langgraph.checkpoint.memory import InMemorySaver

# Giả sử các file tool của bạn đã được sửa như hướng dẫn trước
from Duckduckgo_tool import Duckduckgo_tool
from Scrape_Website import scrape_website

from langchain_core.messages import AIMessage, HumanMessage, SystemMessage, BaseMessage
# THAY ĐỔI: Import thêm ChatPromptTemplate và MessagesPlaceholder
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(message)s",
    handlers=[ 
        logging.FileHandler("chatbot.log", encoding="utf-8"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# --- THAY ĐỔI 1: Sử dụng biến môi trường cho API Key ---
# Hãy tạo file .env hoặc set biến môi trường hệ thống
# Ví dụ: GOOGLE_API_KEY="YOUR_API_KEY_HERE"
# from dotenv import load_dotenv
# load_dotenv()
# Huy_api_key = os.getenv("GOOGLE_API_KEY")

# Tạm thời vẫn dùng key trực tiếp để bạn test, nhưng nên thay đổi
Huy_api_key = "AIzaSyCe1rN61sbsF2WVXDe3w_PSi-j_DwkbDr4"
if not Huy_api_key:
    raise ValueError("Vui lòng cung cấp Google API Key!")


class State(TypedDict):
    messages: Annotated[list[BaseMessage], add_messages]


# --- THAY ĐỔI 2: Tạo Prompt Template với System Message cố định ---
system_prompt = """
Bạn là một trợ lý âm nhạc hữu ích, có khả năng tìm thông tin về bài hát.
Khi người dùng hỏi về bài hát (tên bài, lời bài hát, hoặc tác giả sáng tác):

1.  Sử dụng Duckduckgo_tool để tìm kiếm thông tin trên internet.
    - Nếu là bài hát tiếng Việt, ưu tiên các trang: loibaihat365.com, nhaccuatui.com, hopamchuan.com.
    - Nếu là bài hát tiếng Anh, ưu tiên các trang: genius.com, azlyrics.com, songfacts.com.

2.  Sau khi nhận được kết quả tìm kiếm (danh sách các URL), chọn URL phù hợp nhất.

3.  Sử dụng scrape_website với URL đã chọn để trích xuất phần lời bài hát hoặc thông tin tác giả.

QUY TẮC BẮT BUỘC:
- LUÔN LUÔN sử dụng tool để tìm kiếm. KHÔNG BAO GIỜ trả lời dựa vào kiến thức có sẵn hoặc bịa đặt thông tin.
- Chỉ trả lời trực tiếp vào câu hỏi của người dùng sau khi đã có thông tin từ tool.
"""

prompt = ChatPromptTemplate.from_messages(
    [
        ("system", system_prompt),
        MessagesPlaceholder(variable_name="messages"),
    ]
)

# --- Thiết lập LLM và Tools ---
llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash", temperature=0.7, google_api_key=Huy_api_key)

search_tool = Duckduckgo_tool()
tools = [search_tool, scrape_website]
llm_with_tools = llm.bind_tools(tools)

# --- THAY ĐỔI 3: Kết hợp Prompt và LLM thành một chuỗi (chain) ---
chain = prompt | llm_with_tools


# --- THAY ĐỔI 4: Đơn giản hóa node chatbot ---
def chatbot(state: State):
    try:
        # Lấy toàn bộ messages từ state (bao gồm cả lịch sử)
        messages = state["messages"]

        # Gọi chuỗi đã có sẵn system prompt
        # LangGraph sẽ tự động đưa `messages` vào `MessagesPlaceholder`
        response = chain.invoke({"messages": messages})

        logger.info(f"LLM response: {response.content}")
        if response.tool_calls:
            logger.info(f"Tool calls detected: {response.tool_calls}")

        return {"messages": [response]}

    except Exception as e:
        logger.exception(f"Error in chatbot node: {e}")
        return {"messages": [AIMessage(content="Đã xảy ra lỗi trong quá trình xử lý.")]}


# --- Xây dựng Graph (Không thay đổi) ---
graph_builder = StateGraph(State)
graph_builder.add_node("chatbot", chatbot)
tool_node = ToolNode(tools=tools)
graph_builder.add_node("tools", tool_node)

graph_builder.add_conditional_edges(
    "chatbot",
    tools_condition,
    {"tools": "tools", END: END}
)

graph_builder.add_edge("tools", "chatbot")
graph_builder.add_edge(START, "chatbot")
memory = InMemorySaver()
graph = graph_builder.compile(checkpointer=memory)


# --- Hàm get_response (Không thay đổi nhiều, nhưng logic giờ đã đúng) ---
def get_response(user_input: str, thread_id: str = "default") -> str:
    logger.info(f"New user input for thread '{thread_id}': {user_input}")
    config = {"configurable": {"thread_id": thread_id}}

    try:
        final_content = ""

        # Input cho graph chỉ cần là HumanMessage
        input_messages = [HumanMessage(content=user_input)]

        for state_update in graph.stream(
                {"messages": input_messages},
                config=config,
                stream_mode="values"
        ):
            # Lấy message cuối cùng từ state được cập nhật
            last_message = state_update["messages"][-1]

            if isinstance(last_message, AIMessage) and last_message.content:
                content = last_message.content

                # Gemini trả về content là một list chứa các 'part' (dictionary)
                if isinstance(content, list):
                    # Lấy text từ part đầu tiên nếu nó tồn tại
                    if content and isinstance(content[0], dict) and 'text' in content[0]:
                        final_content = content[0]['text'].strip()
                # Đề phòng trường hợp content là một string đơn giản
                elif isinstance(content, str):
                    final_content = content.strip()

        return final_content
    except Exception as e:
        logger.exception(f"\nError while streaming graph: {e}")
        return "Đã có lỗi xảy ra khi tạo phản hồi."


if __name__ == "__main__":
    thread_id = "user_session_main"
    print("Chatbot đã sẵn sàng. Gõ 'exit' hoặc 'quit' để thoát.")

    while True:
        try:
            user_input = input("User: ")
            if user_input.lower() in ["exit", "quit"]:
                print("Bot: Tạm biệt!")
                break

            response = get_response(user_input, thread_id)
            print(f"Bot: {response}")

        except Exception as e:
            logger.exception(f"Lỗi không mong muốn trong vòng lặp chính: {e}")
            break