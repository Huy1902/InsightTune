# THAY ĐỔI 1: Import thêm BaseModel và Field từ Pydantic
from langchain.tools import BaseTool
from pydantic import BaseModel, Field
from ddgs import DDGS
from typing import Type

# 'Union' không còn cần thiết nữa

# THAY ĐỔI 2: Định nghĩa một schema rõ ràng cho các tham số đầu vào
class DuckduckgoInput(BaseModel):
    """Định nghĩa các tham số cho tool Duckduckgo."""
    query: str = Field(description="Chuỗi truy vấn tìm kiếm trên DuckDuckGo")


class Duckduckgo_tool(BaseTool):
    name: str = "Duckduckgo_tool"
    description: str = "Một tool dùng để tra cứu thông tin trên internet khi cần thiết. Hãy truyền vào một chuỗi tìm kiếm rõ ràng."

    # THAY ĐỔI 3: Gán schema vừa tạo vào thuộc tính args_schema
    args_schema: Type[DuckduckgoInput] = DuckduckgoInput

    # THAY ĐỔI 4: Sửa lại hàm _run để chỉ chấp nhận một chuỗi 'query' duy nhất
    def _run(self, query: str):
        # Logic xử lý 'isinstance' không còn cần thiết nữa vì đầu vào luôn là một chuỗi
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
        # Đảm bảo hàm async cũng có cùng chữ ký
        return self._run(query)

