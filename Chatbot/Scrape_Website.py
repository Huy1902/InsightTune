import requests
from bs4 import BeautifulSoup
from langchain.tools import tool
from pydantic import BaseModel, Field


class ScrapeWebsiteInput(BaseModel):
    """Định nghĩa các tham số cho tool scrape_website."""
    url: str = Field(description="URL hợp lệ của trang web cần cào dữ liệu, ví dụ: 'https://www.example.com'")


@tool(args_schema=ScrapeWebsiteInput)
def scrape_website(url: str) -> str:
    """
    Cào dữ liệu văn bản từ một trang web dựa vào URL của nó.
    Chỉ cào nội dung của một URL duy nhất tại một thời điểm.
    """
    try:
        print("Đang cào dữ liệu từ URL:", url)
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        response = requests.get(url, headers=headers, timeout=100)
        response.raise_for_status()  # Báo lỗi nếu response không thành công

        soup = BeautifulSoup(response.text, 'html.parser')

        for script_or_style in soup(['script', 'style']):
            script_or_style.decompose()

        text = soup.get_text()
        lines = (line.strip() for line in text.splitlines())
        chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
        cleaned_text = '\n'.join(chunk for chunk in chunks if chunk)
        print("Dữ liệu lay tu web la: ",cleaned_text, "\n")
        return cleaned_text

    except requests.RequestException as e:
        return f"Lỗi khi truy cập URL {url}: {e}"
    except Exception as e:
        return f"Đã xảy ra lỗi không xác định khi cào dữ liệu: {e}"