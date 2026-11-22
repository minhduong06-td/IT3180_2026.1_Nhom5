# Sơ đồ UML BlueMoon

Sơ đồ chia theo loại trong từng thư mục. Mỗi hình PNG có tệp PlantUML đi kèm.

- `use-case/`: chức năng và vai trò.
- `activity/` và `sequence/`: đăng nhập, phát hành kỳ thu và ghi nhận thanh toán.
- `class/`: một sơ đồ lớp nghiệp vụ tổng hợp, xem các lớp, thuộc tính, kiểu liệt kê và quan hệ chính cùng nhau. Quan hệ nghiệp vụ đã đối chiếu với khóa và dữ liệu trong schema.
- `component/` và `deployment/`: thành phần ứng dụng và cấu hình triển khai Docker mặc định.
- [ERD database demo](erd/ERD_BlueMoon_Demo.svg): sơ đồ cơ sở dữ liệu lấy từ bản mới nhất của repo.

`BlueMoon.asta` là dự án Astah dùng chung, giữ sơ đồ lớp nghiệp vụ tổng hợp cùng các luồng đăng nhập, phát hành và thanh toán. Một số enum trong Astah có tiền tố `Domain` để khỏi trùng tên trong mô hình.

Sơ đồ component và deployment hiện chỉ có PNG và PlantUML; Astah MCP không có thao tác tạo hai loại này. Các nhánh lỗi chi tiết nằm trong PNG activity và sequence, bản Astah chỉ thể hiện luồng chính.
