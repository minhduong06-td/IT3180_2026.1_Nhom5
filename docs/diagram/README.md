# Sơ đồ UML BlueMoon

Các sơ đồ được chia theo loại trong từng thư mục. Mỗi hình PNG có tệp PlantUML tương ứng.

- `use-case/`: chức năng và vai trò.
- `activity/` và `sequence/`: đăng nhập, phát hành kỳ thu và ghi nhận thanh toán.
- `class/overview.png`: sơ đồ lớp nghiệp vụ tổng hợp, trình bày các lớp, thuộc tính, kiểu liệt kê và quan hệ chính. Các quan hệ nghiệp vụ được đối chiếu với khóa và dữ liệu trong schema.
- `class/`: một sơ đồ lớp tổng hợp để xem các lớp nghiệp vụ và quan hệ chính cùng nhau.
- `component/` và `deployment/`: thành phần ứng dụng và cấu hình triển khai Docker mặc định.
- [ERD database demo](erd/ERD_BlueMoon_Demo.svg): sơ đồ cơ sở dữ liệu lấy từ bản mới nhất của repo.

`BlueMoon.asta` là dự án Astah dùng chung. Dự án giữ một sơ đồ lớp nghiệp vụ tổng hợp cùng các luồng đăng nhập, phát hành và thanh toán. Một số enum trong Astah có tiền tố `Domain` để tránh trùng tên trong mô hình.

Các sơ đồ component và deployment hiện có PNG và PlantUML; Astah MCP không cung cấp thao tác tạo hai loại sơ đồ này. Các nhánh lỗi chi tiết có trong PNG activity và sequence; bản Astah thể hiện luồng chính.
