import logging
import json
from typing import Annotated
import re
from langchain_google_genai import ChatGoogleGenerativeAI
from typing_extensions import TypedDict

from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode, tools_condition
from langgraph.checkpoint.memory import InMemorySaver

from Duckduckgo_tool import Duckduckgo_tool
from Scrape_Website import scrape_website

from langchain_core.messages import AIMessage, HumanMessage, BaseMessage
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

Api_key = "AIzaSyCe1rN61sbsF2WVXDe3w_PSi-j_DwkbDr4"
if not Api_key:
    raise ValueError("Vui lòng cung cấp Google API Key!")


class State(TypedDict):
    messages: Annotated[list[BaseMessage], add_messages]


# system_prompt = """
# # Bạn là một trợ lý âm nhạc hữu ích
# # , có khả năng tìm thông tin về bài hát và phát nhạc tùy theo nhu cầu, mong muốn của người dùng.
# #
# # Nếu đầu ra của người dùng muốn là nghe hoặc mở một bài hát, hãy trả về cho output cuối cùng là:
# # {{"type": "playMusic", "song_name": "<tên bài hát>"}}
# #
# # Nếu chỉ cần thông tin (vd: tác giả, lời bài hát, ý nghĩa), thì output cuối cùng trả về:
# # {{"type": "reply", "reply": "<nội dung trả lời>"}}
# #
# # Bạn cũng có thể giúp gợi ý một vài bài hát cho người dùng để họ tham khảo và có thể phát bài hát đấy khi người dùng muốn nghe.
# # Khi người dùng hỏi về bài hát (tên bài, lời bài hát, tác giả sáng tác hoặc bất cứ thứ gì liên quan đến âm nhạc):
# #
# # 1.  Sử dụng Duckduckgo_tool để tìm kiếm thông tin trên internet.
# #     - Nếu là bài hát tiếng Việt, ưu tiên các trang: loibaihat365.com, nhaccuatui.com, hopamchuan.com.
# #     - Nếu là bài hát tiếng Anh, ưu tiên các trang: genius.com, azlyrics.com, songfacts.com.
# #
# # 2.  Sau khi nhận được kết quả tìm kiếm (danh sách các URL), chọn URL phù hợp nhất.
# #
# # 3.  Sử dụng scrape_website với URL đã chọn để trích xuất phần lời bài hát hoặc thông tin tác giả.
# #
# # QUY TẮC BẮT BUỘC:
# # - LUÔN LUÔN sử dụng tool để tìm kiếm. KHÔNG BAO GIỜ trả lời dựa vào kiến thức có sẵn hoặc bịa đặt thông tin.
# # - Chỉ trả lời trực tiếp vào câu hỏi của người dùng sau khi đã có thông tin từ tool.
# # """
system_prompt = """
You are a helpful music assistant capable of solving various user problems related to music.
You have the following tools to help you find the most relevant information to answer user queries accurately:
1.Use Duckduckgo_tool to search for information on the Internet.
  - If the song is Vietnamese, prioritize these websites: loibaihat365.com, nhaccuatui.com, hopamchuan.com.
  - If the song is English, prioritize these websites: genius.com, azlyrics.com, songfacts.com.
2. After receiving the search results (a list of URLs), choose the most relevant URL.
3. Use scrape_website with the selected URL to extract the lyrics or artist information.
Once you have gathered the useful information, analyze the output and determine its type:
 - If the user’s request is to play music, the output should be:
    {"type": "playMusic", "song_name": "<song name>"}
 - If the user’s request is to get an explanation or information about music, the output should be:
    {"type": "reply", "reply": "<your response>"}
MANDATORY RULES:
- NEVER answer based on your pre-existing knowledge or make up information.
- Only respond directly to the user’s question after obtaining information using the tools.
"""

prompt = ChatPromptTemplate.from_messages(
    [
        ("system", system_prompt),
        MessagesPlaceholder(variable_name="messages"),
    ]
)

llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash", temperature=0.7, google_api_key=Api_key)

search_tool = Duckduckgo_tool()
tools = [search_tool, scrape_website]
llm_with_tools = llm.bind_tools(tools)

chain = prompt | llm_with_tools


def chatbot(state: State):
    try:
        messages = state["messages"]

        response = chain.invoke({"messages": messages})

        logger.info(f"LLM response: {response.content}")
        if response.tool_calls:
            logger.info(f"Tool calls detected: {response.tool_calls}")

        return {"messages": [response]}

    except Exception as e:
        logger.exception(f"Error in chatbot node: {e}")
        return {"messages": [AIMessage(content="Đã xảy ra lỗi trong quá trình xử lý.")]}


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


def get_response(user_input: str, thread_id: str = "default") -> str:
    logger.info(f"New user input for thread '{thread_id}': {user_input}")
    config = {"configurable": {"thread_id": thread_id}}

    try:
        input_messages = [HumanMessage(content=user_input)]

        last_state = None
        for state_update in graph.stream(
                {"messages": input_messages},
                config=config,
                stream_mode="values"
        ):
            last_state = state_update

        if not last_state:
            return "Không nhận được phản hồi từ mô hình."

        last_message = last_state["messages"][-1]
        final_content = last_message.content

        if isinstance(final_content, list):
            text_parts = []
            for part in final_content:
                if isinstance(part, dict) and "text" in part:
                    text_parts.append(part["text"])
            final_content = " ".join(text_parts).strip()

        cleaned = re.sub(r"```(?:json)?|```", "", str(final_content)).strip()

        try:
            parsed = json.loads(cleaned)

            return parsed
        except json.JSONDecodeError:
            logger.warning(f"Không parse được JSON: {cleaned}")
            return {"type": "reply", "reply": cleaned}


    except Exception as e:

        logger.exception(f"\nError while streaming graph: {e}")

        return {"type": "error", "reply": "Đã có lỗi xảy ra khi tạo phản hồi."}

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