# THAY ĐỔI 1: Import thêm BaseModel và Field từ Pydantic
from langchain.tools import BaseTool
from pydantic import BaseModel, Field
from ddgs import DDGS
from typing import Type

class DuckduckgoInput(BaseModel):
    """Định nghĩa các tham số cho tool Duckduckgo."""
    query: str = Field(description="Chuỗi truy vấn tìm kiếm trên DuckDuckGo")


class Duckduckgo_tool(BaseTool):
    name: str = "Duckduckgo_tool"
    description: str = "A tool used to search for information on the Internet when necessary.Provide a clear and specific search query string as input."

    args_schema: Type[DuckduckgoInput] = DuckduckgoInput

    def _run(self, query: str):
        print(f"Đang tìm kiếm trên DuckDuckGo cho: '{query}'")
        try:
            with DDGS() as ddgs:
                # Lấy tối đa 5 kết quả
                results = [r for r in ddgs.text(query, max_results=5)]
                print(f"Đã tìm thấy {len(results)} kết quả.")
                return results
        except Exception as e:
            return f"Đã xảy ra lỗi khi tìm kiếm: {e}"

    async def _arun(self, query: str):
        return self._run(query)

