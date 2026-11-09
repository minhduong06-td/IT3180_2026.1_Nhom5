# BlueMoon

BlueMoon là ứng dụng desktop hỗ trợ ban quản trị và kế toán quản lý căn hộ, nhân khẩu, khoản thu và thanh toán của chung cư. Luồng chính đi từ hồ sơ hộ đến phát hành kỳ thu, theo dõi công nợ và đối soát lịch sử thu.

## Chức năng

| Phạm vi | Chức năng đã có trong mã nguồn |
|---|---|
| Luồng Sprint 1 theo bộ tài liệu trong repo | Đăng nhập, tạo tài khoản, đổi mật khẩu; quản lý căn hộ và nhân khẩu; danh mục khoản thu, lịch sử đơn giá; phát hành kỳ thu; bảng kê, in thông báo; thu tiền, nộp dư, công nợ và tổng quan. |
| Phần mở rộng | Đăng ký phương tiện, phí gửi xe; hóa đơn điện/nước/internet; yêu cầu cư dân; đóng góp tự nguyện; lịch sử biến động cư trú; xuất CSV. |

Các chức năng mở rộng đã có code nhưng được theo dõi riêng với phạm vi nghiệm thu Sprint 1. Trạng thái hiện thực và các điểm còn phải xử lý nằm trong [Đối chiếu Sprint 1](docs/SPRINT1_COVERAGE_REVIEW.md).

## Công nghệ và yêu cầu cài đặt

| Thành phần | Phiên bản/cấu hình của dự án |
|---|---|
| JDK | **25**; `pom.xml` đặt `maven.compiler.release=25` |
| JavaFX | 25.0.2 |
| Maven | 3.9.16, chạy bằng Maven Wrapper trong repo |
| MySQL | 8.4, cấu hình sẵn trong `compose.yaml` |
| Truy cập dữ liệu | JDBC và MySQL Connector/J 26.10.0 |
| Mật khẩu | BCrypt, thư viện jBCrypt 0.4 |
| Kiểm thử | JUnit Jupiter 5.13.4 |

Trên Windows, cần cài Git, JDK 25 và Docker Desktop. Bật Docker Desktop trước khi khởi động MySQL. Lần chạy Maven đầu tiên cần mạng để tải Maven và các dependency; không cần cài Maven riêng.

## Khởi động trên Windows

### 1. Lấy mã nguồn và kiểm tra Java

```powershell
git clone https://github.com/minhduong06-td/IT3180_2026.1_Nhom5.git
Set-Location IT3180_2026.1_Nhom5
java -version
javac -version
.\mvnw.cmd -version
docker version
docker compose version
```

Cả `javac` và Java mà Maven sử dụng phải là phiên bản 25. Nếu đang dùng JDK khác, đặt lại `JAVA_HOME` và `PATH`. Ví dụ dưới đây cần thay đường dẫn bằng thư mục JDK 25 thực tế trên máy:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd -version
```

### 2. Khởi động MySQL và ứng dụng

```powershell
docker compose up -d
docker compose ps
```

Chờ dịch vụ `mysql` có trạng thái `healthy`, sau đó chạy:

```powershell
.\mvnw.cmd javafx:run
```

Ứng dụng tự tạo bảng và cập nhật các cấu trúc được hỗ trợ. Database mới không được nạp dữ liệu mẫu. Nếu chưa có tài khoản, ứng dụng yêu cầu tạo tài khoản quản trị đầu tiên; nếu đã có tài khoản, ứng dụng mở màn hình đăng nhập.

### 3. Dừng hoặc tạo lại database

Dừng MySQL và giữ dữ liệu:

```powershell
docker compose down
```

Chỉ khi chấp nhận xóa toàn bộ dữ liệu local, dùng lệnh sau để xóa volume của dự án rồi tạo lại:

```powershell
docker compose down -v
docker compose up -d
```

Lệnh `down -v` xóa toàn bộ dữ liệu trong volume của cấu hình Compose đang dùng. Khi cần nâng cấp database cũ, sao lưu và thử trên bản sao.

## Khởi tạo tài khoản và dữ liệu

Database mới chỉ có cấu trúc bảng, không có tài khoản hoặc dữ liệu nghiệp vụ mẫu. Lần đầu mở ứng dụng, hãy tạo tài khoản quản trị bằng tên đăng nhập, tên hiển thị và mật khẩu của bạn. Sau đó đăng nhập để thêm căn hộ, cư dân, khoản thu và tài khoản kế toán.

Nếu database đã có tài khoản, ứng dụng chuyển thẳng tới màn hình đăng nhập và giữ nguyên dữ liệu hiện có. Cả hai vai trò đều xem tổng quan, tự đổi mật khẩu và ghi nhận đóng góp tự nguyện. Thu tiền cho khoản phải thu bắt buộc chỉ dành cho kế toán. Ban quản trị có thể tạo tài khoản và xem trạng thái; chưa có chức năng chủ động khóa/mở tài khoản hoặc đổi vai trò sau khi tạo. Khóa đăng nhập tạm thời được kích hoạt sau 5 lần sai mật khẩu, kéo dài 5 phút.

## Cấu hình

### Kết nối database ứng dụng

| Biến môi trường | Giá trị mặc định |
|---|---|
| `BLUEMOON_DB_URL` | `jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh` |
| `BLUEMOON_DB_USER` | `bluemoon_app` |
| `BLUEMOON_DB_PASSWORD` | `bluemoon_dev` |

Ví dụ dùng database đã được tạo với tài khoản tương ứng:

```powershell
$env:BLUEMOON_DB_URL = 'jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh'
$env:BLUEMOON_DB_USER = 'bluemoon_app'
$env:BLUEMOON_DB_PASSWORD = 'bluemoon_dev'
.\mvnw.cmd javafx:run
```

Đặt biến ở đây chỉ thay cấu hình kết nối của ứng dụng, không đổi mật khẩu MySQL trong Docker. JDBC URL dùng múi giờ kết nối `Asia/Ho_Chi_Minh`; các ngày mặc định trên giao diện còn phụ thuộc ngày hệ thống của máy chạy ứng dụng.

### Thông tin chuyển khoản

Đặt `BLUEMOON_BANK_NAME`, `BLUEMOON_BANK_ACCOUNT` và `BLUEMOON_BANK_HOLDER` trước khi mở ứng dụng. Nếu thiếu một trong ba biến, thông báo thu phí hướng dẫn liên hệ Ban quản trị.

## Chạy kiểm thử

### Các test không cần MySQL

Chạy tại thư mục repo, với JDK 25:

```powershell
.\mvnw.cmd "-Dtest=AuthorizationTest,BillingRulesTest,ResidenceChangeServiceTest,CsvExporterTest,FeeNoticePrinterTest" test
```

Nhóm này hiện có 13 phương thức test. `FeeNoticePrinterTest` kiểm tra nội dung chuyển khoản; việc xem trước, bố cục trang in và phản hồi giao diện vẫn cần kiểm tra thủ công.

### Kiểm thử tích hợp MySQL

`MySqlBillingFlowTest` có sửa schema và xóa dữ liệu fixture trong quá trình chạy. Dùng **database kiểm thử riêng, có thể bỏ đi**, không trỏ vào database demo đang dùng hoặc dữ liệu thật.

Ví dụ tạo MySQL kiểm thử ở cổng 3308 để tách khỏi ứng dụng ở cổng 3307:

```powershell
docker run --name bluemoon-it-mysql -d `
  -p 127.0.0.1:3308:3306 `
  -e MYSQL_DATABASE=bluemoon_it `
  -e MYSQL_USER=bluemoon_it `
  -e MYSQL_PASSWORD=bluemoon_it_dev `
  -e MYSQL_ROOT_PASSWORD=bluemoon_it_root_dev `
  mysql:8.4
```

Chờ MySQL sẵn sàng. Có thể kiểm tra nhiều lần bằng lệnh sau; chỉ chạy test khi truy vấn trả về `1`:

```powershell
docker exec bluemoon-it-mysql mysql -h 127.0.0.1 -u bluemoon_it -pbluemoon_it_dev bluemoon_it -e 'SELECT 1;'
```

Trong cùng cửa sổ PowerShell:

```powershell
$env:BLUEMOON_IT_DB_URL = 'jdbc:mysql://localhost:3308/bluemoon_it?connectionTimeZone=Asia/Ho_Chi_Minh'
$env:BLUEMOON_IT_DB_USER = 'bluemoon_it'
$env:BLUEMOON_IT_DB_PASSWORD = 'bluemoon_it_dev'
Remove-Item Env:BLUEMOON_IT_DB_LEGACY -ErrorAction SilentlyContinue
.\mvnw.cmd test
```

`BLUEMOON_IT_DB_*` được test đọc riêng, không dùng thay cho `BLUEMOON_DB_*` của ứng dụng. Khi chưa đặt `BLUEMOON_IT_DB_URL`, test tích hợp bị bỏ qua; một lần build thành công có test bị bỏ qua chưa chứng minh luồng MySQL đã đạt.

Chế độ `BLUEMOON_IT_DB_LEGACY=true` tạo fixture database cũ để kiểm tra bootstrap trên schema có dữ liệu một phần. Chỉ dùng chế độ đó trên database kiểm thử mới, riêng biệt.

Sau khi lấy log, có thể xóa riêng container và volume kiểm thử:

```powershell
docker rm -f -v bluemoon-it-mysql
```

Log kết quả từng lớp nằm ở `target/surefire-reports/`. Ghi commit, môi trường, lệnh chạy, số test đạt/lỗi/bỏ qua vào [Biên bản kiểm thử](docs/QA_TEST_REPORT.md).

## Các giới hạn đang theo dõi

| Mã | Nội dung |
|---|---|
| R-02 | Chưa có cơ chế chống ghi trùng cùng một yêu cầu thu tiền/mã giao dịch. |
| R-03 | Code chọn đơn giá theo ngày phát hành; yêu cầu nghiệp vụ đầu kỳ chưa được đáp ứng. |
| R-04 | Mã căn và tên hộ trên chứng từ cũ còn lấy từ hồ sơ hiện tại. |
| R-05 | Tên liên hệ của căn hộ và chủ hộ trong danh sách nhân khẩu có thể không khớp. |
| R-06 | Nhiều thao tác JDBC còn chạy trên luồng giao diện, có thể làm giao diện chờ khi MySQL chậm. |

Chi tiết, cách kiểm tra lại và thứ tự xử lý: [Đối chiếu Sprint 1](docs/SPRINT1_COVERAGE_REVIEW.md). Việc cập nhật tài liệu không thay thế việc sửa các điểm này trong code.

## Tài liệu dự án

- [Hướng dẫn nghiệp vụ và quyền sử dụng](docs/NGHIEP_VU.md)
- [Phạm vi Sprint 1 và các điểm cần hoàn thiện](docs/SPRINT1_COVERAGE_REVIEW.md)
- [Kế hoạch kiểm thử, fixture và số liệu đối chiếu](docs/QA_TEST_PLAN.md)
- [Lịch sử và trạng thái kiểm thử](docs/QA_TEST_REPORT.md)
